package com.flowpay.app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flowpay.app.data.local.FlowPayDatabase
import com.flowpay.app.data.local.entity.NotificationEntity
import com.flowpay.app.data.local.entity.TransactionEntity
import com.flowpay.app.data.local.entity.UserEntity
import com.flowpay.app.data.repository.UserPreferencesRepository
import com.flowpay.app.engine.PaymentTokenEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

sealed class PaymentResult {
    object Idle : PaymentResult()
    data class Success(val transaction: TransactionEntity) : PaymentResult()
    data class PendingOffline(val transaction: TransactionEntity) : PaymentResult()
    data class Failed(val reason: String) : PaymentResult()
}

class PaymentViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FlowPayDatabase.getDatabase(application)
    private val userDao = db.userDao()
    private val transactionDao = db.transactionDao()
    private val notificationDao = db.notificationDao()
    private val tokenDao = db.tokenDao()

    val tokenEngine = PaymentTokenEngine(tokenDao)
    private val prefsRepo = UserPreferencesRepository(application)

    private val _recipientUpi = MutableStateFlow("")
    val recipientUpi: StateFlow<String> = _recipientUpi.asStateFlow()

    private val _recipientName = MutableStateFlow("")
    val recipientName: StateFlow<String> = _recipientName.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _note = MutableStateFlow("FlowPay Transfer")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _paymentResult = MutableStateFlow<PaymentResult>(PaymentResult.Idle)
    val paymentResult: StateFlow<PaymentResult> = _paymentResult.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun setRecipient(upi: String, name: String) {
        _recipientUpi.value = upi
        _recipientName.value = name
    }

    fun setAmount(value: String) {
        _amount.value = value
    }

    fun setNote(value: String) {
        _note.value = value
    }

    /**
     * Parses standard UPI URI string (e.g. upi://pay?pa=rahul@flowpay&pn=Rahul&am=500)
     */
    fun parseQrCode(qrData: String): Boolean {
        return try {
            if (qrData.startsWith("upi://pay")) {
                val uri = Uri.parse(qrData)
                val pa = uri.getQueryParameter("pa") ?: ""
                val pn = uri.getQueryParameter("pn") ?: pa.substringBefore("@")
                val am = uri.getQueryParameter("am") ?: ""
                if (pa.isNotEmpty()) {
                    _recipientUpi.value = pa
                    _recipientName.value = if (pn.isNotEmpty()) pn else pa
                    if (am.isNotEmpty()) _amount.value = am
                    return true
                }
            } else if (qrData.contains("@")) {
                _recipientUpi.value = qrData.trim()
                _recipientName.value = qrData.substringBefore("@").replaceFirstChar { it.uppercase() }
                return true
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    fun executePayment(isOfflineMode: Boolean) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val activeId = prefsRepo.activeUserIdFlow.first()
                val sender = userDao.getUserById(activeId)
                if (sender == null) {
                    _paymentResult.value = PaymentResult.Failed("Sender user profile not found.")
                    _isProcessing.value = false
                    return@launch
                }

                val amt = _amount.value.toDoubleOrNull()
                if (amt == null || amt <= 0) {
                    _paymentResult.value = PaymentResult.Failed("Please enter a valid amount.")
                    _isProcessing.value = false
                    return@launch
                }

                val targetUpi = _recipientUpi.value.ifBlank { "rahul@flowpay" }
                val targetName = _recipientName.value.ifBlank { "Rahul" }

                // Check wallet / bank balance limit
                if (sender.walletBalance < amt) {
                    _paymentResult.value = PaymentResult.Failed("Insufficient balance. Available: ₹${sender.walletBalance}")
                    _isProcessing.value = false
                    return@launch
                }

                val timestamp = System.currentTimeMillis()
                val txnId = "FP" + (10000000..99999999).random()

                // Generate Unique Security Token
                val tokenHash = tokenEngine.generateToken(sender.upiId, targetUpi, amt, timestamp)

                // Enforce Duplicate Token Prevention
                tokenEngine.validateAndRegisterToken(
                    tokenHash = tokenHash,
                    txnId = txnId,
                    senderUpi = sender.upiId,
                    receiverUpi = targetUpi,
                    amount = amt
                )

                if (isOfflineMode) {
                    // --- OFFLINE PAYMENT ENGINE ---
                    // Enforce offline quota limit check
                    if (amt > sender.offlineQuota) {
                        _paymentResult.value = PaymentResult.Failed("Transaction exceeds offline quota limit of ₹${sender.offlineQuota}")
                        _isProcessing.value = false
                        return@launch
                    }

                    val offlineTxn = TransactionEntity(
                        txnId = txnId,
                        senderUpi = sender.upiId,
                        senderName = sender.name,
                        receiverUpi = targetUpi,
                        receiverName = targetName,
                        amount = amt,
                        timestamp = timestamp,
                        status = "OFFLINE_PENDING",
                        mode = "OFFLINE",
                        token = tokenHash,
                        paymentMethod = "${sender.bankName} (Offline Sync)",
                        note = _note.value
                    )
                    transactionDao.insertTransaction(offlineTxn)

                    // Debit sender provisionally
                    userDao.debitBalance(sender.upiId, amt)

                    // Store offline notification
                    notificationDao.insertNotification(
                        NotificationEntity(
                            userId = sender.id,
                            title = "Offline Payment Queued",
                            message = "Sent ₹$amt to $targetName (Saved locally. Auto-sync when online).",
                            type = "OFFLINE_SAVED"
                        )
                    )

                    _paymentResult.value = PaymentResult.PendingOffline(offlineTxn)

                } else {
                    // --- ONLINE PAYMENT INSTANT SETTLEMENT ---
                    val onlineTxn = TransactionEntity(
                        txnId = txnId,
                        senderUpi = sender.upiId,
                        senderName = sender.name,
                        receiverUpi = targetUpi,
                        receiverName = targetName,
                        amount = amt,
                        timestamp = timestamp,
                        status = "COMPLETED",
                        mode = "ONLINE",
                        token = tokenHash,
                        paymentMethod = sender.bankName,
                        note = _note.value
                    )
                    transactionDao.insertTransaction(onlineTxn)

                    // Debit sender, Credit receiver
                    userDao.debitBalance(sender.upiId, amt)
                    userDao.creditBalance(targetUpi, amt)

                    // Insert Notifications
                    notificationDao.insertNotification(
                        NotificationEntity(
                            userId = sender.id,
                            title = "Payment Successful",
                            message = "Sent ₹$amt to $targetName instantly via UPI.",
                            type = "MONEY_SENT"
                        )
                    )

                    val receiverUser = userDao.getUserByUpi(targetUpi)
                    if (receiverUser != null) {
                        notificationDao.insertNotification(
                            NotificationEntity(
                                userId = receiverUser.id,
                                title = "Money Received",
                                message = "Received ₹$amt from ${sender.name}.",
                                type = "MONEY_RECEIVED"
                            )
                        )
                    }

                    _paymentResult.value = PaymentResult.Success(onlineTxn)
                }

            } catch (e: Exception) {
                _paymentResult.value = PaymentResult.Failed(e.message ?: "Transaction failed.")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun resetState() {
        _paymentResult.value = PaymentResult.Idle
        _amount.value = ""
        _recipientUpi.value = ""
        _recipientName.value = ""
    }
}
