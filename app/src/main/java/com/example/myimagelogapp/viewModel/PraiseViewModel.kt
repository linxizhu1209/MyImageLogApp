package com.example.myimagelogapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myimagelogapp.data.PraiseItemResponse
import com.example.myimagelogapp.data.repository.AppPraiseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PraiseUiItem(
    val id: Long,
    val nickname: String,
    val content: String,
    val dateText: String
)

sealed interface PraiseListUiState {
    data object Idle : PraiseListUiState
    data object Loading : PraiseListUiState
    data class Success(
        val items: List<PraiseUiItem>,
        val hasNext: Boolean,
        val isLoadingMore: Boolean
    ) : PraiseListUiState
    data class Error(val message: String) : PraiseListUiState
}

sealed interface PraiseSubmitUiState {
    data object Idle : PraiseSubmitUiState
    data object Submitting : PraiseSubmitUiState
    data object Success : PraiseSubmitUiState
    data class Error(val message: String) : PraiseSubmitUiState
}

class PraiseViewModel(
    private val repo: AppPraiseRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<PraiseListUiState>(PraiseListUiState.Idle)
    val listState: StateFlow<PraiseListUiState> = _listState.asStateFlow()

    private val _submitState = MutableStateFlow<PraiseSubmitUiState>(PraiseSubmitUiState.Idle)
    val submitState: StateFlow<PraiseSubmitUiState> = _submitState.asStateFlow()

    private val items = mutableListOf<PraiseUiItem>()
    private var currentPage = 0
    private var hasNext = false
    private var isLoading = false

    fun refresh() {
        currentPage = 0
        items.clear()
        loadPage(reset = true)
    }

    fun loadMore() {
        if (!hasNext || isLoading) return
        currentPage++
        loadPage(reset = false)
    }

    private fun loadPage(reset: Boolean) {
        if (isLoading) return
        isLoading = true
        if (reset) {
            _listState.value = PraiseListUiState.Loading
        } else {
            val current = _listState.value
            if (current is PraiseListUiState.Success) {
                _listState.value = current.copy(isLoadingMore = true)
            }
        }
        viewModelScope.launch {
            runCatching {
                repo.list(page = currentPage, size = 20)
            }.onSuccess { res ->
                hasNext = res.hasNext
                val mapped = res.items.map { it.toUiItem() }
                if (reset) items.clear()
                items.addAll(mapped)
                _listState.value = PraiseListUiState.Success(
                    items = items.toList(),
                    hasNext = hasNext,
                    isLoadingMore = false
                )
            }.onFailure { e ->
                if (reset) {
                    _listState.value = PraiseListUiState.Error(e.message ?: "목록을 불러오지 못했습니다.")
                } else {
                    currentPage--
                    val current = _listState.value
                    if (current is PraiseListUiState.Success) {
                        _listState.value = current.copy(isLoadingMore = false)
                    }
                }
            }
            isLoading = false
        }
    }

    fun submit(nickname: String, content: String) {
        _submitState.value = PraiseSubmitUiState.Submitting
        viewModelScope.launch {
            runCatching {
                repo.create(nickname, content)
            }.onSuccess { created ->
                items.add(0, created.toUiItem())
                _listState.value = PraiseListUiState.Success(
                    items = items.toList(),
                    hasNext = hasNext,
                    isLoadingMore = false
                )
                _submitState.value = PraiseSubmitUiState.Success
            }.onFailure { e ->
                _submitState.value = PraiseSubmitUiState.Error(
                    e.message ?: "등록에 실패했습니다."
                )
            }
        }
    }

    fun resetSubmitState() {
        _submitState.value = PraiseSubmitUiState.Idle
    }

    private fun PraiseItemResponse.toUiItem(): PraiseUiItem =
        PraiseUiItem(
            id = id,
            nickname = nickname,
            content = content,
            dateText = formatCreatedAt(createdAt)
        )

    private fun formatCreatedAt(iso: String): String {
        return try {
            val ldt = java.time.LocalDateTime.parse(iso.substringBefore("."))
            ldt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
        } catch (_: Exception) {
            iso.take(16).replace('T', ' ')
        }
    }
}
