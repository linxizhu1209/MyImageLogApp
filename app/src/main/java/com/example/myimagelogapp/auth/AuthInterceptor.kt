package com.example.myimagelogapp.auth

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 저장된 토큰이 있으면 Authorization 헤더를 자동으로 붙이는 OkHttp 인터셉터.
 */
class AuthInterceptor(
    private val context: Context
) : Interceptor {

    /**
     * 요청 시 토큰이 존재하면 Bearer 토큰 헤더를 추가한다.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = AuthSession.token(context)
        val original = chain.request()

        if (token.isNullOrBlank()) {
            return chain.proceed(original)
        }

        val req = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(req)
    }
}

