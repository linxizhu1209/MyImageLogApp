package com.example.myimagelogapp

import android.content.Context
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.myimagelogapp.data.remote.RetrofitProvider
import com.example.myimagelogapp.data.remote.UploadItemDto
import com.example.myimagelogapp.data.remote.UploadResultDto
import com.example.myimagelogapp.data.remote.WeekImagesResponseDto
import com.example.myimagelogapp.data.repository.ImageRepository
import com.example.myimagelogapp.viewModel.ImageRepositoryContract
import com.example.myimagelogapp.worker.UploadWorker
import com.example.myimagelogapp.worker.UploadWorkerDeps
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class UploadWorkerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
//        UploadWorkerDeps.repoProvider = {
//            ImageRepository(RetrofitProvider.imageApi)
//        }
    }

    @Test
    fun doWork_success_returnsSuccess() = runBlocking {
        // given: 테스트용 이미지 파일 만들고 content Uri 생성
        val temp = File(context.cacheDir, "test_upload.jpg")
        FileOutputStream(temp).use { it.write(byteArrayOf(1,2,3,4)) }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            temp
        )

        // repo를 성공하는 fake로 교체
        UploadWorkerDeps.repoProvider = {
            object : ImageRepositoryContract {
                override suspend fun upload(userId: Long, files: List<File>): UploadResultDto {
                    // Worker가 Uri -> File 변환을 제대로 했는지 검증
                    require(files.isNotEmpty())
                    require(userId == 1L)

                    return UploadResultDto(
                        status = "OK",
                        items = listOf(UploadItemDto(1, "http://x/1.jpg", "a.jpg", 10))
                    )
                }

                override suspend fun loadThisWeek(userId: Long): WeekImagesResponseDto {
                    return WeekImagesResponseDto(
                        weekStart = "2026-02-03",
                        weekEnd = "2026-02-09",
                        days = emptyList()
                    )
                }
            }
        }

        val input = Data.Builder()
            .putLong(UploadWorker.KEY_USER_ID, 1L)
            .putStringArray(UploadWorker.KEY_URIS, arrayOf(uri.toString()))
            .putString(UploadWorker.KEY_TITLE, "t")
            .putString(UploadWorker.KEY_CONTENT, "c")
            .putInt(UploadWorker.KEY_PHOTO_COUNT, 1)
            .putInt(UploadWorker.KEY_FAIL_UNTIL_ATTEMPT, 0)
            .build()

        val worker = TestListenableWorkerBuilder<UploadWorker>(context)
            .setInputData(input)
            .build()

        // when
        val result = worker.doWork()

        // then
        assertEquals(androidx.work.ListenableWorker.Result.success()::class, result::class)
    }

    /**
     * 실패 테스트 (상태코드 400이면 retry가 아니라 failure나와야함)
     */
    @Test
    fun doWork_http400_returnsFailure() = runBlocking {
        // given : 테스트용 content Uri 하나 만들어두기
        val temp = File(context.cacheDir, "test_fail.jpg")
        FileOutputStream(temp).use { it.write(byteArrayOf(9, 9, 9)) }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            temp
        )

        UploadWorkerDeps.repoProvider = {
            object : ImageRepositoryContract {
                override suspend fun upload(userId: Long, files: List<File>): UploadResultDto {
                    val errorBody = """{"message":"bad request"}"""
                        .toResponseBody("application/json".toMediaType())
                    throw HttpException(Response.error<Unit>(400, errorBody))
                }

                override suspend fun loadThisWeek(userId: Long): WeekImagesResponseDto {
                    return WeekImagesResponseDto(
                        weekStart = "2026-02-03",
                        weekEnd = "2026-02-09",
                        days = emptyList()
                    )
                }
            }
        }
        val input = Data.Builder()
            .putLong(UploadWorker.KEY_USER_ID, 1L)
            .putStringArray(UploadWorker.KEY_URIS, arrayOf(uri.toString()))
            .putString(UploadWorker.KEY_TITLE, "t")
            .putString(UploadWorker.KEY_CONTENT, "c")
            .putInt(UploadWorker.KEY_PHOTO_COUNT, 0)
            .putInt(UploadWorker.KEY_FAIL_UNTIL_ATTEMPT, 0)
            .build()

        val worker = TestListenableWorkerBuilder<UploadWorker>(context)
            .setInputData(input)
            .build()

        // when
        val result = worker.doWork()

        // then
        assertEquals(androidx.work.ListenableWorker.Result.failure()::class, result::class)

    }

}