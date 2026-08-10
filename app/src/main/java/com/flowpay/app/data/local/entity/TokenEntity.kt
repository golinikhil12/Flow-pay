package com.flowpay.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "used_tokens")
data class TokenEntity(
    @PrimaryKey
    val tokenHash: String,
    val txnId: String,
    val senderUpi: String,
    val receiverUpi: String,
    val amount: Double,
    val createdAt: Long,
    val isUsed: Boolean = true
)
