package com.example.smartspendsa.network

import retrofit2.http.*

interface SmartSpendApi {
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("api/v1/transactions")
    suspend fun getTransactions(): List<TransactionDto>

    @POST("api/v1/transactions/sync")
    suspend fun syncTransactions(@Body request: SyncRequest): SyncResponse

    @GET("api/v1/categories")
    suspend fun getCategories(): List<CategoryDto>
}
