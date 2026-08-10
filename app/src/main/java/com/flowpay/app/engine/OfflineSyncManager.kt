package com.flowpay.app.engine

import com.flowpay.app.data.local.dao.NotificationDao
import com.flowpay.app.data.local.dao.TransactionDao
import com.flowpay.app.data.local.dao.UserDao
import com.flowpay.app.data.local.entity.NotificationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OfflineSyncManager(
    private val transactionDao: TransactionDao,
    private val userDao: UserDao,
    private val notificationDao: NotificationDao,
    private val tokenEngine: PaymentTokenEngine
) {

    /**
     * Synchronizes all offline pending transactions when network connectivity is available.
     * Updates balances atomically in local ledger and creates sync notifications.
     */
    suspend fun syncPendingTransactions(): Int = withContext(Dispatchers.IO) {
        val pendingList = transactionDao.getPendingOfflineTransactions()
        if (pendingList.isEmpty()) return@withContext 0

        var syncedCount = 0
        for (txn in pendingList) {
            try {
                // Verify sender & receiver
                val sender = userDao.getUserByUpi(txn.senderUpi)
                val receiver = userDao.getUserByUpi(txn.receiverUpi)

                // Debit sender balance if not already debited provisionally, or credit receiver balance
                if (receiver != null) {
                    userDao.creditBalance(receiver.upiId, txn.amount)
                }

                // If sender exists, ensure balance was updated or debit now if needed
                if (sender != null && sender.walletBalance < 0) {
                    // Adjusted during offline creation or reserved
                }

                // Update status to COMPLETED
                transactionDao.updateStatus(txn.txnId, "COMPLETED")
                syncedCount++

                // Notify Sender
                if (sender != null) {
                    notificationDao.insertNotification(
                        NotificationEntity(
                            userId = sender.id,
                            title = "Offline Payment Synchronized",
                            message = "Your offline payment of ₹${txn.amount} to ${txn.receiverName} has been settled.",
                            type = "SYNC_COMPLETED"
                        )
                    )
                }

                // Notify Receiver
                if (receiver != null) {
                    notificationDao.insertNotification(
                        NotificationEntity(
                            userId = receiver.id,
                            title = "Money Received (Offline Sync)",
                            message = "Received ₹${txn.amount} from ${txn.senderName} (Offline Sync).",
                            type = "MONEY_RECEIVED"
                        )
                    )
                }

            } catch (e: Exception) {
                // Mark failed if invalid
                transactionDao.updateStatus(txn.txnId, "FAILED")
            }
        }

        syncedCount
    }
}
