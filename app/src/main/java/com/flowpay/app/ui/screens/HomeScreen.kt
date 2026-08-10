package com.flowpay.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowpay.app.data.local.entity.TransactionEntity
import com.flowpay.app.data.local.entity.UserEntity
import com.flowpay.app.ui.components.BalanceCard
import com.flowpay.app.ui.components.FlowPayTopBar
import com.flowpay.app.ui.components.QuickActionGrid
import com.flowpay.app.ui.components.TransactionItem
import com.flowpay.app.ui.theme.PaytmCyan
import com.flowpay.app.ui.theme.PaytmGold
import com.flowpay.app.ui.theme.PendingAmber
import com.flowpay.app.ui.theme.SuccessGreen

@Composable
fun HomeScreen(
    currentUser: UserEntity?,
    isOfflineMode: Boolean,
    onToggleOffline: () -> Unit,
    pendingOfflineCount: Int,
    onManualSync: () -> Unit,
    notificationCount: Int,
    recentTransactions: List<TransactionEntity>,
    onQuickAction: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenQrScanner: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenRechargeBills: () -> Unit,
    onOpenRewards: () -> Unit
) {
    Scaffold(
        topBar = {
            FlowPayTopBar(
                user = currentUser,
                isOfflineMode = isOfflineMode,
                onToggleOffline = onToggleOffline,
                notificationCount = notificationCount,
                onOpenNotifications = onOpenNotifications,
                onOpenQrScanner = onOpenQrScanner,
                onOpenProfile = onOpenProfile
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Balance & Bank Overview Card
            item {
                BalanceCard(
                    user = currentUser,
                    pendingOfflineCount = pendingOfflineCount,
                    onManualSync = onManualSync
                )
            }

            // Offline Mode Banner Alert
            if (isOfflineMode) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = PendingAmber.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "⚡ Secure Offline Payment Mode Active",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PendingAmber
                                )
                                Text(
                                    text = "Payments work offline via cryptographic tokens & auto-sync when online.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions Grid (Paytm UPI Money Transfer & Services)
            item {
                QuickActionGrid(onActionSelected = onQuickAction)
            }

            // Paytm Soundbox Simulator Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(PaytmCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Soundbox",
                                    tint = PaytmCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "FlowPay Soundbox Voice Alerts",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Instant audio settlement alerts active",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SuccessGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelMedium,
                                color = SuccessGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Recharges & Utility Bills Section
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recharge & Bill Payments",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextTextButton(onClick = onOpenRechargeBills) {
                                Text("View All", style = MaterialTheme.typography.labelMedium, color = PaytmCyan)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            UtilityPill("Mobile", Icons.Default.PhoneAndroid, Color(0xFF00BAF2), onOpenRechargeBills)
                            UtilityPill("Electricity", Icons.Default.ElectricBolt, Color(0xFFFFB400), onOpenRechargeBills)
                            UtilityPill("Water", Icons.Default.WaterDrop, Color(0xFF06B6D4), onOpenRechargeBills)
                            UtilityPill("DTH", Icons.Default.Receipt, Color(0xFF8B5CF6), onOpenRechargeBills)
                        }
                    }
                }
            }

            // Rewards & Cashback Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onOpenRewards() },
                    color = PaytmGold.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(PaytmGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = "Rewards",
                                    tint = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Paytm Cashback & Scratch Cards",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Win up to ₹100 cashback on offline payments!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Recent Transactions Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextTextButton(onClick = onOpenHistory) {
                        Text("View History", style = MaterialTheme.typography.labelMedium, color = PaytmCyan)
                    }
                }
            }

            // Recent Transactions List
            if (recentTransactions.isEmpty()) {
                item {
                    Text(
                        text = "No recent transactions found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(recentTransactions.take(5)) { txn ->
                    TransactionItem(
                        transaction = txn,
                        currentUpiId = currentUser?.upiId ?: "",
                        onClick = onOpenHistory
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TextTextButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    TextButton(onClick = onClick) { content() }
}

@Composable
private fun RowScope.UtilityPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, style = MaterialTheme.typography.labelMedium, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
