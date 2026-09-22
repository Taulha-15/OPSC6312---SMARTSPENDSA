package com.example.smartspendsa.network

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val language: String = "English"
)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val token: String?, val user: ApiUser?, val message: String? = null)
data class ApiUser(val id: String, val email: String, val displayName: String, val language: String)
data class TransactionDto(
    val clientId: String? = null,
    val userId: String? = null,
    val categoryName: String,
    val amount: Double,
    val type: String,
    val date: String,
    val notes: String
)
data class SyncRequest(val transactions: List<TransactionDto>)
data class SyncResponse(val synced: Int, val message: String)
data class CategoryDto(
    val id: String? = null,
    val categoryName: String,
    val budgetLimit: Double = 0.0,
    val categoryIcon: String = ""
)
