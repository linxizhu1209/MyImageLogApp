package com.example.myimagelogapp.viewModel

import com.example.myimagelogapp.data.remote.PostDetailDtos
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

/**
 * ImageViewModel의 업로드 기능을 검증하는 단위 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ImageViewModelTest {

    /** 테스트에서 Main Dispatcher를 TestDispatcher로 교체하는 Rule */
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /**
     * uploadImages 성공 시 UploadSuccess 상태로 결과가 전달되는지 검증
     */
    @Test
    fun uploadImages_success_emitsUploadSuccess() = runTest {
        // given: 성공 응답을 반환하는 fake repository
        val fakeRepo = object : ImageRepositoryContract {
            override suspend fun upload(userId: Long, title: String?, content: String?, files: List<File>): UploadResultDto {
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

            override suspend fun getPostDetail(imageId: Long): PostDetailDtos? = null

            override suspend fun updatePost(imageId: Long, title: String, content: String) {}
        }
        val vm = ImageViewModel(fakeRepo)

        // when: 업로드 호출
        vm.uploadImages(userId = 10L, title = "테스트 제목", content = "테스트 내용", files = emptyList())
        advanceUntilIdle()

        // then: UploadSuccess 상태이고 결과 확인
        val state = vm.state.value
        assertTrue("UploadSuccess 상태여야 함", state is ImageUiState.UploadSuccess)
        val success = state as ImageUiState.UploadSuccess
        assertEquals("OK", success.result.status)
        assertEquals(1L, success.result.items.first().id)
    }

    /**
     * uploadImages 호출 시 Loading 상태를 먼저 거치는지 검증
     */
    @Test
    fun uploadImages_emitsLoadingFirst() = runTest {
        // given
        val fakeRepo = object : ImageRepositoryContract {
            override suspend fun upload(userId: Long, title: String?, content: String?, files: List<File>): UploadResultDto {
                return UploadResultDto(status = "OK", items = emptyList())
            }
            override suspend fun loadThisWeek(userId: Long) = WeekImagesResponseDto("", "", emptyList())
            override suspend fun getPostDetail(imageId: Long): PostDetailDtos? = null
            override suspend fun updatePost(imageId: Long, title: String, content: String) {}
        }
        val vm = ImageViewModel(fakeRepo)

        // when: 호출 직후 (advanceUntilIdle 전)
        vm.uploadImages(userId = 1L, title = null, content = null, files = emptyList())

        // then: Loading 상태
        assertTrue("Loading 상태여야 함", vm.state.value is ImageUiState.Loading)

        advanceUntilIdle()

        // 완료 후 UploadSuccess
        assertTrue("완료 후 UploadSuccess 상태", vm.state.value is ImageUiState.UploadSuccess)
    }
}