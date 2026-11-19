package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentType
import com.example.ui.theme.DzBaridiMobBlue
import com.example.ui.theme.DzBaridiMobYellow
import com.example.ui.theme.DzCibTeal
import com.example.ui.theme.DzGold
import com.example.ui.theme.DzGreenContainer
import com.example.ui.theme.DzGreenDark
import com.example.ui.theme.DzGreenPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentMethodSelector(
    selectedPaymentType: PaymentType,
    onPaymentTypeSelected: (PaymentType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PaymentType.values().forEach { paymentType ->
            val isSelected = selectedPaymentType == paymentType
            val (icon, color) = when (paymentType) {
                PaymentType.BARIDIMOB -> Icons.Default.AccountBalance to DzBaridiMobBlue
                PaymentType.EDAHABIA_CIB -> Icons.Default.CreditCard to DzCibTeal
                PaymentType.CASH_ON_DELIVERY -> Icons.Default.LocalShipping to DzGreenPrimary
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onPaymentTypeSelected(paymentType) }
                    .testTag("payment_method_${paymentType.name}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) DzGreenContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                ),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, DzGreenPrimary) else null,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onPaymentTypeSelected(paymentType) },
                        colors = RadioButtonDefaults.colors(selectedColor = DzGreenPrimary)
                    )

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = paymentType.titleFr,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (paymentType.isEscrowProtected) {
                                EscrowBadge(compact = true)
                            }
                        }
                        Text(
                            text = paymentType.titleAr,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = paymentType.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BaridiMobPaymentDialog(
    amountDzd: Long,
    sellerRip: String,
    sellerName: String,
    onConfirmed: (transactionRef: String) -> Unit,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var transactionRef by remember { mutableStateOf("BMOB-TX-${(100000..999999).random()}") }
    var isVerifying by remember { mutableStateOf(false) }
    var copiedToClipboard by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val formattedAmount = NumberFormat.getInstance(Locale.FRANCE).format(amountDzd)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DzBaridiMobBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text("BM", color = DzBaridiMobYellow, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
                Column {
                    Text("Paiement BaridiMob (بريدي موب)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Algérie Poste - بريد الجزائر", fontSize = 12.sp, color = Color.Gray)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Escrow explanation banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DzGreenContainer)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = DzGreenDark, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Séquestre Garanti DZ Daman: Vos fonds sont protégés et ne seront débloqués au vendeur qu'après réception et vérification de votre colis.",
                            fontSize = 11.sp,
                            color = DzGreenDark,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Amount to transfer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Montant total à virer:", fontSize = 13.sp, color = Color.Gray)
                    Text("$formattedAmount DA", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DzGreenPrimary)
                }

                // Seller RIP Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("RIP Destinataire (Vendeur: $sellerName):", fontSize = 11.sp, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sellerRip,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = DzBaridiMobBlue
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(sellerRip))
                                    copiedToClipboard = true
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (copiedToClipboard) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                                    contentDescription = "Copier RIP",
                                    tint = if (copiedToClipboard) DzGreenPrimary else DzBaridiMobBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // BaridiPay QR Simulation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.QrCode, contentDescription = "QR Code BaridiPay", tint = Color.Black, modifier = Modifier.size(36.dp))
                        Column {
                            Text("Scanner avec BaridiMob App", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                            Text("QR Code BaridiPay sécurisé généré", fontSize = 10.sp, color = Color.DarkGray)
                        }
                    }
                }

                OutlinedTextField(
                    value = transactionRef,
                    onValueChange = { transactionRef = it },
                    label = { Text("Numéro de Transaction / Reçu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("baridimob_tx_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isVerifying = true
                    scope.launch {
                        delay(1200) // Simulated Algerian Postal Gateway verification
                        isVerifying = false
                        onConfirmed(transactionRef)
                    }
                },
                enabled = !isVerifying && transactionRef.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DzGreenPrimary),
                modifier = Modifier.testTag("confirm_baridimob_payment")
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Vérification Algérie Poste...")
                } else {
                    Text("Confirmer & Bloquer sous Séquestre")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun CibSatimPaymentDialog(
    amountDzd: Long,
    onConfirmed: (authCode: String) -> Unit,
    onDismiss: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("6035 7123 4589 9012") }
    var expiryDate by remember { mutableStateOf("12/28") }
    var cvv by remember { mutableStateOf("842") }
    var cardHolder by remember { mutableStateOf("AMINE BENALI") }
    var isProcessing3DS by remember { mutableStateOf(false) }
    var showOtpStep by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("749102") }
    val scope = rememberCoroutineScope()
    val formattedAmount = NumberFormat.getInstance(Locale.FRANCE).format(amountDzd)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = DzCibTeal)
                Column {
                    Text("Paiement CIB / Edahabia", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Passerelle SATIM / GIE Monétique 3D-Secure", fontSize = 11.sp, color = Color.Gray)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!showOtpStep) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DzCibTeal)
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("CARTE EDAHABIA / CIB", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("SATIM DZ", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                            Text(
                                text = cardNumber,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 2.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cardHolder, color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                                Text("EXP: $expiryDate", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Montant à débiter:", fontSize = 13.sp, color = Color.Gray)
                        Text("$formattedAmount DA", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DzGreenPrimary)
                    }

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text("Numéro de carte (16 chiffres)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { expiryDate = it },
                            label = { Text("MM/AA") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { cvv = it },
                            label = { Text("CVV2") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                } else {
                    // OTP Step
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(DzGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = DzGreenDark)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Validation 3D-Secure SATIM",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Un code SMS à 6 chiffres a été envoyé au 05** ** ** 65 associé à votre carte Edahabia.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { otpCode = it },
                            label = { Text("Code OTP SMS") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("cib_otp_input")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!showOtpStep) {
                        isProcessing3DS = true
                        scope.launch {
                            delay(1000)
                            isProcessing3DS = false
                            showOtpStep = true
                        }
                    } else {
                        isProcessing3DS = true
                        scope.launch {
                            delay(1200)
                            isProcessing3DS = false
                            onConfirmed("SATIM-AUTH-${(100000..999999).random()}")
                        }
                    }
                },
                enabled = !isProcessing3DS,
                colors = ButtonDefaults.buttonColors(containerColor = DzGreenPrimary),
                modifier = Modifier.testTag("submit_cib_payment")
            ) {
                if (isProcessing3DS) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Traitement SATIM...")
                } else {
                    Text(if (!showOtpStep) "Continuer vers 3D-Secure" else "Valider & Sécuriser la Commande")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
