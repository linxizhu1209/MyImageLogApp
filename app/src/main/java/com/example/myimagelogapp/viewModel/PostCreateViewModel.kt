package com.example.myimagelogapp.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PostCreateViewModel: ViewModel() {

    private val _photos = MutableLiveData<List<String>>(emptyList())
    val photos: LiveData<List<String>> = _photos

    fun addPhotos(newOnes: List<String>, max: Int = 10) {
        val current = _photos.value.orEmpty()
        _photos.value = (current + newOnes).distinct().take(max)
    }

    fun removePhoto(uriString: String) {
        _photos.value = _photos.value.orEmpty().filterNot { it == uriString }
    }
}