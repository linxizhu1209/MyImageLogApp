package com.example.myimagelogapp.data.repository

import com.example.myimagelogapp.data.remote.ImageApi
import com.example.myimagelogapp.data.remote.UploadResultDto
import com.example.myimagelogapp.data.remote.WeekImagesResponseDto
import com.example.myimagelogapp.viewModel.ImageRepositoryContract
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ImageRepository(
    private val api: ImageApi
): ImageRepositoryContract {

    suspend override fun upload(
        userId: Long,
        files: List<File>
    ): UploadResultDto {
        val parts = files.map { file ->
            MultipartBody.Part.createFormData(
                        "files",
                        filename = file.name,
                        body = file.asRequestBody("image/*".toMediaType())
                    )
                }
        return api.uploadImages(userId, parts)
    }


    override suspend fun loadThisWeek(
        userId: Long
    ): WeekImagesResponseDto {
        return api.getThisWeek(userId)
    }
}