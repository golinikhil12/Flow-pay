package com.flowpay.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.flowpay.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE senderUpi = :upiId OR receiverUpi = :upiId ORDER BY timestamp DESC")
    fun getTransactionsForUserFlow(upiId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE senderUpi = :upiId OR receiverUpi = :upiId ORDER BY timestamp DESC")
    suspend fun getTransactionsForUser(upiId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE status = 'OFFLINE_PENDING' ORDER BY timestamp ASC")
    suspend fun getPendingOfflineTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE status = 'OFFLINE_PENDING' ORDER BY timestamp ASC")
    fun getPendingOfflineTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE txnId = :txnId LIMIT 1")
    suspend fun getTransactionById(txnId: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET status = :newStatus WHERE txnId = :txnId")
    suspend fun updateStatus(txnId: String, newStatus: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
