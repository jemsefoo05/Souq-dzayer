package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.OrderEntity
import com.example.data.model.DeliveryStatus
import com.example.data.model.EscrowStatus
import com.example.data.model.TrackingCheckpoint
import com.example.ui.theme.DzGold
import com.example.ui.theme.DzGreenContainer
import com.example.ui.theme.DzGreenDark
import com.example.ui.theme.DzGreenPrimary
import com.example.ui.theme.DzRedUrgent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeliveryTrackerView(
    order: OrderEntity,
    checkpoints: List<TrackingCheckpoint>,
    onAdvanceDelivery: () -> Unit,
    onReleaseEscrow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("order_tracking_card_${order.orderId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Carrier & Tracking Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DzGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = DzGreenDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = order.carrier.displayName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${order.serviceType.labelFr} (${order.serviceType.labelAr})",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Escrow status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (order.escrowStatus) {
                                EscrowStatus.HELD_IN_ESCROW -> DzGreenContainer
                                EscrowStatus.RELEASED_TO_SELLER -> DzGreenDark
                                else -> Color.LightGray
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.escrowStatus.labelFr,
                        color = if (order.escrowStatus == EscrowStatus.RELEASED_TO_SELLER) Color.White else DzGreenDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Tracking Barcode / Code Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("N° de Suivi Colis (Tracking ID):", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = order.trackingNumber,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = DzGreenPrimary,
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(order.trackingNumber))
                            copied = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (copied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                            contentDescription = "Copier",
                            tint = if (copied) DzGreenPrimary else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Delivery Route Preview (Origin Wilaya -> Destination Wilaya)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DzGreenContainer.copy(alpha = 0.3f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Expéditeur", fontSize = 10.sp, color = Color.Gray)
                    Text(order.sellerName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = DzGreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text("Destinataire", fontSize = 10.sp, color = Color.Gray)
                    Text("${order.buyerWilayaName} (${order.buyerCommune})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Secret Courier PIN Code for Buyer inspection
            if (order.deliveryStatus != DeliveryStatus.DELIVERED_INSPECTED) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, DzGold, RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFFBEA))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DzGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Code Secret d'Inspection Colis: ${order.pinSecurityCode}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color(0xFF5D4037)
                            )
                            Text(
                                text = "Communiquez ce code au livreur UNIQUEMENT après avoir ouvert et vérifié l'article.",
                                fontSize = 11.sp,
                                color = Color(0xFF795548),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            // Step Progress Timeline
            Text(
                text = "Historique d'Acheminement (En Temps Réel):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                checkpoints.forEachIndexed { index, checkpoint ->
                    val isLast = index == checkpoints.size - 1
                    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.FRANCE)
                    val dateString = dateFormat.format(Date(checkpoint.timestamp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Timeline Indicator Dot & Line
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isLast) DzGreenPrimary else DzGreenContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isLast) Icons.Default.LocalShipping else Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (isLast) Color.White else DzGreenDark,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        // Content
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = checkpoint.status.labelFr,
                                    fontWeight = if (isLast) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = if (isLast) DzGreenPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = dateString,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = checkpoint.location,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            if (checkpoint.description.isNotBlank()) {
                                Text(
                                    text = checkpoint.description,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons (Simulate next delivery progress / Confirm receipt)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (order.deliveryStatus != DeliveryStatus.DELIVERED_INSPECTED) {
                    OutlinedButton(
                        onClick = onAdvanceDelivery,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("advance_delivery_button_${order.orderId}")
                    ) {
                        Icon(imageVector = Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Avancer Étape", fontSize = 12.sp)
                    }
                }

                if (order.escrowStatus == EscrowStatus.HELD_IN_ESCROW) {
                    Button(
                        onClick = onReleaseEscrow,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_delivery_escrow_${order.orderId}"),
                        colors = ButtonDefaults.buttonColors(containerColor = DzGreenPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Valider Réception", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
