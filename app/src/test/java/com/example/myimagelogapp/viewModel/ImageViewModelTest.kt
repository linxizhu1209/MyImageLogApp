package com.example.myimagelogapp.viewModel

import com.example.myimagelogapp.data.remote.UploadItemDto
import com.example.myimagelogapp.data.remote.UploadResultDto
import com.example.myimagelogapp.data.remote.WeekImagesResponseDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ImageViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uploadImages_success_emitsUploadSuccess() = runTest {
        //given
        val fakeRepo = object : ImageRepositoryContract {
            override suspend fun upload(userId: Long, files: List<File>): UploadResultDto {
                return UploadResultDto(
                    status = "OK",
                    items = listOf(UploadItemDto(1L, "http://x/1.jpg", "a.jpg", 123))
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
        val vm = ImageViewModel(fakeRepo)

        // when
        vm.uploadImages(userId = 10L, files = emptyList())
        advanceUntilIdle()

        //then
        val state = vm.state.value
        assertTrue(state is ImageUiState.UploadSuccess)
        val success = state as ImageUiState.UploadSuccess
        assertEquals("OK", success.result.status)
        assertEquals(1L, success.result.items.first().id)

    }


}