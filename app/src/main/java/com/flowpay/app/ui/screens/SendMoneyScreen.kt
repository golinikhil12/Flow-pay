package com.flowpay.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowpay.app.data.local.entity.UserEntity
import com.flowpay.app.ui.components.PinVerificationModal
import com.flowpay.app.ui.theme.PendingAmber
import com.flowpay.app.ui.theme.SuccessGreen

data class QuickContact(val name: String, val upiId: String, val bank: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMoneyScreen(
    currentUser: UserEntity?,
    isOfflineMode: Boolean,
    initialRecipientUpi: String,
    initialRecipientName: String,
    initialAmount: String,
    onExecutePayment: (isOffline: Boolean) -> Unit,
    onBack: () -> Unit,
    setRecipient: (upi: String, name: String) -> Unit,
    setAmount: (String) -> Unit,
    setNote: (String) -> Unit,
    noteText: String
) {
    var recipientInput by remember { mutableStateOf(initialRecipientUpi.ifEmpty { "rahul@flowpay" }) }
    var recipientNameInput by remember { mutableStateOf(initialRecipientName.ifEmpty { "Rahul" }) }
    var amountInput by remember { mutableStateOf(initialAmount) }
    var noteInput by remember { mutableStateOf(noteText) }
    var showPinModal by remember { mutableStateOf(false) }

    val quickContacts = listOf(
        QuickContact("Rahul", "rahul@flowpay", "HDFC Bank"),
        QuickContact("Nikhil", "nikhil@flowpay", "State Bank of India"),
        QuickContact("Starbucks Coffee", "starbucks@upi", "ICICI Bank"),
        QuickContact("Electricity Board", "electricity@bill", "Canara Bank")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Send Money", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Connection Mode Banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isOfflineMode) PendingAmber.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (isOfflineMode) PendingAmber else SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOfflineMode) "Offline Mode: Encrypted Token Settlement Active" else "Online Mode: Real-Time Instant UPI Settlement",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOfflineMode) PendingAmber else SuccessGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Contact Chips
                Text(
                    text = "Frequent & Recent Contacts",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(quickContacts) { contact ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (recipientInput == contact.upiId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                recipientInput = contact.upiId
                                recipientNameInput = contact.name
                                setRecipient(contact.upiId, contact.name)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = contact.name.take(1),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = contact.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Text(text = contact.upiId, style = MaterialTheme.typography.labelMedium, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Recipient UPI ID / Phone Input
                OutlinedTextField(
                    value = recipientInput,
                    onValueChange = {
                        recipientInput = it
                        recipientNameInput = it.substringBefore("@").replaceFirstChar { c -> c.uppercase() }
                        setRecipient(it, recipientNameInput)
                    },
                    label = { Text("Recipient UPI ID / Mobile Number") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Field
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = {
                        amountInput = it
                        setAmount(it)
                    },
                    label = { Text("Enter Amount (₹)") },
                    prefix = { Text("₹ ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Note Field
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = {
                        noteInput = it
                        setNote(it)
                    },
                    label = { Text("Add a Note (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Payment Source Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = currentUser?.bankName ?: "Linked Bank Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(text = "Available Balance: ₹%,.2f".format(currentUser?.walletBalance ?: 0.0), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Pay Button
            Button(
                onClick = {
                    setRecipient(recipientInput, recipientNameInput)
                    setAmount(amountInput)
                    setNote(noteInput)
                    showPinModal = true
                },
                enabled = amountInput.toDoubleOrNull() != null && amountInput.toDouble() > 0 && recipientInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOfflineMode) "PAY ₹${amountInput.ifEmpty { "0" }} (OFFLINE)" else "PROCEED TO PAY ₹${amountInput.ifEmpty { "0" }}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showPinModal) {
        PinVerificationModal(
            amount = amountInput.toDoubleOrNull() ?: 0.0,
            recipientName = recipientNameInput,
            onDismiss = { showPinModal = false },
            onPinSuccess = {
                showPinModal = false
                onExecutePayment(isOfflineMode)
            }
        )
    }
}
