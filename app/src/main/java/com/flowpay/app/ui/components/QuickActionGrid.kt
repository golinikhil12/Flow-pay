package com.flowpay.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowpay.app.ui.theme.PaytmCyan

data class FlowPayActionItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun QuickActionGrid(
    onActionSelected: (String) -> Unit
) {
    val upiTransferActions = listOf(
        FlowPayActionItem("mobile", "To Mobile / Contact", Icons.Default.Call, Color(0xFF00BAF2)),
        FlowPayActionItem("send", "To UPI ID / Bank", Icons.Default.Send, Color(0xFF00B970)),
        FlowPayActionItem("self", "To Self A/c", Icons.Default.SwapHoriz, Color(0xFF8B5CF6)),
        FlowPayActionItem("balance", "Check Balance", Icons.Default.AccountBalanceWallet, Color(0xFFFFB400))
    )

    val quickServiceActions = listOf(
        FlowPayActionItem("scan", "Scan Any QR", Icons.Default.QrCodeScanner, Color(0xFF00BAF2)),
        FlowPayActionItem("receive", "Receive Money", Icons.Default.QrCode2, Color(0xFF10B981)),
        FlowPayActionItem("bank", "FlowPay Bank", Icons.Default.AccountBalance, Color(0xFF3B82F6)),
        FlowPayActionItem("history", "Passbook", Icons.Default.History, Color(0xFFEC4899))
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UPI Money Transfer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "FlowPay UPI",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = PaytmCyan,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Row 1: UPI Transfers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                upiTransferActions.forEach { item ->
                    ActionItemCell(item = item, onClick = { onActionSelected(item.id) })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row 2: Services
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                quickServiceActions.forEach { item ->
                    ActionItemCell(item = item, onClick = { onActionSelected(item.id) })
                }
            }
        }
    }
}

@Composable
private fun RowScope.ActionItemCell(
    item: FlowPayActionItem,
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
                .size(46.dp)
                .clip(CircleShape)
                .background(item.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = item.color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
