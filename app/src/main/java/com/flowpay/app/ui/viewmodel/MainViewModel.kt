package com.flowpay.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flowpay.app.data.local.FlowPayDatabase
import com.flowpay.app.data.local.entity.NotificationEntity
import com.flowpay.app.data.local.entity.UserEntity
import com.flowpay.app.data.repository.UserPreferencesRepository
import com.flowpay.app.engine.OfflineSyncManager
import com.flowpay.app.engine.PaymentTokenEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FlowPayDatabase.getDatabase(application)
    private val userDao = db.userDao()
    private val transactionDao = db.transactionDao()
    private val notificationDao = db.notificationDao()
    private val tokenDao = db.tokenDao()

    val prefsRepo = UserPreferencesRepository(application)
    val tokenEngine = PaymentTokenEngine(tokenDao)
    val syncManager = OfflineSyncManager(transactionDao, userDao, notificationDao, tokenEngine)

    val activeUserId: StateFlow<String> = prefsRepo.activeUserIdFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, "user_1"
    )

    val isDarkMode: StateFlow<Boolean> = prefsRepo.isDarkModeFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, true
    )

    val isOfflineMode: StateFlow<Boolean> = prefsRepo.isOfflineModeFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = _notifications.asStateFlow()

    private val _pendingOfflineCount = MutableStateFlow(0)
    val pendingOfflineCount: StateFlow<Int> = _pendingOfflineCount.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        // Observe active user changes
        viewModelScope.launch {
            activeUserId.collectLatest { userId ->
                userDao.getUserByIdFlow(userId).collectLatest { user ->
                    _currentUser.value = user
                }
            }
        }

        // Observe notifications
        viewModelScope.launch {
            activeUserId.collectLatest { userId ->
                notificationDao.getNotificationsFlow(userId).collectLatest { list ->
                    _notifications.value = list
                }
            }
        }

        // Observe pending offline transactions
        viewModelScope.launch {
            transactionDao.getPendingOfflineTransactionsFlow().collectLatest { list ->
                _pendingOfflineCount.value = list.size
            }
        }

        // Observe offline mode toggle to trigger automatic synchronization when returning online
        viewModelScope.launch {
            var wasOffline = false
            isOfflineMode.collectLatest { offline ->
                if (wasOffline && !offline) {
                    // Restored online connection -> trigger auto sync!
                    val count = syncManager.syncPendingTransactions()
                    if (count > 0) {
                        _toastMessage.value = "Synced $count pending offline payment(s) successfully!"
                    }
                }
                wasOffline = offline
            }
        }
    }

    fun toggleOfflineMode() {
        viewModelScope.launch {
            val nextState = !isOfflineMode.value
            prefsRepo.setOfflineMode(nextState)
            if (nextState) {
                _toastMessage.value = "Offline Mode Enabled - Payments will be saved locally"
            } else {
                _toastMessage.value = "Online Mode Restored - Auto-synchronizing transactions..."
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            prefsRepo.setDarkMode(!isDarkMode.value)
        }
    }

    fun manualSyncNow() {
        viewModelScope.launch {
            val count = syncManager.syncPendingTransactions()
            if (count > 0) {
                _toastMessage.value = "Successfully synchronized $count offline payment(s)!"
            } else {
                _toastMessage.value = "No pending offline payments to sync."
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
