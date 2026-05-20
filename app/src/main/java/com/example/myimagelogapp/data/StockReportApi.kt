package com.example.myimagelogapp.data

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT

interface StockReportApi {
    @PUT("api/stock-report-subscription/me")
    suspend fun upsert(
        @Body body: StockReportUpsertRequest
    ): StockReportSubscriptionResponse

    @GET("api/stock-report-subscriptions/me")
    suspend fun getMine(): StockReportSubscriptionResponse

    @DELETE("api/stock-report-subscriptions/me")
    suspend fun disable()
}

data class StockReportUpsertRequest(
    val email: String,
    val symbols: List<String>,
    val sendHour: Int? = 14,
    val enabled: Boolean? = true
)

data class StockReportSubscriptionResponse(
    val id: Long,
    val email: String,
    val symbols: List<String>,
    val sendHour: Int,
    val enabled: Boolean
)