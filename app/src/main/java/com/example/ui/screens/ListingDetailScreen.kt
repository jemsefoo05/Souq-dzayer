package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ListingEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.model.DeliveryCarrier
import com.example.data.model.DeliveryServiceType
import com.example.data.model.PaymentType
import com.example.data.model.Wilaya
import com.example.data.model.WilayaData
import com.example.ui.components.AlgerianWilayaBottomSheet
import com.example.ui.components.BaridiMobPaymentDialog
import com.example.ui.components.CibSatimPaymentDialog
import com.example.ui.components.EscrowBadge
import com.example.ui.components.ItemVisualGraphic
import com.example.ui.components.PaymentMethodSelector
import com.example.ui.components.PriceTagDZD
import com.example.ui.theme.DzGold
import com.example.ui.theme.DzGoldContainer
import com.example.ui.theme.DzGreenContainer
import com.example.ui.theme.DzGreenDark
import com.example.ui.theme.DzGreenPrimary
import com.example.ui.theme.DzRedUrgent
import com.example.ui.viewmodel.MarketplaceViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    listing: ListingEntity,
    viewModel: MarketplaceViewModel,
    onBack: () -> Unit,
    onChatClick: (ListingEntity) -> Unit,
    onOrderCompleted: (OrderEntity) -> Unit,
    onProceedToPaymentConfirmation: ((PaymentCheckoutSummary) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(listing.id) {
        viewModel.incrementViews(listing.id)
    }

    var selectedBuyerWilaya by remember { mutableStateOf(WilayaData.getWilaya(16)) } // Alger default
    var showWilayaSheet by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showBaridiMobModal by remember { mutableStateOf(false) }
    var showCibModal by remember { mutableStateOf(false) }

    // Checkout Form state
    var buyerName by remember { mutableStateOf("Amine Benali") }
    var buyerPhone by remember { mutableStateOf("0554 12 87 65") }
    var buyerCommune by remember { mutableStateOf("Chéraga") }
    var buyerAddress by remember { mutableStateOf("Cité 1200 logts, Bt 4, N° 15") }
    var selectedPaymentType by remember { mutableStateOf(PaymentType.BARIDIMOB) }
    var selectedCarrier by remember { mutableStateOf(DeliveryCarrier.YALIDINE) }
    var selectedServiceType by remember { mutableStateOf(DeliveryServiceType.HOME_DELIVERY) }

    val estimatedDeliveryFee = remember(selectedBuyerWilaya, selectedServiceType) {
        if (selectedServiceType == DeliveryServiceType.HOME_DELIVERY) {
            selectedBuyerWilaya?.defaultDeliveryFeeHome ?: listing.deliveryHomeFee
        } else {
            selectedBuyerWilaya?.defaultDeliveryFeeDesk ?: listing.deliveryDeskFee
        }
    }
    val totalAmount = listing.priceDzd + estimatedDeliveryFee

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(listing.title, maxLines = 1, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleFavorite(listing) },
                        modifier = Modifier.testTag("detail_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (listing.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favoris",
                            tint = if (listing.isFavorite) DzRedUrgent else Color.Gray
                        )
                    }
                    IconButton(onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Regardez cette annonce sur SouqDZ: ${listing.title} à ${listing.priceDzd} DA (${listing.wilayaName})")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Partager l'annonce"))
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Partager")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            // Sticky Bottom CTA Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("detail_bottom_bar"),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onChatClick(listing) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_chat_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = DzGreenPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Offre / Chat", color = DzGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { showCheckoutDialog = true },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("detail_buy_now_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DzGreenPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Acheter Daman", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Graphic Image Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    ItemVisualGraphic(
                        imageKey = listing.imageUrl,
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 0
                    )

                    // Condition Pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "État: ${listing.condition}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Urgent Pill
                    if (listing.isUrgent) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DzRedUrgent)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "🔥 URGENT",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // Price & Title & Escrow Badge
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PriceTagDZD(
                            priceDzd = listing.priceDzd,
                            isNegotiable = listing.isNegotiable,
                            isLarge = true
                        )
                        EscrowBadge()
                    }

                    Text(
                        text = listing.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = DzGreenPrimary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "${String.format("%02d", listing.wilayaCode)} - ${listing.wilayaName}, ${listing.communeName}",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text(
                                text = "${listing.viewsCount} vues",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            item {
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // Seller Profile Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(DzGreenPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = listing.sellerName.take(2).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(listing.sellerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        if (listing.sellerVerified) {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Vérifié", tint = DzGreenDark, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = DzGold, modifier = Modifier.size(14.dp))
                                        Text(
                                            text = "${listing.sellerRating} • Vendeur vérifié BaridiMob",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            // Direct Phone Call Button
                            IconButton(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${listing.sellerPhone.replace(" ", "")}")
                                    }
                                    context.startActivity(dialIntent)
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(DzGreenPrimary)
                                    .size(38.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = "Appeler", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }

                        // BaridiMob RIP badge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.6f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Paiements acceptés: BaridiMob (Algérie Poste) • CIB / Edahabia • Daman DZ",
                                fontSize = 11.sp,
                                color = DzGreenDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Description Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Description / التفاصيل", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(
                        text = listing.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                    )
                }
            }

            // Algerian Delivery & Shipping Simulator Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = DzGreenPrimary)
                                Text("Livraison & Tarifs Wilayas", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            // Wilaya picker button
                            Text(
                                text = "Wilaya: ${selectedBuyerWilaya?.nameFr ?: "Alger"}",
                                color = DzGreenPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DzGreenContainer)
                                    .clickable { showWilayaSheet = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Delivery options preview
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Yalidine Express option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Yalidine Express (Bureau / Stop Desk)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Délai estimé: 24 - 48h", fontSize = 11.sp, color = Color.Gray)
                                }
                                Text("${selectedBuyerWilaya?.defaultDeliveryFeeDesk ?: 400} DA", fontWeight = FontWeight.ExtraBold, color = DzGreenPrimary, fontSize = 13.sp)
                            }

                            // Home delivery option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Livraison À Domicile (Yalidine / ZR Express)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Remise en main propre contre code PIN", fontSize = 11.sp, color = Color.Gray)
                                }
                                Text("${selectedBuyerWilaya?.defaultDeliveryFeeHome ?: 650} DA", fontWeight = FontWeight.ExtraBold, color = DzGreenPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Checkout / Buy Dialog
    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = DzGreenPrimary)
                    Text("Acheter avec Garantie DZ Daman", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "1. Coordonnées de Livraison:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = buyerName,
                            onValueChange = { buyerName = it },
                            label = { Text("Nom & Prénom") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("checkout_name_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = buyerPhone,
                            onValueChange = { buyerPhone = it },
                            label = { Text("Numéro de Téléphone (DZ)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("checkout_phone_input")
                        )
                    }

                    item {
                        // Wilaya Selector for Checkout
                        OutlinedButton(
                            onClick = { showWilayaSheet = true },
                            modifier = Modifier.fillMaxWidth().testTag("checkout_wilaya_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Wilaya: ${selectedBuyerWilaya?.code} - ${selectedBuyerWilaya?.nameFr} (${selectedBuyerWilaya?.nameAr})")
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = buyerCommune,
                            onValueChange = { buyerCommune = it },
                            label = { Text("Commune / Baladiya") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = buyerAddress,
                            onValueChange = { buyerAddress = it },
                            label = { Text("Adresse complète ou Bureau Stop Desk") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text(
                            text = "2. Mode de Livraison:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { selectedServiceType = DeliveryServiceType.HOME_DELIVERY },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedServiceType == DeliveryServiceType.HOME_DELIVERY) DzGreenContainer else Color.Transparent
                                )
                            ) {
                                Text("À Domicile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { selectedServiceType = DeliveryServiceType.STOP_DESK },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedServiceType == DeliveryServiceType.STOP_DESK) DzGreenContainer else Color.Transparent
                                )
                            ) {
                                Text("Stop Desk", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Text(
                            text = "3. Mode de Paiement Sécurisé:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    item {
                        PaymentMethodSelector(
                            selectedPaymentType = selectedPaymentType,
                            onPaymentTypeSelected = { selectedPaymentType = it }
                        )
                    }

                    item {
                        // Total Price Summary
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DzGreenContainer),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Prix article:", fontSize = 12.sp)
                                    Text("${NumberFormat.getInstance(Locale.FRANCE).format(listing.priceDzd)} DA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Frais livraison (${selectedBuyerWilaya?.nameFr}):", fontSize = 12.sp)
                                    Text("$estimatedDeliveryFee DA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total TTC:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${NumberFormat.getInstance(Locale.FRANCE).format(totalAmount)} DA", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = DzGreenDark)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCheckoutDialog = false
                        when (selectedPaymentType) {
                            PaymentType.BARIDIMOB, PaymentType.EDAHABIA_CIB -> {
                                if (onProceedToPaymentConfirmation != null) {
                                    val summary = PaymentCheckoutSummary(
                                        listingId = listing.id,
                                        itemTitle = listing.title,
                                        itemImageUrl = listing.imageUrl,
                                        sellerName = listing.sellerName,
                                        sellerRip = listing.sellerBaridiRip,
                                        buyerName = buyerName,
                                        buyerPhone = buyerPhone,
                                        buyerWilayaName = selectedBuyerWilaya?.nameFr ?: "Alger",
                                        buyerCommune = buyerCommune,
                                        carrier = selectedCarrier,
                                        itemPriceDzd = listing.priceDzd,
                                        deliveryFeeDzd = estimatedDeliveryFee,
                                        initialPaymentType = selectedPaymentType
                                    )
                                    onProceedToPaymentConfirmation(summary)
                                } else {
                                    if (selectedPaymentType == PaymentType.BARIDIMOB) {
                                        showBaridiMobModal = true
                                    } else {
                                        showCibModal = true
                                    }
                                }
                            }
                            PaymentType.CASH_ON_DELIVERY -> {
                                viewModel.createOrder(
                                    listing = listing,
                                    buyerName = buyerName,
                                    buyerPhone = buyerPhone,
                                    buyerWilayaCode = selectedBuyerWilaya?.code ?: 16,
                                    buyerCommune = buyerCommune,
                                    buyerAddressLine = buyerAddress,
                                    paymentType = PaymentType.CASH_ON_DELIVERY,
                                    carrier = selectedCarrier,
                                    serviceType = selectedServiceType,
                                    paymentReference = "COD-${(100000..999999).random()}",
                                    onOrderCreated = { onOrderCompleted(it) }
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DzGreenPrimary),
                    modifier = Modifier.testTag("checkout_continue_button")
                ) {
                    Text("Procéder au Paiement")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCheckoutDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // BaridiMob Modal
    if (showBaridiMobModal) {
        BaridiMobPaymentDialog(
            amountDzd = totalAmount,
            sellerRip = listing.sellerBaridiRip,
            sellerName = listing.sellerName,
            onConfirmed = { transactionRef ->
                showBaridiMobModal = false
                viewModel.createOrder(
                    listing = listing,
                    buyerName = buyerName,
                    buyerPhone = buyerPhone,
                    buyerWilayaCode = selectedBuyerWilaya?.code ?: 16,
                    buyerCommune = buyerCommune,
                    buyerAddressLine = buyerAddress,
                    paymentType = PaymentType.BARIDIMOB,
                    carrier = selectedCarrier,
                    serviceType = selectedServiceType,
                    paymentReference = transactionRef,
                    onOrderCreated = { onOrderCompleted(it) }
                )
            },
            onDismiss = { showBaridiMobModal = false }
        )
    }

    // CIB / Edahabia Modal
    if (showCibModal) {
        CibSatimPaymentDialog(
            amountDzd = totalAmount,
            onConfirmed = { authCode ->
                showCibModal = false
                viewModel.createOrder(
                    listing = listing,
                    buyerName = buyerName,
                    buyerPhone = buyerPhone,
                    buyerWilayaCode = selectedBuyerWilaya?.code ?: 16,
                    buyerCommune = buyerCommune,
                    buyerAddressLine = buyerAddress,
                    paymentType = PaymentType.EDAHABIA_CIB,
                    carrier = selectedCarrier,
                    serviceType = selectedServiceType,
                    paymentReference = authCode,
                    onOrderCreated = { onOrderCompleted(it) }
                )
            },
            onDismiss = { showCibModal = false }
        )
    }

    if (showWilayaSheet) {
        AlgerianWilayaBottomSheet(
            selectedWilayaCode = selectedBuyerWilaya?.code,
            onWilayaSelected = { wilaya ->
                if (wilaya != null) {
                    selectedBuyerWilaya = wilaya
                    if (wilaya.communes.isNotEmpty()) {
                        buyerCommune = wilaya.communes.first()
                    }
                }
            },
            onDismissRequest = { showWilayaSheet = false },
            showAllOption = false
        )
    }
}
