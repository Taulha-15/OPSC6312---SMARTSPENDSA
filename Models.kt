package com.example.smartspendsa.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val email: String,
    val passwordHash: String,
    val displayName: String,
    val language: String = "English"
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val categoryId: Int = 0,
    val categoryName: String,
    val budgetLimit: Double = 0.0,
    val categoryIcon: String = ""
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val transactionId: Int = 0,
    val userId: Int,
    val categoryId: Int,
    val categoryName: String,
    val amount: Double,
    val type: String,
    val date: String,
    val notes: String,
    val syncStatus: Boolean = false
)
