package com.flowpay.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowpay.app.data.local.entity.UserEntity
import com.flowpay.app.ui.theme.PaytmCyan
import com.flowpay.app.ui.theme.PaytmNavy
import com.flowpay.app.ui.theme.PendingAmber
import com.flowpay.app.ui.theme.SuccessGreen

@Composable
fun FlowPayTopBar(
    user: UserEntity?,
    isOfflineMode: Boolean,
    onToggleOffline: () -> Unit,
    notificationCount: Int,
    onOpenNotifications: () -> Unit,
    onOpenQrScanner: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Surface(
        color = PaytmNavy,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Row: Profile Avatar, Paytm/FlowPay Logo, Connection Mode, Scanner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar & Branding
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenProfile() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PaytmCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.name?.take(1)?.uppercase() ?: "P",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Flow",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Pay",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = PaytmCyan,
                                fontSize = 18.sp
                            )
                        }
                        Text(
                            text = "UPI • ${user?.name ?: "User"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Controls Right Side: Connection Mode Pill & Scanner Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Online/Offline Pill Toggle Simulator
                    val pillBgColor by animateColorAsState(
                        targetValue = if (isOfflineMode) PendingAmber.copy(alpha = 0.25f) else SuccessGreen.copy(alpha = 0.25f),
                        label = "pillBg"
                    )
                    val pillTextColor by animateColorAsState(
                        targetValue = if (isOfflineMode) PendingAmber else SuccessGreen,
                        label = "pillText"
                    )

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onToggleOffline() },
                        color = pillBgColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isOfflineMode) Icons.Default.SignalCellularOff else Icons.Default.SignalCellularAlt,
                                contentDescription = "Connection Mode",
                                tint = pillTextColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOfflineMode) "OFFLINE" else "ONLINE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = pillTextColor,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // QR Scanner Quick Button (Iconic Paytm Cyan Pill)
                    IconButton(
                        onClick = onOpenQrScanner,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PaytmCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Notification Bell Icon with Badge
                    BadgedBox(
                        badge = {
                            if (notificationCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = Color.White
                                ) {
                                    Text(text = if (notificationCount > 9) "9+" else notificationCount.toString())
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Paytm Search Bar Pill
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onOpenQrScanner() },
                color = Color.White.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Pay anyone on UPI or scan any QR code",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
