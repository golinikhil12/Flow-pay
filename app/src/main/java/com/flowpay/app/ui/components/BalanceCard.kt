package com.flowpay.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowpay.app.data.local.entity.UserEntity
import com.flowpay.app.ui.theme.CardGradientEnd
import com.flowpay.app.ui.theme.CardGradientStart
import com.flowpay.app.ui.theme.PaytmCyan
import com.flowpay.app.ui.theme.PaytmNavy

@Composable
fun BalanceCard(
    user: UserEntity?,
    pendingOfflineCount: Int,
    onManualSync: () -> Unit
) {
    var isBalanceVisible by remember { mutableStateOf(true) }
    val clipboardManager = LocalClipboardManager.current
    var showCopiedToast by remember { mutableStateOf(false) }

    val gradient = Brush.linearGradient(
        colors = listOf(CardGradientStart, CardGradientEnd)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(20.dp)
        ) {
            Column {
                // Header Row: Paytm Bank Branding & Account Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Bank",
                            tint = PaytmCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${user?.bankName ?: "Linked Bank Account"}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = PaytmCyan,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "PRIMARY UPI",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Balance Title & Eye Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isBalanceVisible = !isBalanceVisible }
                ) {
                    Text(
                        text = "Total Available Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Balance",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Amount Display
                Text(
                    text = if (isBalanceVisible) "₹%,.2f".format(user?.walletBalance ?: 0.0) else "••••••••",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Row: UPI ID & Offline Sync indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            user?.upiId?.let {
                                clipboardManager.setText(AnnotatedString(it))
                                showCopiedToast = true
                            }
                        }
                    ) {
                        Text(
                            text = "UPI ID: ${user?.upiId ?: "nikhil@flowpay"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy UPI",
                            tint = PaytmCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    if (pendingOfflineCount > 0) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onManualSync() },
                            color = Color(0xFFFFB400)
                        ) {
                            Text(
                                text = "$pendingOfflineCount Sync Pending",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    } else {
                        Text(
                            text = "Offline Quota: ₹5,000",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
