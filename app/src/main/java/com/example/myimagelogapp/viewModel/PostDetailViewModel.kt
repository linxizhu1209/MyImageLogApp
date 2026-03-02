package com.example.myimagelogapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myimagelogapp.data.repository.ImageRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


/**
 * 글 상세 데이터
 */
data class PostDetailData(
    val title: String?,
    val content: String?,
    val imageUrl: String?,
    val createdAt: String?,
    val updatedAt: String?
)

/**
 * 글 상세 화면용 ViewModel. 조회 및 수정 이벤트를 처리
 */
class PostDetailViewModel(
    private val repo: ImageRepository
): ViewModel() {

    private val _postData = MutableStateFlow<PostDetailData?>(null)
    val postData = _postData.asStateFlow()

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    fun loadPost(imageId: Long) {
        viewModelScope.launch {
            val dto = repo.getPostDetail(imageId)
            if (dto != null) {
                _postData.value = PostDetailData(
                    title = dto.title,
                    content = dto.content,
                    imageUrl = dto.imageUrl,
                    createdAt = dto.createdAt,
                    updatedAt = dto.updatedAt
                )
            }
        }
    }

    fun updatePost(imageId: Long, title: String, content: String) {
        viewModelScope.launch {
            runCatching {
                repo.updatePost(imageId, title, content)
            }.onSuccess {
                _events.emit(Event.Updated)
            }.onFailure { e ->
                _events.emit(Event.Error(e.message ?: "수정 실패"))
            }
        }
    }

    sealed interface Event {
        data object Updated : Event
        data class Error(val message: String) : Event
    }

    class Factory(private val repo: ImageRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PostDetailViewModel(repo) as T
    }
}