package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryCarrier
import com.example.data.model.PaymentType
import com.example.ui.theme.DzBaridiMobBlue
import com.example.ui.theme.DzBaridiMobYellow
import com.example.ui.theme.DzCibTeal
import com.example.ui.theme.DzGold
import com.example.ui.theme.DzGoldContainer
import com.example.ui.theme.DzGoldLight
import com.example.ui.theme.DzGreenContainer
import com.example.ui.theme.DzGreenDark
import com.example.ui.theme.DzGreenLight
import com.example.ui.theme.DzGreenOnContainer
import com.example.ui.theme.DzGreenPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Data bundle representing checkout details passed to the payment confirmation layout.
 */
data class PaymentCheckoutSummary(
    val listingId: String = "101",
    val itemTitle: String = "iPhone 15 Pro 128Go Titane Naturel",
    val itemImageUrl: String = "",
    val sellerName: String = "Youcef Tech",
    val sellerRip: String = "00799999002345678912",
    val buyerName: String = "Karim Benali",
    val buyerPhone: String = "0555123456",
    val buyerWilayaName: String = "Alger (الجزائر)",
    val buyerCommune: String = "Bab Ezzouar",
    val carrier: DeliveryCarrier = DeliveryCarrier.YALIDINE,
    val itemPriceDzd: Long = 185000L,
    val deliveryFeeDzd: Long = 400L,
    val initialPaymentType: PaymentType = PaymentType.BARIDIMOB
) {
    val totalAmountDzd: Long
        get() = itemPriceDzd + deliveryFeeDzd
}

/**
 * Result state when the user confirms payment or when the gateway processes the transaction.
 */
sealed class PaymentGatewayResult {
    data object Idle : PaymentGatewayResult()
    data class Processing(val message: String) : PaymentGatewayResult()
    data class Success(
        val paymentType: PaymentType,
        val transactionReference: String,
        val approvalCode: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : PaymentGatewayResult()
    data class Error(val errorMessage: String) : PaymentGatewayResult()
}

/**
 * Placeholder Payment Confirmation Screen Layout supporting future integration for:
 * 1. BaridiMob (Algérie Poste) - RIP transfer, BaridiPay QR, Pre-auth reference
 * 2. CIB / Edahabia (SATIM / GIE Monétique) - EMV card tokenization, 3D-Secure 2.0 OTP
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentConfirmationScreen(
    checkoutSummary: PaymentCheckoutSummary = PaymentCheckoutSummary(),
    onBack: () -> Unit,
    onPaymentConfirmed: (paymentType: PaymentType, transactionRef: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Selected payment tab: 0 = BaridiMob, 1 = CIB / Edahabia
    var selectedMethodTab by remember {
        mutableStateOf(
            if (checkoutSummary.initialPaymentType == PaymentType.EDAHABIA_CIB) 1 else 0
        )
    }

    // BaridiMob input state
    var baridiMobTransactionRef by remember { mutableStateOf("") }
    var baridiMobSenderPhone by remember { mutableStateOf(checkoutSummary.buyerPhone) }
    var hasCopiedRip by remember { mutableStateOf(false) }

    // CIB / Edahabia input state
    var cardNumber by remember { mutableStateOf("5053 7700 1234 5678") }
    var cardExpiry by remember { mutableStateOf("12/28") }
    var cardCvv by remember { mutableStateOf("842") }
    var cardHolder by remember { mutableStateOf(checkoutSummary.buyerName.uppercase()) }
    var otpCode by remember { mutableStateOf("") }
    var otpStepActive by remember { mutableStateOf(false) }

    // Gateway Simulation & Integration Debug View
    var gatewayResult by remember { mutableStateOf<PaymentGatewayResult>(PaymentGatewayResult.Idle) }
    var showApiDeveloperPayload by remember { mutableStateOf(false) }

    val formattedTotal = NumberFormat.getInstance(Locale.FRANCE).format(checkoutSummary.totalAmountDzd)
    val formattedItemPrice = NumberFormat.getInstance(Locale.FRANCE).format(checkoutSummary.itemPriceDzd)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("payment_confirmation_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Confirmation de Paiement",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "تأكيد الدفع • Garantie DZ Daman",
                            fontSize = 11.sp,
                            color = DzGreenPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("payment_confirmation_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    // Quick toggle to show future API integration payload
                    IconButton(
                        onClick = { showApiDeveloperPayload = !showApiDeveloperPayload },
                        modifier = Modifier.testTag("toggle_api_payload_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Inspecter API Integration Payload",
                            tint = if (showApiDeveloperPayload) DzGreenPrimary else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. DZ Daman Escrow Trust Banner
            item {
                EscrowTrustHeaderBanner()
            }

            // 2. Total Amount & Order Summary Card
            item {
                AmountAndOrderSummaryCard(
                    checkoutSummary = checkoutSummary,
                    formattedItemPrice = formattedItemPrice,
                    formattedTotal = formattedTotal
                )
            }

            // 3. Payment Method Tabs (BaridiMob vs CIB/Edahabia)
            item {
                PaymentMethodTabSelector(
                    selectedTab = selectedMethodTab,
                    onTabSelected = { selectedMethodTab = it }
                )
            }

            // 4. Future Integration Developer Info Banner
            item {
                GatewayIntegrationStatusBadge(
                    currentMethod = if (selectedMethodTab == 0) "BaridiMob Algérie Poste v2" else "SATIM GIE Monétique CIB/Edahabia"
                )
            }

            // Optional: API Developer Payload Preview
            if (showApiDeveloperPayload) {
                item {
                    ApiPayloadInspectorCard(
                        selectedMethodTab = selectedMethodTab,
                        checkoutSummary = checkoutSummary,
                        baridiMobTransactionRef = baridiMobTransactionRef,
                        cardNumber = cardNumber
                    )
                }
            }

            // 5. Method-Specific Confirmation Layout
            if (selectedMethodTab == 0) {
                // ==========================================
                // BARIDIMOB CONFIRMATION LAYOUT (ALGERIE POSTE)
                // ==========================================
                item {
                    BaridiMobConfirmationLayout(
                        sellerRip = checkoutSummary.sellerRip,
                        sellerName = checkoutSummary.sellerName,
                        totalAmountDzd = checkoutSummary.totalAmountDzd,
                        formattedTotal = formattedTotal,
                        transactionRef = baridiMobTransactionRef,
                        senderPhone = baridiMobSenderPhone,
                        hasCopiedRip = hasCopiedRip,
                        onCopyRip = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("RIP BaridiMob", checkoutSummary.sellerRip))
                            hasCopiedRip = true
                            Toast.makeText(context, "RIP copié : ${checkoutSummary.sellerRip}", Toast.LENGTH_SHORT).show()
                        },
                        onTransactionRefChange = { baridiMobTransactionRef = it },
                        onSenderPhoneChange = { baridiMobSenderPhone = it }
                    )
                }
            } else {
                // ==========================================
                // CIB / EDAHABIA CONFIRMATION LAYOUT (SATIM)
                // ==========================================
                item {
                    CibEdahabiaConfirmationLayout(
                        totalAmountFormatted = formattedTotal,
                        cardNumber = cardNumber,
                        cardExpiry = cardExpiry,
                        cardCvv = cardCvv,
                        cardHolder = cardHolder,
                        otpCode = otpCode,
                        otpStepActive = otpStepActive,
                        buyerPhone = checkoutSummary.buyerPhone,
                        onCardNumberChange = { cardNumber = it },
                        onCardExpiryChange = { cardExpiry = it },
                        onCardCvvChange = { cardCvv = it },
                        onCardHolderChange = { cardHolder = it },
                        onOtpCodeChange = { otpCode = it },
                        onRequestOtp = { otpStepActive = true }
                    )
                }
            }

            // 6. Security Assurance Badges
            item {
                SecurityAssuranceBadges()
            }

            // 7. Action Button: Confirm & Submit to Payment Gateway
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when (val result = gatewayResult) {
                        is PaymentGatewayResult.Processing -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = DzGreenLight)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = DzGreenPrimary,
                                        strokeWidth = 2.5.dp
                                    )
                                    Column {
                                        Text(result.message, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Sécurisation du montant sous séquestre...", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        is PaymentGatewayResult.Success -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = DzGreenContainer)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = DzGreenDark)
                                        Text("Paiement Sécurisé avec Succès !", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DzGreenDark)
                                    }
                                    Text("Réf: ${result.transactionReference} | Code Auth: ${result.approvalCode}", fontSize = 12.sp)
                                    Text("Les fonds sont bloqués sur DZ Daman jusqu'à réception de votre colis.", fontSize = 11.sp, color = DzGreenDark)
                                }
                            }
                        }

                        is PaymentGatewayResult.Error -> {
                            Text(
                                text = result.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        PaymentGatewayResult.Idle -> {
                            // Primary Submit Button
                            Button(
                                onClick = {
                                    scope.launch {
                                        val method = if (selectedMethodTab == 0) PaymentType.BARIDIMOB else PaymentType.EDAHABIA_CIB
                                        gatewayResult = PaymentGatewayResult.Processing("Communication avec la passerelle ${if (selectedMethodTab == 0) "BaridiMob" else "SATIM"}...")
                                        delay(1400)

                                        val ref = if (selectedMethodTab == 0) {
                                            if (baridiMobTransactionRef.isNotBlank()) baridiMobTransactionRef else "BM-${(100000..999999).random()}"
                                        } else {
                                            "SATIM-${(1000000..9999999).random()}"
                                        }

                                        val approval = "AUTH-${(1000..9999).random()}"
                                        gatewayResult = PaymentGatewayResult.Success(method, ref, approval)
                                        delay(800)
                                        onPaymentConfirmed(method, ref)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("confirm_payment_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedMethodTab == 0) DzBaridiMobBlue else DzGreenPrimary
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White)
                                    Text(
                                        text = if (selectedMethodTab == 0) "Confirmer le Paiement BaridiMob ($formattedTotal DA)" else "Valider et Payer avec SATIM ($formattedTotal DA)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            // Secondary Later button
                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pay_later_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Payer plus tard / Enregistrer le brouillon")
                            }
                        }
                    }

                    // Hotline Support Note
                    Text(
                        text = "Assistance 24/7 : Algérie Poste 1530 • SATIM Monétique 3020",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Top Trust Banner explaining escrow protection
 */
@Composable
private fun EscrowTrustHeaderBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DzGreenLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(DzGreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Garantie DZ Daman Sécurisée",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = DzGreenDark
                    )
                    Text("ضمان الجزائر", color = DzGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Text(
                    text = "L'argent reste bloqué sous séquestre jusqu'à l'inspection du colis auprès du livreur.",
                    fontSize = 11.sp,
                    color = DzGreenOnContainer,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

/**
 * Order amount & delivery summary
 */
@Composable
private fun AmountAndOrderSummaryCard(
    checkoutSummary: PaymentCheckoutSummary,
    formattedItemPrice: String,
    formattedTotal: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total à Régler", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = "$formattedTotal DA",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = DzGreenDark
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DzGoldContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Devise : DZD (دج)", color = Color(0xFF5D4037), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Article : ${checkoutSummary.itemTitle}", fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.weight(1f))
                Text("$formattedItemPrice DA", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = DzGreenPrimary, modifier = Modifier.size(14.dp))
                    Text("Livraison ${checkoutSummary.carrier.displayName} (${checkoutSummary.buyerWilayaName}) :", fontSize = 12.sp, color = Color.DarkGray)
                }
                Text("${checkoutSummary.deliveryFeeDzd} DA", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Frais Séquestre Daman :", fontSize = 12.sp, color = DzGreenPrimary)
                Text("0 DA (Offert)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DzGreenPrimary)
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Buyer & Seller info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Vendeur : ${checkoutSummary.sellerName}", fontSize = 11.sp, color = Color.Gray)
                Text("Acheteur : ${checkoutSummary.buyerName}", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

/**
 * Tab selector to toggle between BaridiMob and CIB/Edahabia
 */
@Composable
private fun PaymentMethodTabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = DzGreenPrimary,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                color = if (selectedTab == 0) DzBaridiMobBlue else DzGreenPrimary,
                height = 3.dp
            )
        },
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .testTag("payment_method_tabs")
    ) {
        // BaridiMob Tab
        Tab(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.testTag("tab_baridimob"),
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(DzBaridiMobBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("BM", color = DzBaridiMobYellow, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Column {
                        Text(
                            text = "BaridiMob",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) DzBaridiMobBlue else Color.DarkGray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "بريديموب",
                            fontSize = 9.sp,
                            color = if (selectedTab == 0) DzBaridiMobBlue else Color.Gray
                        )
                    }
                }
            }
        )

        // CIB / Edahabia Tab
        Tab(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.testTag("tab_cib_edahabia"),
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = if (selectedTab == 1) DzGreenPrimary else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "CIB / Edahabia",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) DzGreenPrimary else Color.DarkGray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "الذهبية / SATIM",
                            fontSize = 9.sp,
                            color = if (selectedTab == 1) DzGreenPrimary else Color.Gray
                        )
                    }
                }
            }
        )
    }
}

/**
 * Developer status badge showing readiness for future BaridiMob and SATIM API integration
 */
@Composable
private fun GatewayIntegrationStatusBadge(currentMethod: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = DzGold,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Mode Prêt pour Intégration : $currentMethod",
                fontSize = 11.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(DzGreenContainer)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("API Ready", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DzGreenDark)
            }
        }
    }
}

/**
 * BaridiMob layout: RIP display with copy, QR representation, transaction reference input, and deeplink hook
 */
@Composable
private fun BaridiMobConfirmationLayout(
    sellerRip: String,
    sellerName: String,
    totalAmountDzd: Long,
    formattedTotal: String,
    transactionRef: String,
    senderPhone: String,
    hasCopiedRip: Boolean,
    onCopyRip: () -> Unit,
    onTransactionRefChange: (String) -> Unit,
    onSenderPhoneChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Algérie Poste Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Brand Header
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
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DzBaridiMobBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("BM", color = DzBaridiMobYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Column {
                            Text("Algérie Poste • BaridiMob", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("بريد الجزائر • خدمة التحويل الفوري", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DzBaridiMobBlue.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Virement P2P", color = DzBaridiMobBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Divider()

                // Beneficiary RIP Box
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "1. RIP du Vendeur (Compte Séquestre Garanti) :",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    // Formatted RIP display
                    val formattedRip = formatAlgerianRip(sellerRip)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, DzBaridiMobBlue.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Numéro RIP (20 Chiffres) :", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = formattedRip,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                color = DzBaridiMobBlue
                            )
                            Text("Bénéficiaire : $sellerName", fontSize = 10.sp, color = Color.DarkGray)
                        }

                        Button(
                            onClick = onCopyRip,
                            modifier = Modifier.testTag("copy_rip_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasCopiedRip) DzGreenPrimary else DzBaridiMobBlue
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (hasCopiedRip) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copier RIP",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (hasCopiedRip) "Copié" else "Copier", fontSize = 11.sp)
                        }
                    }
                }

                // QR Code BaridiPay Mock Placeholder
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DzGoldLight)
                        .border(1.dp, DzGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = DzBaridiMobBlue)
                        Text("BaridiPay QR Code (Paiement Direct)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DzBaridiMobBlue)
                    }

                    // Visual QR mock
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "QR Code BaridiPay",
                                modifier = Modifier.size(72.dp),
                                tint = Color.Black
                            )
                            Text("$totalAmountDzd DA", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = DzBaridiMobBlue)
                        }
                    }

                    Text(
                        text = "Scannez depuis l'application BaridiMob pour remplir le montant et le RIP automatiquement.",
                        fontSize = 10.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }

                // Transaction Reference Verification Field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "2. Référence de la Transaction BaridiMob :",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = transactionRef,
                        onValueChange = onTransactionRefChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("baridimob_transaction_ref_input"),
                        placeholder = { Text("Ex: BM-20240914-987654 ou N° Reçu") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.AccountBalance, contentDescription = null, tint = DzBaridiMobBlue)
                        },
                        trailingIcon = {
                            if (transactionRef.isNotBlank()) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = DzGreenDark)
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Text(
                        text = "Entrez le numéro reçu par SMS d'Algérie Poste après avoir validé le virement.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                // Sender Phone Number
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Numéro de Téléphone Lié au Compte (+213) :",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = senderPhone,
                        onValueChange = onSenderPhoneChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("baridimob_sender_phone_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = {
                            Text("🇩🇿 +213", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }
    }
}

/**
 * CIB / Edahabia layout: SATIM EMV card preview, card number, expiry, CVV, and 3D-Secure 2.0 OTP simulation
 */
@Composable
private fun CibEdahabiaConfirmationLayout(
    totalAmountFormatted: String,
    cardNumber: String,
    cardExpiry: String,
    cardCvv: String,
    cardHolder: String,
    otpCode: String,
    otpStepActive: Boolean,
    buyerPhone: String,
    onCardNumberChange: (String) -> Unit,
    onCardExpiryChange: (String) -> Unit,
    onCardCvvChange: (String) -> Unit,
    onCardHolderChange: (String) -> Unit,
    onOtpCodeChange: (String) -> Unit,
    onRequestOtp: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // SATIM / Edahabia Golden Card Visual Mock
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF1E3C2B),
                                Color(0xFF00563B),
                                Color(0xFFC49A45)
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top row: Brand & EMV chip
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
                                    .size(32.dp, 24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DzGold),
                                contentAlignment = Alignment.Center
                            ) {
                                // EMV chip look
                                Box(
                                    modifier = Modifier
                                        .size(24.dp, 16.dp)
                                        .border(1.dp, Color(0xFF6B4E1B), RoundedCornerShape(2.dp))
                                )
                            }
                            Text("SATIM WebPay", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Text("الذهبية EDAHABIA", color = DzGold, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }

                    // Card Number
                    Text(
                        text = if (cardNumber.isNotBlank()) cardNumber else "•••• •••• •••• ••••",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    // Bottom row: Holder & Expiry
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TITULAIRE DE LA CARTE", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                            Text(cardHolder.ifBlank { "NOM PRENOM" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("EXPIRE FIN", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                            Text(cardExpiry.ifBlank { "MM/AA" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // SATIM Card Inputs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = DzGreenPrimary, modifier = Modifier.size(16.dp))
                    Text("Passerelle Sécurisée SATIM / GIE Monétique", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Divider()

                // Card Number Field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Numéro de Carte (16 chiffres Edahabia ou CIB) :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = onCardNumberChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("satim_card_number_input"),
                        placeholder = { Text("5053 7700 XXXX XXXX") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = DzCibTeal)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Row with Expiry & CVV
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Date d'expiration :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = cardExpiry,
                            onValueChange = onCardExpiryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("satim_card_expiry_input"),
                            placeholder = { Text("12/28") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Code CVV2 (3 chiffres) :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = cardCvv,
                            onValueChange = onCardCvvChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("satim_card_cvv_input"),
                            placeholder = { Text("•••") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Cardholder Name
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Nom et Prénom (tel qu'indiqué sur la carte) :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = cardHolder,
                        onValueChange = onCardHolderChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("satim_card_holder_input"),
                        placeholder = { Text("NOM PRENOM") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // 3D Secure / OTP Section
                AnimatedVisibility(
                    visible = otpStepActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DzGreenLight)
                            .border(1.dp, DzGreenPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = DzGreenPrimary)
                            Text("Authentification SATIM 3D-Secure 2.0", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DzGreenDark)
                        }
                        Text(
                            text = "Un code SMS à 6 chiffres a été envoyé au $buyerPhone pour autoriser le débit de $totalAmountFormatted DA.",
                            fontSize = 11.sp,
                            color = DzGreenOnContainer
                        )
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = onOtpCodeChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("satim_otp_input"),
                            placeholder = { Text("Entrez le code SMS (ex: 849201)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                if (!otpStepActive) {
                    OutlinedButton(
                        onClick = onRequestOtp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("request_satim_otp_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Activer la vérification 3D-Secure SMS", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Developer inspection card showing the future REST/Webhook payload for integration with BaridiMob or SATIM
 */
@Composable
private fun ApiPayloadInspectorCard(
    selectedMethodTab: Int,
    checkoutSummary: PaymentCheckoutSummary,
    baridiMobTransactionRef: String,
    cardNumber: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222A))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = DzGold, modifier = Modifier.size(16.dp))
                    Text("Future API Integration Payload (JSON)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Text(
                    if (selectedMethodTab == 0) "POST /api/v1/baridimob/verify" else "POST /api/v1/satim/registerOrder",
                    color = DzGold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }

            val sampleJson = if (selectedMethodTab == 0) {
                """
                {
                  "gateway": "ALGERIE_POSTE_BARIDIMOB",
                  "order_id": "${checkoutSummary.listingId}",
                  "amount_dzd": ${checkoutSummary.totalAmountDzd},
                  "currency": "DZD",
                  "seller_rip": "${checkoutSummary.sellerRip}",
                  "buyer_phone": "${checkoutSummary.buyerPhone}",
                  "escrow_mode": "DZ_DAMAN_HOLD",
                  "transaction_ref": "${baridiMobTransactionRef.ifBlank { "BM-AUTOGEN" }}",
                  "callback_url": "https://api.souqdz.com/webhooks/baridimob"
                }
                """.trimIndent()
            } else {
                """
                {
                  "gateway": "SATIM_GIE_MONETIQUE",
                  "orderNumber": "${checkoutSummary.listingId}",
                  "amount": ${checkoutSummary.totalAmountDzd * 100},
                  "currency": "012",
                  "pan": "${cardNumber.replace(" ", "")}",
                  "returnUrl": "https://api.souqdz.com/payment/satim/success",
                  "failUrl": "https://api.souqdz.com/payment/satim/fail",
                  "threeDSecure": true
                }
                """.trimIndent()
            }

            Text(
                text = sampleJson,
                color = Color(0xFF80CBC4),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF14171E))
                    .padding(10.dp)
            )

            Text(
                text = "Ce layout est prêt à se connecter directement à l'API BaridiMob (Algérie Poste) ou SATIM WebPay sans changer l'interface utilisateur.",
                color = Color.LightGray,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Security trust badges: SATIM, Algérie Poste, SSL 256-bit, DZ Daman Escrow
 */
@Composable
private fun SecurityAssuranceBadges() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = DzGreenPrimary, modifier = Modifier.size(14.dp))
            Text("SSL 256-bit", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = DzBaridiMobBlue, modifier = Modifier.size(14.dp))
            Text("Algérie Poste Certifié", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = DzCibTeal, modifier = Modifier.size(14.dp))
            Text("SATIM GIE Monétique", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Helper to display an Algerian 20-digit RIP formatted:
 * 007 99999 0001234567 89
 */
private fun formatAlgerianRip(rawRip: String): String {
    val clean = rawRip.filter { it.isDigit() }
    if (clean.length != 20) return rawRip
    return "${clean.substring(0, 3)} ${clean.substring(3, 8)} ${clean.substring(8, 18)} ${clean.substring(18, 20)}"
}
