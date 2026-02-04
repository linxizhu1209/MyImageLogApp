package com.example.myimagelogapp.viewModel

import com.example.myimagelogapp.data.remote.UploadResultDto
import com.example.myimagelogapp.data.remote.WeekImagesResponseDto

sealed class ImageUiState {
    data object Idle : ImageUiState()
    data object Loading : ImageUiState()

    data class UploadSuccess(val result: UploadResultDto) : ImageUiState()
    data class WeekLoadSuccess(val result: WeekImagesResponseDto) : ImageUiState()

    data class Error(
        val message: String,
        val code: Int? = null
    ) : ImageUiState()
}