package com.flowpay.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.flowpay.app.ui.components.FlowPayBottomNav
import com.flowpay.app.ui.screens.*
import com.flowpay.app.ui.theme.FlowPayTheme
import com.flowpay.app.ui.viewmodel.*

class MainActivity : ComponentActivity() {

    private val mainViewModel by viewModels<MainViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()
    private val paymentViewModel by viewModels<PaymentViewModel>()
    private val historyViewModel by viewModels<HistoryViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isDarkMode by mainViewModel.isDarkMode.collectAsState()
            val isOfflineMode by mainViewModel.isOfflineMode.collectAsState()
            val currentUser by mainViewModel.currentUser.collectAsState()
            val notifications by mainViewModel.notifications.collectAsState()
            val pendingOfflineCount by mainViewModel.pendingOfflineCount.collectAsState()
            val toastMessage by mainViewModel.toastMessage.collectAsState()

            val demoUsers by authViewModel.allDemoUsers.collectAsState()
            val activeUserId by authViewModel.activeUserId.collectAsState()

            val paymentResult by paymentViewModel.paymentResult.collectAsState()

            val rawTransactions by historyViewModel.rawTransactions.collectAsState()
            val selectedFilter by historyViewModel.selectedFilter.collectAsState()
            val searchQuery by historyViewModel.searchQuery.collectAsState()

            val navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }

            // Observe toast messages
            LaunchedEffect(toastMessage) {
                toastMessage?.let { msg ->
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    mainViewModel.clearToast()
                }
            }

            // Observe payment results
            LaunchedEffect(paymentResult) {
                when (val res = paymentResult) {
                    is PaymentResult.Success -> {
                        navController.navigate("payment_status")
                    }
                    is PaymentResult.PendingOffline -> {
                        navController.navigate("payment_status")
                    }
                    is PaymentResult.Failed -> {
                        navController.navigate("payment_status")
                    }
                    else -> {}
                }
            }

            FlowPayTheme(darkTheme = isDarkMode) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "login"

                val bottomBarRoutes = listOf("home", "banking", "passbook", "history", "profile")
                val showBottomBar = currentRoute in bottomBarRoutes

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        if (showBottomBar) {
                            FlowPayBottomNav(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = "login"
                        ) {
                            // Login Screen
                            composable("login") {
                                LoginScreen(
                                    demoUsers = demoUsers,
                                    activeUserId = activeUserId,
                                    onSelectUser = { authViewModel.switchUser(it) },
                                    onVerifyPin = { authViewModel.verifyPin(it) },
                                    onLoginSuccess = {
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // Home Screen Dashboard
                            composable("home") {
                                HomeScreen(
                                    currentUser = currentUser,
                                    isOfflineMode = isOfflineMode,
                                    onToggleOffline = { mainViewModel.toggleOfflineMode() },
                                    pendingOfflineCount = pendingOfflineCount,
                                    onManualSync = { mainViewModel.manualSyncNow() },
                                    notificationCount = notifications.count { !it.isRead },
                                    recentTransactions = rawTransactions,
                                    onQuickAction = { actionId ->
                                        when (actionId) {
                                            "scan" -> navController.navigate("scan_qr")
                                            "send", "upi", "mobile", "bank", "self" -> navController.navigate("send_money")
                                            "receive" -> navController.navigate("receive_money")
                                            "balance" -> navController.navigate("banking")
                                        }
                                    },
                                    onOpenNotifications = { navController.navigate("notifications") },
                                    onOpenQrScanner = { navController.navigate("scan_qr") },
                                    onOpenProfile = { navController.navigate("profile") },
                                    onOpenHistory = { navController.navigate("history") },
                                    onOpenRechargeBills = { navController.navigate("recharge_bills") },
                                    onOpenRewards = { navController.navigate("rewards") }
                                )
                            }

                            // Send Money Screen
                            composable("send_money") {
                                val recUpi by paymentViewModel.recipientUpi.collectAsState()
                                val recName by paymentViewModel.recipientName.collectAsState()
                                val amt by paymentViewModel.amount.collectAsState()
                                val note by paymentViewModel.note.collectAsState()

                                SendMoneyScreen(
                                    currentUser = currentUser,
                                    isOfflineMode = isOfflineMode,
                                    initialRecipientUpi = recUpi,
                                    initialRecipientName = recName,
                                    initialAmount = amt,
                                    onExecutePayment = { offline -> paymentViewModel.executePayment(offline) },
                                    onBack = { navController.popBackStack() },
                                    setRecipient = { upi, name -> paymentViewModel.setRecipient(upi, name) },
                                    setAmount = { paymentViewModel.setAmount(it) },
                                    setNote = { paymentViewModel.setNote(it) },
                                    noteText = note
                                )
                            }

                            // Receive Money Screen
                            composable("receive_money") {
                                ReceiveMoneyScreen(
                                    currentUser = currentUser,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Scan QR Code Screen
                            composable("scan_qr") {
                                ScanQrScreen(
                                    onQrScanned = { payload ->
                                        if (paymentViewModel.parseQrCode(payload)) {
                                            navController.navigate("send_money")
                                        }
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Payment Status / Receipt Screen
                            composable("payment_status") {
                                val currentResult = paymentViewModel.paymentResult.collectAsState().value
                                val txn = when (currentResult) {
                                    is PaymentResult.Success -> currentResult.transaction
                                    is PaymentResult.PendingOffline -> currentResult.transaction
                                    else -> null
                                }
                                val errMsg = if (currentResult is PaymentResult.Failed) currentResult.reason else null

                                PaymentStatusScreen(
                                    transaction = txn,
                                    errorMessage = errMsg,
                                    onDone = {
                                        paymentViewModel.resetState()
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // Banking Screen
                            composable("banking") {
                                BankingScreen(
                                    currentUser = currentUser,
                                    recentTransactions = rawTransactions,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Passbook Screen
                            composable("passbook") {
                                PassbookScreen(
                                    currentUser = currentUser,
                                    transactions = rawTransactions,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // History Screen
                            composable("history") {
                                HistoryScreen(
                                    currentUser = currentUser,
                                    rawTransactions = rawTransactions,
                                    selectedFilter = selectedFilter,
                                    searchQuery = searchQuery,
                                    onFilterChanged = { historyViewModel.setFilter(it) },
                                    onQueryChanged = { historyViewModel.setSearchQuery(it) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Profile Screen
                            composable("profile") {
                                ProfileScreen(
                                    currentUser = currentUser,
                                    onSwitchUser = {
                                        val target = if (activeUserId == "user_1") "user_2" else "user_1"
                                        authViewModel.switchUser(target)
                                    },
                                    onResetData = { authViewModel.resetDemoData() },
                                    onOpenSettings = { navController.navigate("settings") },
                                    onLogout = {
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // Notifications Screen
                            composable("notifications") {
                                NotificationsScreen(
                                    notifications = notifications,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Settings Screen
                            composable("settings") {
                                SettingsScreen(
                                    isDarkMode = isDarkMode,
                                    isOfflineMode = isOfflineMode,
                                    onToggleDarkMode = { mainViewModel.toggleDarkMode() },
                                    onToggleOfflineMode = { mainViewModel.toggleOfflineMode() },
                                    onResetDemoData = { authViewModel.resetDemoData() },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Rewards Screen
                            composable("rewards") {
                                RewardsOffersScreen(onBack = { navController.popBackStack() })
                            }

                            // Recharge & Bills Screen
                            composable("recharge_bills") {
                                RechargeBillsScreen(
                                    onPayBill = { biller, amt ->
                                        paymentViewModel.setRecipient(biller, biller.substringBefore(" -"))
                                        paymentViewModel.setAmount(amt)
                                        navController.navigate("send_money")
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
