package com.flowpay.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val txnId: String,
    val senderUpi: String,
    val senderName: String,
    val receiverUpi: String,
    val receiverName: String,
    val amount: Double,
    val timestamp: Long,
    val status: String, // "COMPLETED", "OFFLINE_PENDING", "FAILED"
    val mode: String,   // "ONLINE", "OFFLINE"
    val token: String,
    val paymentMethod: String = "Linked Bank Account",
    val note: String = "FlowPay Transfer",
    val category: String = "Transfer"
)
