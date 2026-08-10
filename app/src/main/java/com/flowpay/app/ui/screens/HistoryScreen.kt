package com.flowpay.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowpay.app.data.local.entity.TransactionEntity
import com.flowpay.app.data.local.entity.UserEntity
import com.flowpay.app.ui.components.TransactionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    currentUser: UserEntity?,
    rawTransactions: List<TransactionEntity>,
    selectedFilter: String,
    searchQuery: String,
    onFilterChanged: (String) -> Unit,
    onQueryChanged: (String) -> Unit,
    onBack: () -> Unit
) {
    val userUpi = currentUser?.upiId ?: ""

    val filteredTransactions = rawTransactions.filter { txn ->
        val isReceived = txn.receiverUpi.equals(userUpi, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "OFFLINE_PENDING" -> txn.status == "OFFLINE_PENDING"
            "COMPLETED" -> txn.status == "COMPLETED"
            "SENT" -> !isReceived
            "RECEIVED" -> isReceived
            else -> true
        }

        val query = searchQuery.trim().lowercase()
        val matchesQuery = if (query.isEmpty()) true else {
            txn.receiverName.lowercase().contains(query) ||
            txn.senderName.lowercase().contains(query) ||
            txn.txnId.lowercase().contains(query) ||
            txn.amount.toString().contains(query)
        }

        matchesFilter && matchesQuery
    }

    val filterOptions = listOf(
        "ALL" to "All",
        "OFFLINE_PENDING" to "Offline Pending",
        "COMPLETED" to "Completed",
        "SENT" to "Sent",
        "RECEIVED" to "Received"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction History", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChanged,
                placeholder = { Text("Search by name, UPI ID, or Txn ID") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips Row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filterOptions) { (key, label) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChanged(key) },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // History List
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "No matching transactions found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTransactions) { txn ->
                        TransactionItem(
                            transaction = txn,
                            currentUpiId = userUpi,
                            onClick = {}
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}
