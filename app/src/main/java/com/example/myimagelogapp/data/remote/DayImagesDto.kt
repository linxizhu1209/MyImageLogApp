package com.example.myimagelogapp.data.remote

data class DayImagesDto(
    val day: String,
    val date: String,
    val images: List<ImageSummaryDto>
) {
    data class ImageSummaryDto(
        val id: Long,
        val url: String,
        val originalName: String,
        val size: Long,
        val createdAt: String
    )
}
