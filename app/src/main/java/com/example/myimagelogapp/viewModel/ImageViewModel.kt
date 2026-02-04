package com.example.myimagelogapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myimagelogapp.data.remote.UploadResultDto
import com.example.myimagelogapp.data.remote.WeekImagesResponseDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException
import java.io.File

class ImageViewModel(
    private val repo: ImageRepositoryContract
) : ViewModel() {

    private val _state = MutableStateFlow<ImageUiState>(ImageUiState.Idle)
    val state: StateFlow<ImageUiState> = _state.asStateFlow()

    fun uploadImages(userId: Long, files: List<File>) {
        viewModelScope.launch {
            _state.value = ImageUiState.Loading
            try {
                val res = repo.upload(userId, files)
                _state.value = ImageUiState.UploadSuccess(res)
            } catch (e: HttpException) {
                _state.value = ImageUiState.Error(
                    message = "서버 오류가 발생했습니다.",
                    code = e.code()
                )
            } catch (e: IOException) {
                _state.value = ImageUiState.Error(
                    message = "네트워크 연결을 확인해주세요."
                )
            } catch (e: Exception) {
                _state.value = ImageUiState.Error(
                    message = "알 수 없는 오류가 발생했습니다."
                )
            }
        }
    }

    fun loadThisWeek(userId: Long) {
        viewModelScope.launch {
            _state.value  = ImageUiState.Loading
            try {
                val res = repo.loadThisWeek(userId)
                _state.value = ImageUiState.WeekLoadSuccess(res)
            } catch (e: HttpException) {
                _state.value = ImageUiState.Error(
                    message = "서버 오류가 발생했습니다.",
                    code = e.code()
                )
            } catch (e: IOException) {
                _state.value = ImageUiState.Error(
                    message = "네트워크 연결을 확인해주세요."
                )
            } catch (e: Exception) {
                _state.value = ImageUiState.Error(
                    message = "알 수 없는 오류가 발생했습니다."
                )
            }
        }
    }
}

interface ImageRepositoryContract {
    suspend fun upload(userId: Long, files: List<File>): UploadResultDto
    suspend fun loadThisWeek(userId: Long): WeekImagesResponseDto
}