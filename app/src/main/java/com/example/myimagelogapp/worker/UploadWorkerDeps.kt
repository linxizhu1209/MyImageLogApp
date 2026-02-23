package com.example.myimagelogapp.worker

import android.content.Context
import com.example.myimagelogapp.data.remote.RetrofitProvider
import com.example.myimagelogapp.data.repository.ImageRepository
import com.example.myimagelogapp.viewModel.ImageRepositoryContract

object UploadWorkerDeps {
    var repoProvider: (Context) -> ImageRepositoryContract = { context ->
        ImageRepository(
            api = RetrofitProvider.createImageApi(context)
        )
    }
}