package com.example.myimagelogapp.data.remote

/**
 * 서버 ImageController.upload 응답 매핑
 */
data class UploadResultDto(
    val status: String,
    val items: List<UploadItemDto>
)

data class UploadItemDto(
    val id: Long,
    val url: String,
    val originalName: String,
    val size: Long
)