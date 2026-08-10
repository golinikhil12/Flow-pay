package com.flowpay.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flowpay.app.data.local.FlowPayDatabase
import com.flowpay.app.data.local.entity.TransactionEntity
import com.flowpay.app.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FlowPayDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()
    private val userDao = db.userDao()
    private val prefsRepo = UserPreferencesRepository(application)

    private val activeUserId: StateFlow<String> = prefsRepo.activeUserIdFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, "user_1"
    )

    private val _rawTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val rawTransactions: StateFlow<List<TransactionEntity>> = _rawTransactions.asStateFlow()

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            activeUserId.collectLatest { userId ->
                val user = userDao.getUserById(userId)
                if (user != null) {
                    transactionDao.getTransactionsForUserFlow(user.upiId).collectLatest { list ->
                        _rawTransactions.value = list
                    }
                }
            }
        }
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
