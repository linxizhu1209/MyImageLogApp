package com.example.myimagelogapp.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.myimagelogapp.data.AppDatabase
import com.example.myimagelogapp.data.remote.UploadItemDto
import com.example.myimagelogapp.entity.UploadTaskEntity
import com.example.myimagelogapp.util.UploadNotifications
import com.example.myimagelogapp.util.toFile
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

class UploadWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        UploadNotifications.ensureChannel(applicationContext)

        val db = AppDatabase.get(applicationContext)
        val dao = db.uploadTaskDao()

        val title = inputData.getString(KEY_TITLE).orEmpty()
        val content = inputData.getString(KEY_CONTENT).orEmpty()
        val photoCount = inputData.getInt(KEY_PHOTO_COUNT, 0)

        val userId = inputData.getLong(KEY_USER_ID, -1L)
        val uriStrings = inputData.getStringArray(KEY_URIS)?.toList().orEmpty()


        // 실패 시뮬레이션
        val failUntilAttempt = inputData.getInt(KEY_FAIL_UNTIL_ATTEMPT, 0)
        Log.d("UploadWorker", "START attempt=$runAttemptCount userId=$userId uriCount=${uriStrings.size} failUntil=$failUntilAttempt")
        if (runAttemptCount < failUntilAttempt) {
            setForeground(createForegroundInfo(title, 0, "retrying... attempt=${runAttemptCount + 1}"))
            dao.upsert(
                UploadTaskEntity(
                    workId = id.toString(),
                    title = title,
                    content = content,
                    photoCount = photoCount,
                    status = "RETRY",
                    progress = 0,
                    createdAt = System.currentTimeMillis(),
                    errorMessage = "Simulated failure (attempt=${runAttemptCount + 1})"
                )
            )
            return Result.retry()
        }

        // 입력 검증
        if (userId <= 0L) {
            dao.upsert(
                UploadTaskEntity(
                    workId = id.toString(),
                    title = title,
                    content = content,
                    photoCount = photoCount,
                    status = "FAILED",
                    progress = 0,
                    createdAt = System.currentTimeMillis(),
                    errorMessage = "Invalid userId"
                )
            )
            return Result.failure()
        }
        if (uriStrings.isEmpty()) {
            dao.upsert(
                UploadTaskEntity(
                    workId = id.toString(),
                    title = title,
                    content = content,
                    photoCount = photoCount,
                    status = "FAILED",
                    progress = 0,
                    createdAt = System.currentTimeMillis(),
                    errorMessage = "No photos to upload"
                )
            )
            return Result.failure()
        }

        setForeground(createForegroundInfo(title, 0, "starting..."))

        dao.upsert(
            UploadTaskEntity(
                workId = id.toString(),
                title = title,
                content = content,
                photoCount = photoCount,
                status = "RUNNING",
                progress = 0,
                createdAt = System.currentTimeMillis(),
                errorMessage = null
            )
        )

        // Uri -> File 변환
        val files = uriStrings.mapNotNull { it.toFile(applicationContext) }
        Log.d("UploadWorker", "converted files=${files.size}")
        if (files.isEmpty()) {
            dao.upsert(
                UploadTaskEntity(
                    workId = id.toString(),
                    title = title,
                    content = content,
                    photoCount = photoCount,
                    status = "FAILED",
                    progress = 0,
                    createdAt = System.currentTimeMillis(),
                    errorMessage = "Failed to convert Uris to Files"
                )
            )
            return Result.failure()
        }
        // 업로드 호출
        val repo = UploadWorkerDeps.repoProvider(applicationContext)


        return try {
            Log.d("UploadWorker", "calling repo.upload() files=${files.size}")
            setForeground(createForegroundInfo(title, 40, "uploading..."))
            val res = repo.upload(userId, files) // 서버의 upload 호출

            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val type = Types.newParameterizedType(List::class.java, UploadItemDto::class.java)
            val adapter = moshi.adapter<List<UploadItemDto>>(type)

            val itemsJson = adapter.toJson(res.items)

            dao.upsert(
                UploadTaskEntity(
                    workId = id.toString(),
                    title = title,
                    content = content,
                    photoCount = photoCount,
                    status = "SUCCEEDED",
                    progress = 100,
                    createdAt = System.currentTimeMillis(),
                    errorMessage = null,
                    resultJson = itemsJson
                )
            )
            Log.d("UploadWorker", "upload success items=${res.items.size}")
            return Result.success(
                workDataOf(
                    KEY_RESULT_JSON to itemsJson
                )
            )
        } catch (e: HttpException) {
            val code = e.code()

            val shouldRetry = code in 500..599

            dao.upsert(UploadTaskEntity(
                workId = id.toString(),
                title = title,
                content = content,
                photoCount = photoCount,
                status = if (shouldRetry) "RETRY" else "FAILED",
                progress = 40,
                createdAt = System.currentTimeMillis(),
                errorMessage = "HTTP $code"
            ))
            if (shouldRetry) Result.retry()
            else
                Log.e("UploadWorker", "FAIL: Invalid userId=$userId")
                Result.failure()
        } catch (e: IOException) {
            Log.e("UploadWorker", "Network error", e)
            dao.upsert(
                UploadTaskEntity(
                    workId = id.toString(),
                    title = title,
                    content = content,
                    photoCount = photoCount,
                    status = "RETRY",
                    progress = 40,
                    createdAt = System.currentTimeMillis(),
                    errorMessage = "Network error"
                )
            )
            Result.retry()
        } catch (e: Exception) {
            Log.e("UploadWorker", "Unknown error", e)
            dao.upsert(
                UploadTaskEntity(
                    workId = id.toString(),
                    title = title,
                    content = content,
                    photoCount = photoCount,
                    status = "FAILED",
                    progress = 40,
                    createdAt = System.currentTimeMillis(),
                    errorMessage = e.message ?: "Unknown error"
                )
            )
            Result.failure()
        } finally {
            files.forEach { runCatching { it.delete() } }
        }

    }

    private fun createForegroundInfo(title: String, progress: Int, msg: String): ForegroundInfo {
        val notificationId = id.hashCode()
        val notification = UploadNotifications.buildProgress(
            context = applicationContext,
            title = title.ifBlank { "Uploading" },
            progress = progress,
            isIndeterminate = false,
            contentText = msg
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Q(API 29)+ : Foreground service type을 명시
            ForegroundInfo(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    companion object {
        const val KEY_USER_ID = "userId"
        const val KEY_URIS = "uris"
        const val KEY_TITLE = "title"
        const val KEY_CONTENT = "content"
        const val KEY_PHOTO_COUNT = "photoCount"
        const val KEY_PROGRESS = "progress"

        const val KEY_FAIL_UNTIL_ATTEMPT = "failUntilAttempt"
        const val KEY_RESULT_JSON = "resultJson"
        const val TAG_UPLOAD = "tag_upload"
    }

}