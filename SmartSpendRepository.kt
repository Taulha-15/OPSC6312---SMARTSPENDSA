package com.example.smartspendsa.repository

import android.content.Context
import com.example.smartspendsa.data.*
import com.example.smartspendsa.network.*
import kotlinx.coroutines.flow.Flow
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDate
import java.util.UUID

class SmartSpendRepository(
    private val context: Context,
    private val db: SmartSpendDatabase
) {
    private val users = db.userDao()
    private val categories = db.categoryDao()
    private val transactions = db.transactionDao()

    suspend fun seedCategories() {
        categories.insertAll(
            listOf(
                Category(categoryName = "Food", categoryIcon = "🍔"),
                Category(categoryName = "Transport", categoryIcon = "🚗"),
                Category(categoryName = "Entertainment", categoryIcon = "🎬"),
                Category(categoryName = "Bills", categoryIcon = "🧾"),
                Category(categoryName = "Salary", categoryIcon = "💰"),
                Category(categoryName = "Other", categoryIcon = "📦")
            )
        )
    }

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        language: String
    ): Result<User> {
        if (users.findByEmail(email) != null) return Result.failure(Exception("An account with this email already exists."))
        val hash = BCrypt.hashpw(password, BCrypt.gensalt(12))
        val id = users.insert(User(email = email, passwordHash = hash, displayName = displayName, language = language))
        val user = User(id.toInt(), email, hash, displayName, language)
        // Try the online API, but keep the local account if the API is offline.
        runCatching {
            val response = ApiClient.api.register(RegisterRequest(email, password, displayName, language))
            ApiClient.setToken(response.token)
        }
        return Result.success(user)
    }

    suspend fun login(email: String, password: String): Result<User> {
        // Try the server first so that online users receive a JWT token.
        try {
            val response = ApiClient.api.login(LoginRequest(email, password))
            val remote = response.user
            if (remote != null && response.token != null) {
                ApiClient.setToken(response.token)
                val local = users.findByEmail(email)
                if (local != null) {
                    return Result.success(local)
                }
                val hash = BCrypt.hashpw(password, BCrypt.gensalt(12))
                val id = users.insert(
                    User(
                        email = remote.email,
                        passwordHash = hash,
                        displayName = remote.displayName,
                        language = remote.language
                    )
                )
                return Result.success(User(id.toInt(), remote.email, hash, remote.displayName, remote.language))
            }
        } catch (_: Exception) {
            // If the API is unavailable, continue with the local RoomDB login.
        }

        val local = users.findByEmail(email)
        return if (local != null && BCrypt.checkpw(password, local.passwordHash)) {
            Result.success(local)
        } else {
            Result.failure(Exception("Invalid email or password."))
        }
    }

    fun observeCategories() = categories.observeAll()
    fun observeTransactions(userId: Int): Flow<List<TransactionEntity>> = transactions.observeForUser(userId)

    suspend fun addTransaction(
        userId: Int, categoryId: Int, categoryName: String,
        amount: Double, type: String, date: String, notes: String
    ) {
        transactions.insert(
            TransactionEntity(
                userId = userId, categoryId = categoryId, categoryName = categoryName,
                amount = amount, type = type, date = date, notes = notes, syncStatus = false
            )
        )
    }

    suspend fun sync(userId: Int): Result<Int> {
        return try {
            val pending = transactions.unsynced(userId)
            if (pending.isEmpty()) return Result.success(0)
            val request = SyncRequest(
                pending.map {
                    TransactionDto(
                        clientId = UUID.randomUUID().toString(),
                        userId = userId.toString(),
                        categoryName = it.categoryName,
                        amount = it.amount,
                        type = it.type,
                        date = it.date,
                        notes = it.notes
                    )
                }
            )
            val result = ApiClient.api.syncTransactions(request)
            transactions.markSynced(pending.map { it.transactionId })
            Result.success(result.synced)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setBudget(category: Category, budget: Double) {
        categories.update(category.copy(budgetLimit = budget))
    }
}
