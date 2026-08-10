package com.flowpay.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.flowpay.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE upiId = :upiId OR phone = :phone LIMIT 1")
    suspend fun getUserByUpiOrPhone(upiId: String, phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE upiId = :upiId LIMIT 1")
    suspend fun getUserByUpi(upiId: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET walletBalance = walletBalance + :amount WHERE upiId = :upiId")
    suspend fun creditBalance(upiId: String, amount: Double)

    @Query("UPDATE users SET walletBalance = walletBalance - :amount WHERE upiId = :upiId")
    suspend fun debitBalance(upiId: String, amount: Double)
}
