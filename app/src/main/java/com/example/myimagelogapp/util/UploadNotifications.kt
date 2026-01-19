package com.example.myimagelogapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.activity.contextaware.ContextAware
import androidx.core.app.NotificationCompat
import com.example.myimagelogapp.R

object UploadNotifications {
    const val CHANNEL_ID = "upload_channel"
    const val CHANNEL_NAME = "Uploads"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val ch = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(ch)
        }
    }

    fun buildProgress(
        context: Context,
        title: String,
        progress: Int,
        isIndeterminate: Boolean = false,
        contentText: String? = null
    ) = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(title.ifBlank { "Uploading..." })
        .setContentText(contentText ?: "progress $progress%")
        .setOnlyAlertOnce(true)
        .setOngoing(true)
        .setProgress(100, progress.coerceIn(0, 100), isIndeterminate)
        .build()
}