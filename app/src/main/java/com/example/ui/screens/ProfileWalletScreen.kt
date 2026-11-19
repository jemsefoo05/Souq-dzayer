package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EscrowStatus
import com.example.ui.theme.DzBaridiMobBlue
import com.example.ui.theme.DzBaridiMobYellow
import com.example.ui.theme.DzCibTeal
import com.example.ui.theme.DzGold
import com.example.ui.theme.DzGreenContainer
import com.example.ui.theme.DzGreenDark
import com.example.ui.theme.DzGreenLight
import com.example.ui.theme.DzGreenPrimary
import com.example.ui.viewmodel.MarketplaceViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileWalletScreen(
    viewModel: MarketplaceViewModel,
    onBack: () -> Unit,
    onOpenPaymentConfirmationDemo: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.allOrders.collectAsState()
    val favorites by viewModel.favoriteListings.collectAsState()

    val totalEscrowHeld = orders
        .filter { it.escrowStatus == EscrowStatus.HELD_IN_ESCROW }
        .sumOf { it.totalAmountDzd }

    val totalReleased = orders
        .filter { it.escrowStatus == EscrowStatus.RELEASED_TO_SELLER }
        .sumOf { it.totalAmountDzd }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Mon Compte & Portefeuille DZ", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User ID & Verified Badge
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(DzGreenPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("AB", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Amine Benali", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = DzGreenDark, modifier = Modifier.size(16.dp))
                            }
                            Text("0554 12 87 65 • Alger (16)", fontSize = 12.sp, color = Color.Gray)
                            Text("Membre Vérifié Daman DZ • ID #DZ-98214", fontSize = 11.sp, color = DzGreenDark, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Daman DZ Escrow Wallet Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(DzGreenDark, DzGreenPrimary, Color(0xFF00381B))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = DzGold, modifier = Modifier.size(20.dp))
                                Text("Portefeuille Sécurisé Daman DZ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text("ضمان DZ", color = DzGold, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }

                        Column {
                            Text("Fonds sous Séquestre (Garantis):", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text(
                                text = "${NumberFormat.getInstance(Locale.FRANCE).format(totalEscrowHeld)} DA",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp
                            )
                        }

                        Divider(color = Color.White.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Ventes Validées:", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text("${NumberFormat.getInstance(Locale.FRANCE).format(totalReleased)} DA", color = DzGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Commandes Traitées:", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text("${orders.size} Colis 58w", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Linked Payment Accounts (BaridiMob & Edahabia)
            item {
                Text("Moyens de Paiement Connectés", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // BaridiMob RIP
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DzBaridiMobBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("BM", color = DzBaridiMobYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Compte BaridiMob Algérie Poste", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("RIP: 00799999001234567890", fontSize = 11.sp, color = Color.Gray)
                            }
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = DzGreenDark, modifier = Modifier.size(18.dp))
                        }

                        Divider()

                        // Edahabia / CIB SATIM
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DzCibTeal),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Carte CIB / Edahabia SATIM", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("•••• •••• •••• 9012 (Exp: 12/28)", fontSize = 11.sp, color = Color.Gray)
                            }
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = DzGreenDark, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Integration Demo Card: Payment Confirmation Screen (BaridiMob & CIB/Edahabia)
            if (onOpenPaymentConfirmationDemo != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPaymentConfirmationDemo() }
                            .testTag("open_payment_confirmation_demo_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DzGreenLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DzGreenPrimary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DzGreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        "Écran Confirmation de Paiement",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = DzGreenDark
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(DzGold)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("NOUVEAU", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }
                                }
                                Text(
                                    "Aperçu du layout prêt pour intégration BaridiMob & CIB / SATIM",
                                    fontSize = 11.sp,
                                    color = DzGreenDark.copy(alpha = 0.8f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DzGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Guarantees & Algerian FAQs
            item {
                Text("Comment fonctionne SouqDZ ?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = DzGreenPrimary, modifier = Modifier.size(20.dp))
                            Column {
                                Text("1. Protection Escrow Daman DZ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("L'argent payé via BaridiMob ou CIB reste bloqué sur un compte séquestre neutre. Il n'est transféré au vendeur que lorsque vous validez votre colis.", fontSize = 11.sp, color = Color.DarkGray, lineHeight = 15.sp)
                            }
                        }

                        Divider()

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = DzGreenPrimary, modifier = Modifier.size(20.dp))
                            Column {
                                Text("2. Suivi de Colis Multi-Transporteurs (58 Wilayas)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Intégration directe des bordereaux Yalidine, ZR Express, et EMS Algérie Poste. Vous recevez un code PIN d'inspection à donner au livreur après ouverture.", fontSize = 11.sp, color = Color.DarkGray, lineHeight = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
