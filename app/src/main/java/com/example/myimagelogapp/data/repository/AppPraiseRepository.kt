package com.example.myimagelogapp.data.repository

import com.example.myimagelogapp.data.AppPraiseApi
import com.example.myimagelogapp.data.PraiseCreateRequest
import com.example.myimagelogapp.data.PraiseItemResponse
import com.example.myimagelogapp.data.PraiseListResponse

class AppPraiseRepository(
    private val api: AppPraiseApi
) {
    suspend fun list(page: Int = 0, size: Int = 20): PraiseListResponse =
        api.list(page, size)

    suspend fun create(nickname: String, content: String): PraiseItemResponse =
        api.create(PraiseCreateRequest(nickname.trim(), content.trim()))
}
