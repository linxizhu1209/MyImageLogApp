package com.example.myimagelogapp.data.remote

import android.content.Context
import com.example.myimagelogapp.R
import com.example.myimagelogapp.auth.AuthSession
import com.example.myimagelogapp.auth.SessionInvalidatingInterceptor
import com.example.myimagelogapp.data.AppPraiseApi
import com.example.myimagelogapp.data.StockReportApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitProvider {
    fun createImageApi(context: Context): ImageApi {
        val baseUrl = context.getString(R.string.base_url)

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(SessionInvalidatingInterceptor(context))
            .addInterceptor { chain ->
                val req = chain.request()
                val token = AuthSession.token(context)
                val newReq = if (!token.isNullOrBlank()) {
                    req.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else req
                chain.proceed(newReq)
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)   // 3분 (AI 요약 시간 고려)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(ImageApi::class.java)
    }

    fun createStockReportApi(context: Context): StockReportApi {
        val baseUrl = context.getString(R.string.base_url)

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(SessionInvalidatingInterceptor(context))
            .addInterceptor { chain ->
                val req = chain.request()
                val token = AuthSession.token(context)
                val newReq = if (!token.isNullOrBlank()) {
                    req.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else req
                chain.proceed(newReq)
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(StockReportApi::class.java)
    }

    /** 로그인 없이 호출 가능 (서버 permitAll) */
    fun createAppPraiseApi(context: Context): AppPraiseApi {
        val baseUrl = context.getString(R.string.base_url)
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit.create(AppPraiseApi::class.java)
    }
}