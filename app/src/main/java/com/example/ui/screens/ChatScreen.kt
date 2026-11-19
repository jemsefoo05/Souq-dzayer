package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ListingEntity
import com.example.ui.components.ItemVisualGraphic
import com.example.ui.components.PriceTagDZD
import com.example.ui.theme.DzGoldContainer
import com.example.ui.theme.DzGoldOnContainer
import com.example.ui.theme.DzGreenContainer
import com.example.ui.theme.DzGreenDark
import com.example.ui.theme.DzGreenPrimary
import com.example.ui.theme.DzRedUrgent
import com.example.ui.viewmodel.MarketplaceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    listing: ListingEntity,
    viewModel: MarketplaceViewModel,
    onBack: () -> Unit,
    onBuyDirect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messagesFlow = remember(listing.id) { viewModel.getChatForListing(listing.id) }
    val messages by messagesFlow.collectAsState(initial = emptyList())

    var inputText by remember { mutableStateOf("") }
    var showOfferDialog by remember { mutableStateOf(false) }
    var offerAmountText by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DzGreenPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = listing.sellerName.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Column {
                            Text(listing.sellerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("En ligne • Répond rapidement", fontSize = 11.sp, color = DzGreenDark)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    Button(
                        onClick = onBuyDirect,
                        colors = ButtonDefaults.buttonColors(containerColor = DzGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp).testTag("chat_buy_button")
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Acheter Daman", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { showOfferDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(DzGoldContainer)
                                .testTag("chat_make_offer_button")
                        ) {
                            Icon(imageVector = Icons.Default.LocalOffer, contentDescription = "Faire une offre", tint = DzGoldOnContainer)
                        }

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Écrire un message en français / عربي...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_message_input"),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendChatMessage(
                                        listingId = listing.id,
                                        senderName = "Moi (Acheteur)",
                                        text = inputText
                                    )
                                    inputText = ""
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(DzGreenPrimary)
                                .testTag("chat_send_button")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer", tint = Color.White)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Item Header card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        ItemVisualGraphic(imageKey = listing.imageUrl, modifier = Modifier.fillMaxSize())
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(listing.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                        PriceTagDZD(priceDzd = listing.priceDzd, isNegotiable = listing.isNegotiable)
                    }
                }
            }

            // Message list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderId == "buyer"
                    val dateFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)
                    val timeStr = dateFormat.format(Date(msg.timestamp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        if (msg.isOffer && msg.offerAmountDzd != null) {
                            // Offer Card in Chat
                            Card(
                                modifier = Modifier
                                    .width(280.dp)
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMe) DzGreenContainer else DzGoldContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.LocalOffer, contentDescription = null, tint = DzGreenDark)
                                        Text("Proposition d'Offre:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Text(
                                        text = "${NumberFormat.getInstance(Locale.FRANCE).format(msg.offerAmountDzd)} DA",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = DzGreenDark
                                    )
                                    Text(msg.messageText, fontSize = 12.sp)

                                    if (msg.offerStatus == "PENDING" && !isMe) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { viewModel.respondToOffer(msg.id, false) },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Refuser", fontSize = 11.sp)
                                            }
                                            Button(
                                                onClick = { viewModel.respondToOffer(msg.id, true) },
                                                colors = ButtonDefaults.buttonColors(containerColor = DzGreenPrimary),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Accepter", fontSize = 11.sp)
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "Statut: ${msg.offerStatus}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = when (msg.offerStatus) {
                                                "ACCEPTED" -> DzGreenDark
                                                "DECLINED" -> DzRedUrgent
                                                else -> Color.Gray
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            // Standard message bubble
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isMe) 16.dp else 4.dp,
                                            bottomEnd = if (isMe) 4.dp else 16.dp
                                        )
                                    )
                                    .background(if (isMe) DzGreenPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = msg.messageText,
                                        color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = timeStr,
                                        color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                        fontSize = 9.sp,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Offer Dialog
    if (showOfferDialog) {
        AlertDialog(
            onDismissRequest = { showOfferDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.LocalOffer, contentDescription = null, tint = DzGreenPrimary)
                    Text("Proposer un Prix (Offre / سومة)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Prix initial affiché: ${NumberFormat.getInstance(Locale.FRANCE).format(listing.priceDzd)} DA", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = offerAmountText,
                        onValueChange = { offerAmountText = it },
                        label = { Text("Votre proposition en DA") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("chat_offer_amount_input")
                    )
                }
            },
            confirmButton = {
                val amount = offerAmountText.toLongOrNull()
                Button(
                    onClick = {
                        if (amount != null && amount > 0) {
                            viewModel.sendChatMessage(
                                listingId = listing.id,
                                senderName = "Moi (Acheteur)",
                                text = "Je vous propose $amount DA pour cet article.",
                                isOffer = true,
                                offerAmount = amount
                            )
                            showOfferDialog = false
                        }
                    },
                    enabled = amount != null && amount > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = DzGreenPrimary)
                ) {
                    Text("Envoyer l'Offre")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showOfferDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
