package com.example.myimagelogapp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.example.myimagelogapp.data.AppDatabase

class UploadQueueViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).uploadTaskDao()
    val tasks = dao.observeAll().asLiveData()
}
