package com.example.myimagelogapp.data.remote

/**
 * 개별 뉴스 항목
 */
data class StockNewsDto(
    val id: Long,
    val title: String,
    val summary: String?,
    val sourceUrl: String?,
    val source: String?
)

/**
 * 오늘의 뉴스 응답
 */
data class TodayNewsResponseDto(
    val date: String,
    val news: List<StockNewsDto>
)