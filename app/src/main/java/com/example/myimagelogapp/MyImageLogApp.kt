package com.example.myimagelogapp

import android.app.Application
import android.util.Log

/**
 * 앱 전역에서 마지막 크래시 스택을 저장하는 Application 클래스.
 */
class MyImageLogApp : Application() {

    /**
     * 처리되지 않은 예외를 저장해 다음 실행에서 원인 파악이 가능하게 한다.
     */
    override fun onCreate() {
        super.onCreate()

        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            runCatching {
                val stack = Log.getStackTraceString(e)
                getSharedPreferences(PREF, MODE_PRIVATE)
                    .edit()
                    .putString(KEY_LAST_CRASH, stack)
                    .apply()
                Log.e("CrashCapture", stack)
            }
            prev?.uncaughtException(t, e)
        }
    }

    companion object {
        private const val PREF = "crash_capture"
        private const val KEY_LAST_CRASH = "last_crash"

        fun consumeLastCrash(app: Application): String? {
            val sp = app.getSharedPreferences(PREF, MODE_PRIVATE)
            val v = sp.getString(KEY_LAST_CRASH, null)
            if (!v.isNullOrBlank()) {
                sp.edit().remove(KEY_LAST_CRASH).apply()
            }
            return v
        }
    }
}

