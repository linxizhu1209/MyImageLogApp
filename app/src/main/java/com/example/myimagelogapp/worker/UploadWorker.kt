package com.example.myimagelogapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.myimagelogapp.data.AppDatabase
import com.example.myimagelogapp.entity.UploadTaskEntity
import kotlinx.coroutines.delay

class UploadWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.get(applicationContext)
        val dao = db.uploadTaskDao()

        val title = inputData.getString(KEY_TITLE).orEmpty()
        val content = inputData.getString(KEY_CONTENT).orEmpty()
        val photoCount = inputData.getInt(KEY_PHOTO_COUNT, 0)

        dao.upsert(
            UploadTaskEntity(
                workId = id.toString(),
                title = title,
                content = content,
                photoCount = photoCount,
                status = "RUNNING",
                progress = 0,
                createdAt = System.currentTimeMillis()
            )
        )

        for (p in 1..100 step 5) {
            setProgress(workDataOf(KEY_PROGRESS to p))
            dao.upsert(
                UploadTaskEntity(
                    workId = id.toString(),
                    title = title,
                    content = content,
                    photoCount = photoCount,
                    status = "RUNNING",
                    progress = p,
                    createdAt = System.currentTimeMillis()
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
                createdAt = System.currentTimeMillis()
            )
        )
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_CONTENT = "content"
        const val KEY_PHOTO_COUNT = "photoCount"
        const val KEY_PROGRESS = "progress"
        const val TAG_UPLOAD = "tag_upload"
    }

}