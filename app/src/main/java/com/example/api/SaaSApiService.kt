package com.example.api

import retrofit2.http.*

data class LoginRequest(val phone: String, val pin: String)
data class AuthResponse(
    val token: String,
    val userId: Int,
    val name: String,
    val role: String, // "CUSTOMER", "PROVIDER", "ADMIN"
    val balanceIqd: Double
)

data class SyncPositionRequest(
    val providerId: Int,
    val latOffset: Float,
    val lngOffset: Float,
    val isAvailable: Boolean
)

data class SaasStatsResponse(
    val totalBookings: Int,
    val totalRevenueIqd: Double,
    val totalCommissionIqd: Double,
    val activeDriversCount: Int,
    val systemStatus: String
)

interface SaaSApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/register-provider")
    suspend fun registerProvider(
        @Header("Authorization") token: String,
        @Query("name") name: String,
        @Query("phone") phone: String,
        @Query("serviceType") serviceType: String,
        @Query("vehiclePlate") vehiclePlate: String,
        @Query("city") city: String
    ): AuthResponse

    @POST("api/drivers/sync-position")
    suspend fun syncPosition(
        @Header("Authorization") token: String,
        @Body request: SyncPositionRequest
    ): Map<String, Any>

    @GET("api/saas/stats")
    suspend fun getSaasStats(
        @Header("Authorization") token: String
    ): SaasStatsResponse

    @POST("api/payments/record-payout")
    suspend fun recordPayout(
        @Header("Authorization") token: String,
        @Query("providerId") providerId: Int,
        @Query("amountIqd") amountIqd: Double,
        @Query("method") method: String // "ZAIN_CASH" or "ASIA_PAY"
    ): Map<String, Any>
}
