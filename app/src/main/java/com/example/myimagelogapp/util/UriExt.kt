package com.example.myimagelogapp.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import androidx.core.net.toUri

/**
 * Top-level 확장 함수 (기존클래스 확장 함수 클래스)
 */
fun String.toFile(context: Context): File? {
    val uri = this.toUri()
    val input = context.contentResolver.openInputStream(uri) ?: return null

    val temp = File.createTempFile("upload_", "jpg", context.cacheDir)
    FileOutputStream(temp).use { out ->
        input.copyTo(out)
    }
    return temp
}
