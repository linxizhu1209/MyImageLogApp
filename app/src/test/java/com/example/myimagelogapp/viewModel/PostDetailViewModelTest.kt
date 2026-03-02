package com.example.myimagelogapp.viewModel

import com.example.myimagelogapp.data.remote.ImageApi
import com.example.myimagelogapp.data.remote.PostDetailDtos
import com.example.myimagelogapp.data.remote.UpdatePostRequest
import com.example.myimagelogapp.data.remote.UploadResultDto
import com.example.myimagelogapp.data.remote.WeekImagesResponseDto
import com.example.myimagelogapp.data.repository.ImageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * PostDetailViewModel의 글 조회 및 수정 기능을 검증하는 단위 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PostDetailViewModelTest {

    /** 테스트에서 Main Dispatcher를 TestDispatcher로 교체하는 Rule */
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /**
     * loadPost 성공 시 postData에 제목, 내용, 이미지URL, 시간이 설정되는지 검증
     */
    @Test
    fun loadPost_success_updatesPostData() = runTest {
        // given
        val expectedDto = PostDetailDtos(
            id = 1L,
            title = "테스트 제목",
            content = "테스트 내용",
            imageUrl = "http://test/image.jpg",
            createdAt = "2026-03-02T12:00:00",
            updatedAt = "2026-03-02T13:00:00"
        )
        val fakeApi = createFakeApi(postDetail = expectedDto)
        val repo = ImageRepository(fakeApi)
        val vm = PostDetailViewModel(repo)

        // when
        vm.loadPost(imageId = 1L)
        advanceUntilIdle()

        // then
        val data = vm.postData.value
        assertNotNull("postData가 null이 아님", data)
        assertEquals("테스트 제목", data?.title)
        assertEquals("테스트 내용", data?.content)
        assertEquals("http://test/image.jpg", data?.imageUrl)
        assertEquals("2026-03-02T12:00:00", data?.createdAt)
        assertEquals("2026-03-02T13:00:00", data?.updatedAt)
    }

    /**
     * loadPost 실패(null 반환) 시 postData가 null인 상태 유지 검증
     */
    @Test
    fun loadPost_notFound_postDataRemainsNull() = runTest {
        // given
        val fakeApi = createFakeApi(postDetail = null)
        val repo = ImageRepository(fakeApi)
        val vm = PostDetailViewModel(repo)

        // when
        vm.loadPost(imageId = 999L)
        advanceUntilIdle()

        // then
        assertNull("postData가 null이어야 함", vm.postData.value)
    }

    /**
     * updatePost 성공 시 Updated 이벤트가 발생하는지 검증
     */
    @Test
    fun updatePost_success_emitsUpdatedEvent() = runTest {
        // given
        var updateCalled = false
        val fakeApi = object : ImageApi {
            override suspend fun uploadImages(userId: Long, title: String?, content: String?, files: List<MultipartBody.Part>): UploadResultDto =
                throw NotImplementedError()
            override suspend fun getThisWeek(userId: Long): WeekImagesResponseDto =
                throw NotImplementedError()
            override suspend fun getPostDetail(imageId: Long): PostDetailDtos? = null
            override suspend fun updatePost(imageId: Long, body: UpdatePostRequest) {
                updateCalled = true
            }
        }
        val repo = ImageRepository(fakeApi)
        val vm = PostDetailViewModel(repo)

        // events 수집 준비
        val events = mutableListOf<PostDetailViewModel.Event>()
        val job = launch {
            vm.events.collect { events.add(it) }
        }

        // when
        vm.updatePost(imageId = 1L, title = "새 제목", content = "새 내용")
        advanceUntilIdle()

        // then
        assertTrue("updatePost가 호출되어야 함", updateCalled)
        assertEquals("Updated 이벤트 1개", 1, events.size)
        assertTrue("Updated 이벤트여야 함", events[0] is PostDetailViewModel.Event.Updated)

        job.cancel()
    }

    /**
     * updatePost 실패 시 Error 이벤트가 발생하는지 검증
     */
    @Test
    fun updatePost_failure_emitsErrorEvent() = runTest {
        // given
        val fakeApi = object : ImageApi {
            override suspend fun uploadImages(userId: Long, title: String?, content: String?, files: List<MultipartBody.Part>): UploadResultDto =
                throw NotImplementedError()
            override suspend fun getThisWeek(userId: Long): WeekImagesResponseDto =
                throw NotImplementedError()
            override suspend fun getPostDetail(imageId: Long): PostDetailDtos? = null
            override suspend fun updatePost(imageId: Long, body: UpdatePostRequest) {
                throw RuntimeException("수정 실패")
            }
        }
        val repo = ImageRepository(fakeApi)
        val vm = PostDetailViewModel(repo)

        // events 수집 준비
        val events = mutableListOf<PostDetailViewModel.Event>()
        val job = launch {
            vm.events.collect { events.add(it) }
        }

        // when
        vm.updatePost(imageId = 1L, title = "제목", content = "내용")
        advanceUntilIdle()

        // then
        assertEquals("이벤트 1개", 1, events.size)
        assertTrue("Error 이벤트여야 함", events[0] is PostDetailViewModel.Event.Error)
        assertEquals("수정 실패", (events[0] as PostDetailViewModel.Event.Error).message)

        job.cancel()
    }

    /** 테스트용 Fake API 생성 헬퍼 함수 */
    private fun createFakeApi(
        postDetail: PostDetailDtos?
    ): ImageApi = object : ImageApi {
        override suspend fun uploadImages(userId: Long, title: String?, content: String?, files: List<MultipartBody.Part>): UploadResultDto =
            throw NotImplementedError()
        override suspend fun getThisWeek(userId: Long): WeekImagesResponseDto =
            throw NotImplementedError()
        override suspend fun getPostDetail(imageId: Long): PostDetailDtos? = postDetail
        override suspend fun updatePost(imageId: Long, body: UpdatePostRequest) {}
    }
}
