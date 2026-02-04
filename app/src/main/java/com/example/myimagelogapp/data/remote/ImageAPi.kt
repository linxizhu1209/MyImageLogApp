package com.example.myimagelogapp.data.remote

import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ImageAPi {

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
}