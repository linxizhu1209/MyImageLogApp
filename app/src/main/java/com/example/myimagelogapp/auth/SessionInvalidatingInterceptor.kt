package com.example.myimagelogapp.auth

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 401/403 응답 시 저장된 로그인 세션을 제거한다.
 */
class SessionInvalidatingInterceptor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401 || response.code == 403) {
            AuthSession.clear(context)
        }
        return response
    }
}
