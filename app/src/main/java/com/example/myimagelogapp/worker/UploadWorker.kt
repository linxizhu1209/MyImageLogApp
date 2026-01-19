package com.example.myimagelogapp.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.myimagelogapp.data.AppDatabase
import com.example.myimagelogapp.entity.UploadTaskEntity
import com.example.myimagelogapp.util.UploadNotifications
import kotlinx.coroutines.delay

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

        // 실패 시뮬레이션
        val failUntilAttemp = inputData.getInt(KEY_FAIL_UNTIL_ATTEMPT, 0)

        if (runAttemptCount < failUntilAttemp) {
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


        for (p in 1..100 step 5) {
            setProgress(workDataOf(KEY_PROGRESS to p))

            setForeground(createForegroundInfo(title, p, "uploading..."))
            dao.upsert(
                UploadTaskEntity(
                    workId = id.toString(),
                    title = title,
                    content = content,
                    photoCount = photoCount,
                    status = "RUNNING",
                    progress = p,
                    createdAt = System.currentTimeMillis(),
                    errorMessage = null
                )
            )
            delay(120)
        }

        dao.upsert(
            UploadTaskEntity(
                workId = id.toString(),
                title = title,
                content = content,
                photoCount = photoCount,
                status = "SUCCEEDED",
                progress = 100,
                createdAt = System.currentTimeMillis(),
                errorMessage = null
            )
        )
        return Result.success()
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
        const val KEY_TITLE = "title"
        const val KEY_CONTENT = "content"
        const val KEY_PHOTO_COUNT = "photoCount"
        const val KEY_PROGRESS = "progress"

        const val KEY_FAIL_UNTIL_ATTEMPT = "failUntilAttempt"
        const val TAG_UPLOAD = "tag_upload"
    }

}