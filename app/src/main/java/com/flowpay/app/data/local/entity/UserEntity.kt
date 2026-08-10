package com.flowpay.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phone: String,
    val upiId: String,
    val bankName: String,
    val accountNumber: String,
    val ifscCode: String,
    val walletBalance: Double,
    val offlineQuota: Double = 5000.0,
    val pin: String = "1234",
    val kycStatus: String = "Verified",
    val email: String = ""
)
