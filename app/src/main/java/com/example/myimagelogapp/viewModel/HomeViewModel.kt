package com.example.myimagelogapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myimagelogapp.data.remote.WeekImagesResponseDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

data class HomeImageUiItem(
    val id: Long,
    val url: String,
    val dateText: String,
    val sortKey: Long
)

sealed interface HomeUiState {
    data object Idle : HomeUiState
    data object Loading : HomeUiState
    data class Success(val items: List<HomeImageUiItem>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val repo: ImageRepositoryContract
) : ViewModel() {
    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun loadThisWeek(userId: Long) {
        viewModelScope.launch {
            _state.value = HomeUiState.Loading
            runCatching {
                repo.loadThisWeek(userId)
            }.onSuccess { res ->
                val items = res.toHomeUiItemSorted()
                _state.value = HomeUiState.Success(items)
            }.onFailure { e ->
                _state.value = HomeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * 서버 WeekImagesresponseDto -> HomeImageUiItem 변환
     */
    private fun WeekImagesResponseDto.toHomeUiItemSorted(): List<HomeImageUiItem> {
        val out = mutableListOf<HomeImageUiItem>()
        for (day in days) {
            for (img in day.images) {
                val (dateText, sortKey) = parseCreatedAt(img.createdAt)
                out += HomeImageUiItem(
                    id = img.id,
                    url = img.url,
                    dateText = dateText,
                    sortKey = sortKey
                )
            }
        }
        return out.sortedByDescending { it.sortKey }
    }

    private fun parseCreatedAt(createdAt: String): Pair<String, Long> {
        val outFmt = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        return try {
            val odt = OffsetDateTime.parse(createdAt)
            val txt = odt.toLocalDate().format(outFmt)
            txt to odt.toInstant().toEpochMilli()
        } catch (_: Exception) {
            try {
                val ldt = LocalDateTime.parse(createdAt)
                val txt = ldt.toLocalDate().format(outFmt)
                txt to ldt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {
                // 파싱 실패: 날짜 텍스트는 대충 앞부분만
                val safe = createdAt.take(10).replace('-', '.')
                safe to 0L
            }
        }
    }
}
