package com.example.smartspendsa.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?
    @Insert suspend fun insert(user: User): Long
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY categoryName")
    fun observeAll(): Flow<List<Category>>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Category>)
    @Update suspend fun update(category: Category)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY transactionId DESC")
    fun observeForUser(userId: Int): Flow<List<TransactionEntity>>
    @Insert suspend fun insert(transaction: TransactionEntity): Long
    @Update suspend fun update(transaction: TransactionEntity)
    @Delete suspend fun delete(transaction: TransactionEntity)
    @Query("SELECT * FROM transactions WHERE userId = :userId AND syncStatus = 0")
    suspend fun unsynced(userId: Int): List<TransactionEntity>
    @Query("UPDATE transactions SET syncStatus = 1 WHERE transactionId IN (:ids)")
    suspend fun markSynced(ids: List<Int>)
}

@Database(
    entities = [User::class, Category::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SmartSpendDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
}
