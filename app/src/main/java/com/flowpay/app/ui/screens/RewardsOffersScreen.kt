package com.flowpay.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowpay.app.ui.theme.SuccessGreen

data class ScratchCardItem(val id: Int, var isScratched: Boolean = false, val rewardText: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsOffersScreen(onBack: () -> Unit) {
    val cards = remember {
        mutableStateListOf(
            ScratchCardItem(1, false, "₹50 Cashback"),
            ScratchCardItem(2, false, "₹100 Cashback"),
            ScratchCardItem(3, true, "₹25 Cashback"),
            ScratchCardItem(4, true, "₹10 Cashback")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rewards & Scratch Cards", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("TOTAL REWARDS WON", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("₹185.00", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    Text("Cashback directly credited to your primary bank account.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Your Scratch Cards", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cards) { card ->
                    Surface(
                        modifier = Modifier
                            .height(130.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                val idx = cards.indexOf(card)
                                if (idx != -1) cards[idx] = card.copy(isScratched = true)
                            },
                        color = if (card.isScratched) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
                            if (card.isScratched) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(card.rewardText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                    Text("Won!", style = MaterialTheme.typography.labelMedium, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Tap to Scratch", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
