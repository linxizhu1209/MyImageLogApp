package com.example.myimagelogapp.viewModel

import com.example.myimagelogapp.data.remote.DayImagesDto
import com.example.myimagelogapp.data.remote.PostDetailDtos
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
 * HomeViewModel의 loadThisWeek 및 최신순 정렬 로직을 검증하는 단위 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    /** 테스트에서 Main Dispatcher를 TestDispatcher로 교체하는 Rule */
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /**
     * loadThisWeek 성공 시, 서버 응답이 최신순(sortKey 내림차순)으로 정렬되어
     * Success 상태로 전달되는지 검증
     */
    @Test
    fun loadThisWeek_success_sortsByNewestFirst() = runTest {
        // given: 오래된 이미지와 최신 이미지를 포함한 서버 응답
        val olderImage = DayImagesDto.ImageSummaryDto(
            id = 1L,
            url = "http://test/1.jpg",
            originalName = "old.jpg",
            size = 100L,
            createdAt = "2026-02-20T10:00:00"
        )
        val newerImage = DayImagesDto.ImageSummaryDto(
            id = 2L,
            url = "http://test/2.jpg",
            originalName = "new.jpg",
            size = 200L,
            createdAt = "2026-02-22T15:30:00"
        )

        val fakeRepo = createFakeRepo(
            weekResponse = WeekImagesResponseDto(
                weekStart = "2026-02-17",
                weekEnd = "2026-02-23",
                days = listOf(
                    DayImagesDto("THURSDAY", "2026-02-20", listOf(olderImage)),
                    DayImagesDto("SATURDAY", "2026-02-22", listOf(newerImage))
                )
            )
        )

        val vm = HomeViewModel(fakeRepo)

        // when: loadThisWeek 호출
        vm.loadThisWeek(userId = 1L)
        advanceUntilIdle()

        // then: Success 상태이고, 최신 이미지가 먼저 정렬됨
        val state = vm.state.value
        assertTrue("상태가 Success여야 함", state is HomeUiState.Success)

        val items = (state as HomeUiState.Success).items
        assertEquals("아이템 개수가 2개여야 함", 2, items.size)
        assertEquals("첫 번째는 최신 이미지(id=2)", 2L, items[0].id)
        assertEquals("두 번째는 오래된 이미지(id=1)", 1L, items[1].id)
    }

    /**
     * loadThisWeek 호출 시 Loading 상태를 먼저 거치는지 검증
     */
    @Test
    fun loadThisWeek_emitsLoadingFirst() = runTest {
        // given
        val fakeRepo = createFakeRepo(
            weekResponse = WeekImagesResponseDto("2026-02-17", "2026-02-23", emptyList())
        )
        val vm = HomeViewModel(fakeRepo)

        // when: 호출 직후 (advanceUntilIdle 전)
        vm.loadThisWeek(userId = 1L)

        // then: Loading 상태
        assertTrue("Loading 상태여야 함", vm.state.value is HomeUiState.Loading)

        advanceUntilIdle()

        // 완료 후 Success
        assertTrue("완료 후 Success 상태", vm.state.value is HomeUiState.Success)
    }

    /**
     * loadThisWeek 실패 시 Error 상태로 에러 메시지가 전달되는지 검증
     */
    @Test
    fun loadThisWeek_failure_emitsError() = runTest {
        // given: 예외를 던지는 fake repository
        val fakeRepo = object : ImageRepositoryContract {
            override suspend fun upload(userId: Long, title: String?, content: String?, files: List<File>) =
                throw NotImplementedError()
            override suspend fun loadThisWeek(userId: Long): WeekImagesResponseDto =
                throw RuntimeException("네트워크 오류")
            override suspend fun getPostDetail(imageId: Long): PostDetailDtos? = null
            override suspend fun updatePost(imageId: Long, title: String, content: String) {}
        }

        val vm = HomeViewModel(fakeRepo)

        // when
        vm.loadThisWeek(userId = 1L)
        advanceUntilIdle()

        // then
        val state = vm.state.value
        assertTrue("Error 상태여야 함", state is HomeUiState.Error)
        assertEquals("네트워크 오류", (state as HomeUiState.Error).message)
    }

    /**
     * 빈 응답일 때 Success 상태이지만 items가 비어있는지 검증
     */
    @Test
    fun loadThisWeek_emptyResponse_returnsEmptyList() = runTest {
        // given
        val fakeRepo = createFakeRepo(
            weekResponse = WeekImagesResponseDto("2026-02-17", "2026-02-23", emptyList())
        )
        val vm = HomeViewModel(fakeRepo)

        // when
        vm.loadThisWeek(userId = 1L)
        advanceUntilIdle()

        // then
        val state = vm.state.value
        assertTrue(state is HomeUiState.Success)
        assertTrue("빈 리스트여야 함", (state as HomeUiState.Success).items.isEmpty())
    }

    /**
     * 날짜 파싱이 제대로 되는지 검증 (yyyy.MM.dd 형식)
     */
    @Test
    fun loadThisWeek_parsesDateCorrectly() = runTest {
        // given
        val image = DayImagesDto.ImageSummaryDto(
            id = 1L,
            url = "http://test/1.jpg",
            originalName = "test.jpg",
            size = 100L,
            createdAt = "2026-03-02T12:30:45"
        )
        val fakeRepo = createFakeRepo(
            weekResponse = WeekImagesResponseDto(
                weekStart = "2026-02-24",
                weekEnd = "2026-03-02",
                days = listOf(DayImagesDto("MONDAY", "2026-03-02", listOf(image)))
            )
        )
        val vm = HomeViewModel(fakeRepo)

        // when
        vm.loadThisWeek(userId = 1L)
        advanceUntilIdle()

        // then
        val items = (vm.state.value as HomeUiState.Success).items
        assertEquals("날짜 형식이 yyyy.MM.dd", "2026.03.02", items[0].dateText)
    }

    /** 테스트용 Fake Repository 생성 헬퍼 함수 */
    private fun createFakeRepo(
        weekResponse: WeekImagesResponseDto
    ): ImageRepositoryContract = object : ImageRepositoryContract {
        override suspend fun upload(userId: Long, title: String?, content: String?, files: List<File>): UploadResultDto =
            throw NotImplementedError("테스트에서 사용하지 않음")
        override suspend fun loadThisWeek(userId: Long): WeekImagesResponseDto = weekResponse
        override suspend fun getPostDetail(imageId: Long): PostDetailDtos? = null
        override suspend fun updatePost(imageId: Long, title: String, content: String) {}
    }
}
