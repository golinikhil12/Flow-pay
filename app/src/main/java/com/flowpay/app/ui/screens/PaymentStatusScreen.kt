package com.flowpay.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowpay.app.data.local.entity.TransactionEntity
import com.flowpay.app.ui.theme.PendingAmber
import com.flowpay.app.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PaymentStatusScreen(
    transaction: TransactionEntity?,
    errorMessage: String?,
    onDone: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val isPendingOffline = transaction?.status == "OFFLINE_PENDING"
    val isFailed = errorMessage != null || transaction?.status == "FAILED"

    val statusColor = when {
        isFailed -> MaterialTheme.colorScheme.error
        isPendingOffline -> PendingAmber
        else -> SuccessGreen
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                // Status Icon Circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isFailed -> Icons.Default.ErrorOutline
                            isPendingOffline -> Icons.Default.Sync
                            else -> Icons.Default.Check
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = when {
                        isFailed -> "Payment Failed"
                        isPendingOffline -> "Payment Saved Offline"
                        else -> "Money Sent Successfully!"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when {
                        isFailed -> errorMessage ?: "Transaction could not be processed."
                        isPendingOffline -> "Transaction signed & stored locally. Will auto-sync when online."
                        else -> "Transfer completed instantly via FlowPay UPI Engine."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (transaction != null) {
                    Text(
                        text = "₹%,.2f".format(transaction.amount),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Transaction Receipt Card
            if (transaction != null) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "TRANSACTION RECEIPT",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        ReceiptRow(label = "Transaction ID", value = transaction.txnId, onCopy = {
                            clipboardManager.setText(AnnotatedString(transaction.txnId))
                        })
                        ReceiptRow(label = "To Recipient", value = "${transaction.receiverName} (${transaction.receiverUpi})")
                        ReceiptRow(label = "From Sender", value = "${transaction.senderName} (${transaction.senderUpi})")
                        ReceiptRow(label = "Payment Source", value = transaction.paymentMethod)
                        ReceiptRow(label = "Mode", value = transaction.mode)
                        ReceiptRow(label = "Date & Time", value = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(transaction.timestamp)))
                        ReceiptRow(label = "Security Token", value = transaction.token.take(16) + "...", onCopy = {
                            clipboardManager.setText(AnnotatedString(transaction.token))
                        })
                    }
                }
            }

            // Bottom Action
            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Done", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
            if (onCopy != null) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onCopy, modifier = Modifier.size(16.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}
