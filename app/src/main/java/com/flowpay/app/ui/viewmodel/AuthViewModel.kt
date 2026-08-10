package com.flowpay.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flowpay.app.data.local.FlowPayDatabase
import com.flowpay.app.data.local.entity.UserEntity
import com.flowpay.app.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FlowPayDatabase.getDatabase(application)
    private val userDao = db.userDao()
    private val prefsRepo = UserPreferencesRepository(application)

    val activeUserId: StateFlow<String> = prefsRepo.activeUserIdFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, "user_1"
    )

    private val _allDemoUsers = MutableStateFlow<List<UserEntity>>(emptyList())
    val allDemoUsers: StateFlow<List<UserEntity>> = _allDemoUsers.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    init {
        viewModelScope.launch {
            userDao.getAllUsersFlow().collect { users ->
                _allDemoUsers.value = users
            }
        }
    }

    fun switchUser(userId: String) {
        viewModelScope.launch {
            prefsRepo.setActiveUserId(userId)
        }
    }

    fun verifyPin(enteredPin: String): Boolean {
        if (enteredPin == "1234") {
            _loginError.value = null
            return true
        } else {
            _loginError.value = "Invalid PIN. Default demo PIN is 1234"
            return false
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            db.transactionDao().deleteAll()
            db.tokenDao().deleteAll()
            db.notificationDao().deleteAll()
            FlowPayDatabase.seedDatabase(db)
        }
    }

    fun clearError() {
        _loginError.value = null
    }
}
