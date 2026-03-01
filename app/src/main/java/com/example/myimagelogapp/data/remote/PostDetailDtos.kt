package com.example.myimagelogapp.data.remote

/**
 * 서버에서 한 건의 글 (이미지+글+제목) 조회 시 사용하는 DTO
 */
data class PostDetailDtos(
    val id: Long,
    val title: String?,
    val content: String?,
    val imageUrl: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class UpdatePostRequest(
    val title: String,
    val content: String
)