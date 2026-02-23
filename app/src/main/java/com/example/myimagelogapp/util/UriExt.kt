package com.example.myimagelogapp.util

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import androidx.core.net.toUri

/**
 * Top-level 확장 함수 (기존클래스 확장 함수 클래스)
 */
fun String.toFile(context: Context): File? {
    return try {
        if (isBlank()) return null
        val uri = Uri.parse(this)

        val resolver = context.contentResolver
        resolver.openInputStream(uri)?.use { input ->
            val temp = File.createTempFile("upload_", ".jpg", context.cacheDir)
            FileOutputStream(temp).use { out -> input.copyTo(out) }
            Log.d("UriExt", "toFile OK uri=$uri -> ${temp.absolutePath} size=${temp.length()}")
            temp
        } ?: run {
            Log.e("UriExt", "openInputStream null uri=$uri")
            null
        }
    } catch (e: Exception) {
        Log.e("UriExt", "toFile EX uriString=$this", e)
        null
    }
}
