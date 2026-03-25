package com.example.myimagelogapp.auth

import android.content.Context

object AuthSession {
    private const val PREF = "auth_pref"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ID = "user_id"

    fun save(context: Context, token: String, userId: Long) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_USER_ID, userId)
            .apply()
    }

    fun token(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)

    fun userId(context: Context): Long =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getLong(KEY_USER_ID, -1L)

    fun isLoggedIn(context: Context): Boolean =
        !token(context).isNullOrBlank() && userId(context) > 0L

    fun clear(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}