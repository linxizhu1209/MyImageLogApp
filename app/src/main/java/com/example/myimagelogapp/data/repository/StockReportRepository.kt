package com.example.myimagelogapp.data.repository

import android.content.Context
import com.example.myimagelogapp.auth.AuthSession
import com.example.myimagelogapp.data.StockReportApi
import com.example.myimagelogapp.data.StockReportSubscriptionResponse
import com.example.myimagelogapp.data.StockReportUpsertRequest
import retrofit2.HttpException

class StockReportRepository(
    private val api: StockReportApi,
    private val context: Context
) {
    suspend fun subscribe(email: String, symbols: List<String>) {
        requireLoggedIn()
        api.upsert(StockReportUpsertRequest(email = email, symbols = symbols))
    }

    suspend fun getSubscription(): StockReportSubscriptionResponse? {
        if (!AuthSession.isLoggedIn(context)) return null
        return try {
            api.getMine()
        } catch (e: HttpException) {
            if (e.code() == 404) null else throw e
        }
    }

    suspend fun disable() {
        requireLoggedIn()
        api.disable()
    }

    private fun requireLoggedIn() {
        if (!AuthSession.isLoggedIn(context)) {
            throw IllegalStateException("로그인이 필요합니다.")

        }
    }
}