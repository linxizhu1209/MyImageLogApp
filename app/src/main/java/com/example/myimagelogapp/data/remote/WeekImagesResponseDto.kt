package com.example.myimagelogapp.data.remote

data class WeekImagesResponseDto(
    val weekStart: String,
    val weekEnd: String,
    val days: List<DayImagesDto>
)