package com.flowpay.app.engine

import com.flowpay.app.data.local.dao.TokenDao
import com.flowpay.app.data.local.entity.TokenEntity
import java.security.MessageDigest
import java.util.UUID

class PaymentTokenEngine(private val tokenDao: TokenDao) {

    /**
     * Generates an encrypted SHA-256 token representation for a payment transaction.
     */
    fun generateToken(
        senderUpi: String,
        receiverUpi: String,
        amount: Double,
        timestamp: Long,
        nonce: String = UUID.randomUUID().toString()
    ): String {
        val rawInput = "$senderUpi|$receiverUpi|$amount|$timestamp|$nonce"
        val bytes = MessageDigest.getInstance("SHA-256").digest(rawInput.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Validates if token has already been used. If not, records it as used.
     * Throws IllegalStateException if duplicate request is detected.
     */
    suspend fun validateAndRegisterToken(
        tokenHash: String,
        txnId: String,
        senderUpi: String,
        receiverUpi: String,
        amount: Double
    ): Boolean {
        val existingToken = tokenDao.getToken(tokenHash)
        if (existingToken != null) {
            throw IllegalStateException("Duplicate payment detected! Security Token $tokenHash has already been processed.")
        }
        val newToken = TokenEntity(
            tokenHash = tokenHash,
            txnId = txnId,
            senderUpi = senderUpi,
            receiverUpi = receiverUpi,
            amount = amount,
            createdAt = System.currentTimeMillis(),
            isUsed = true
        )
        tokenDao.insertToken(newToken)
        return true
    }

    suspend fun isTokenUsed(tokenHash: String): Boolean {
        return tokenDao.getToken(tokenHash) != null
    }
}
