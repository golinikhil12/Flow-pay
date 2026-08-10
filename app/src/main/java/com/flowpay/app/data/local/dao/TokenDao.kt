package com.flowpay.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flowpay.app.data.local.entity.TokenEntity

@Dao
interface TokenDao {
    @Query("SELECT * FROM used_tokens WHERE tokenHash = :tokenHash LIMIT 1")
    suspend fun getToken(tokenHash: String): TokenEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertToken(token: TokenEntity)

    @Query("DELETE FROM used_tokens")
    suspend fun deleteAll()
}
