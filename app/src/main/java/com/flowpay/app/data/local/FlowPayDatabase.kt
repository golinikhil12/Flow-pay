package com.flowpay.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.flowpay.app.data.local.dao.NotificationDao
import com.flowpay.app.data.local.dao.TokenDao
import com.flowpay.app.data.local.dao.TransactionDao
import com.flowpay.app.data.local.dao.UserDao
import com.flowpay.app.data.local.entity.NotificationEntity
import com.flowpay.app.data.local.entity.TokenEntity
import com.flowpay.app.data.local.entity.TransactionEntity
import com.flowpay.app.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        TokenEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FlowPayDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun tokenDao(): TokenDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: FlowPayDatabase? = null

        fun getDatabase(context: Context): FlowPayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlowPayDatabase::class.java,
                    "flowpay_database"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(database)
                    }
                }
            }
        }

        suspend fun seedDatabase(database: FlowPayDatabase) {
            val userDao = database.userDao()
            val transactionDao = database.transactionDao()
            val notificationDao = database.notificationDao()

            if (userDao.getUserById("user_1") == null) {
                val user1 = UserEntity(
                    id = "user_1",
                    name = "Nikhil",
                    phone = "9876543210",
                    upiId = "nikhil@flowpay",
                    bankName = "State Bank of India",
                    accountNumber = "1234567890",
                    ifscCode = "SBIN0001234",
                    walletBalance = 50000.0,
                    offlineQuota = 5000.0,
                    pin = "1234",
                    kycStatus = "Verified",
                    email = "nikhil@example.com"
                )
                val user2 = UserEntity(
                    id = "user_2",
                    name = "Rahul",
                    phone = "9123456780",
                    upiId = "rahul@flowpay",
                    bankName = "HDFC Bank",
                    accountNumber = "9876543210",
                    ifscCode = "HDFC0005678",
                    walletBalance = 30000.0,
                    offlineQuota = 5000.0,
                    pin = "1234",
                    kycStatus = "Verified",
                    email = "rahul@example.com"
                )
                userDao.insertAll(listOf(user1, user2))

                // Seed welcome transactions
                val welcomeTxn = TransactionEntity(
                    txnId = "TXN_INIT_001",
                    senderUpi = "system@flowpay",
                    senderName = "FlowPay Welcome Bonus",
                    receiverUpi = "nikhil@flowpay",
                    receiverName = "Nikhil",
                    amount = 50000.0,
                    timestamp = System.currentTimeMillis() - 86400000L,
                    status = "COMPLETED",
                    mode = "ONLINE",
                    token = "WELCOME_TOKEN_NIKHIL",
                    paymentMethod = "State Bank of India",
                    note = "Initial Deposit",
                    category = "Transfer"
                )
                transactionDao.insertTransaction(welcomeTxn)

                notificationDao.insertNotification(
                    NotificationEntity(
                        userId = "user_1",
                        title = "Welcome to FlowPay!",
                        message = "Your offline UPI payment account is active. Balance: ₹50,000.",
                        type = "INFO"
                    )
                )
                notificationDao.insertNotification(
                    NotificationEntity(
                        userId = "user_2",
                        title = "Welcome to FlowPay!",
                        message = "Your offline UPI payment account is active. Balance: ₹30,000.",
                        type = "INFO"
                    )
                )
            }
        }
    }
}
