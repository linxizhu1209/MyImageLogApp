package com.example.myimagelogapp.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ImageApi {

    @Multipart
    @POST("api/images/upload")
    suspend fun uploadImages(
        @Query("userId") userId: Long,
        @Part files: List<MultipartBody.Part>
    ): UploadResultDto

    @GET("api/images/week")
    suspend fun getThisWeek(
        @Query("userId") userId: Long
    ): WeekImagesResponseDto

    /**
     * 이미지 id로 해당 글 조회.
     */
    @GET("api/posts/{id}")
    suspend fun getPostDetail(@Path("id") imageId: Long): PostDetailDtos?

    /**
     * 해당 이미지 글 수정.
     */
    @PUT("api/posts/{id}")
    suspend fun updatePost(
        @Path("id") imageId: Long,
        @Body body: UpdatePostRequest
    ): Unit
}