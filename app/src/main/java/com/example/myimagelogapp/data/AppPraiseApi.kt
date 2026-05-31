package com.example.myimagelogapp.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AppPraiseApi {
    @GET("api/app-praises")
    suspend fun list(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PraiseListResponse

    @POST("api/app-praises")
    suspend fun create(@Body body: PraiseCreateRequest): PraiseItemResponse
}

data class PraiseCreateRequest(
    val nickname: String,
    val content: String
)

data class PraiseItemResponse(
    val id: Long,
    val nickname: String,
    val content: String,
    val createdAt: String
)

data class PraiseListResponse(
    val items: List<PraiseItemResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val hasNext: Boolean
)
