package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.example.data.model.CategoryData
import com.example.data.model.WilayaData
import com.example.ui.components.AlgerianWilayaBottomSheet
import com.example.ui.components.ItemVisualGraphic
import com.example.ui.theme.DzGold
import com.example.ui.theme.DzGreenContainer
import com.example.ui.theme.DzGreenDark
import com.example.ui.theme.DzGreenPrimary
import com.example.ui.theme.DzRedUrgent
import com.example.ui.viewmodel.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    viewModel: MarketplaceViewModel,
    onBack: () -> Unit,
    onListingCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var isNegotiable by remember { mutableStateOf(true) }
    var selectedCategoryId by remember { mutableStateOf(CategoryData.allCategories.first().id) }
    var selectedSubcategory by remember { mutableStateOf(CategoryData.allCategories.first().subcategories.first()) }
    var selectedCondition by remember { mutableStateOf("Neuf") }
    var selectedWilaya by remember { mutableStateOf(WilayaData.getWilaya(16)!!) } // Alger default
    var communeName by remember { mutableStateOf("Chéraga") }
    var sellerName by remember { mutableStateOf("Karim DZ") }
    var sellerPhone by remember { mutableStateOf("0550 12 34 56") }
    var sellerBaridiRip by remember { mutableStateOf("00799999001234567890") }
    var deliverySupported by remember { mutableStateOf(true) }
    var useYalidine by remember { mutableStateOf(true) }
    var useZrExpress by remember { mutableStateOf(true) }
    var useEmsPoste by remember { mutableStateOf(true) }
    var isUrgent by remember { mutableStateOf(false) }
    var selectedImageKey by remember { mutableStateOf("img_ps5") }
    var showWilayaSheet by remember { mutableStateOf(false) }

    val conditions = listOf("Neuf", "Comme neuf", "Bon état", "Occasion")
    val sampleImageKeys = listOf(
        "img_golf8" to "Véhicule",
        "img_iphone15" to "Téléphone",
        "img_ps5" to "Gaming",
        "img_ac" to "Électroménager",
        "img_karakou" to "Mode / Traditionnel",
        "img_olive_oil" to "Huile d'olive",
        "img_dates" to "Dattes",
        "img_apartment" to "Immobilier"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Publier une Annonce / بيع سلعة", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("create_back_button")) {
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Catégorie / القسم", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(CategoryData.allCategories) { cat ->
                            val isSelected = selectedCategoryId == cat.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategoryId = cat.id
                                    selectedSubcategory = cat.subcategories.firstOrNull() ?: ""
                                },
                                label = { Text("${cat.nameFr} (${cat.nameAr})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DzGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Visual Theme / Image Key Picker
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("2. Illustration / Photo de l'article", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(sampleImageKeys) { (key, label) ->
                            val isSelected = selectedImageKey == key
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedImageKey = key }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    ItemVisualGraphic(imageKey = key, modifier = Modifier.fillMaxSize())
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.4f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                        }
                                    }
                                }
                                Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }

            // Title & Description
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("3. Détails de l'article", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Titre de l'annonce (ex: Golf 8 R-Line 2023)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("create_title_input")
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description détaillée (spécifications, état, accessoires...)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("create_desc_input")
                    )
                }
            }

            // Price & Negotiation
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("4. Prix & Conditions (DA)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Prix en Dinar Algérien (DA)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("create_price_input")
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Checkbox(
                                checked = isNegotiable,
                                onCheckedChange = { isNegotiable = it },
                                colors = CheckboxDefaults.colors(checkedColor = DzGreenPrimary)
                            )
                            Text("Négociable", fontSize = 13.sp)
                        }
                    }

                    // Condition chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        conditions.forEach { cond ->
                            val isSelected = selectedCondition == cond
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCondition = cond },
                                label = { Text(cond) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DzGreenContainer,
                                    selectedLabelColor = DzGreenDark
                                )
                            )
                        }
                    }
                }
            }

            // Wilaya & Location
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("5. Localisation (58 Wilayas)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedButton(
                        onClick = { showWilayaSheet = true },
                        modifier = Modifier.fillMaxWidth().testTag("create_wilaya_button"),
                        shape = RoundedCornerShape(10.dp)
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
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = DzGreenPrimary)
                                Text("${String.format("%02d", selectedWilaya.code)} - ${selectedWilaya.nameFr} (${selectedWilaya.nameAr})")
                            }
                            Text("Changer", color = DzGreenPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = communeName,
                        onValueChange = { communeName = it },
                        label = { Text("Commune / Baladiya") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("create_commune_input")
                    )
                }
            }

            // Delivery and Payment settings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = DzGreenDark)
                                Text("Livraison vers 58 Wilayas", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Switch(
                                checked = deliverySupported,
                                onCheckedChange = { deliverySupported = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DzGreenPrimary, checkedTrackColor = DzGreenContainer)
                            )
                        }

                        if (deliverySupported) {
                            Text("Transporteurs partenaires acceptés:", fontSize = 12.sp, color = Color.Gray)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = useYalidine, onCheckedChange = { useYalidine = it })
                                    Text("Yalidine", fontSize = 12.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = useZrExpress, onCheckedChange = { useZrExpress = it })
                                    Text("ZR Express", fontSize = 12.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = useEmsPoste, onCheckedChange = { useEmsPoste = it })
                                    Text("EMS Poste", fontSize = 12.sp)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = sellerBaridiRip,
                            onValueChange = { sellerBaridiRip = it },
                            label = { Text("RIP BaridiMob (pour recevoir vos fonds sécurisés)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Urgent toggle
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isUrgent) DzRedUrgent.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Mettre en Offre Urgente (عاجل)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Apparaît en tête de liste avec badge rouge", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isUrgent,
                        onCheckedChange = { isUrgent = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = DzRedUrgent, checkedTrackColor = DzRedUrgent.copy(alpha = 0.4f))
                    )
                }
            }

            // Submit Button
            item {
                val parsedPrice = priceText.toLongOrNull() ?: 0L
                val isValid = title.isNotBlank() && description.isNotBlank() && parsedPrice > 0

                Button(
                    onClick = {
                        val carriers = mutableListOf<String>()
                        if (useYalidine) carriers.add("Yalidine")
                        if (useZrExpress) carriers.add("ZR Express")
                        if (useEmsPoste) carriers.add("EMS Algérie Poste")

                        viewModel.createListing(
                            title = title,
                            description = description,
                            priceDzd = parsedPrice,
                            isNegotiable = isNegotiable,
                            categoryId = selectedCategoryId,
                            subcategory = selectedSubcategory,
                            condition = selectedCondition,
                            wilayaCode = selectedWilaya.code,
                            wilayaName = selectedWilaya.nameFr,
                            communeName = communeName,
                            sellerName = sellerName,
                            sellerPhone = sellerPhone,
                            sellerBaridiRip = sellerBaridiRip,
                            imageUrl = selectedImageKey,
                            deliverySupported = deliverySupported,
                            deliveryCarriers = carriers,
                            deliveryHomeFee = selectedWilaya.defaultDeliveryFeeHome,
                            deliveryDeskFee = selectedWilaya.defaultDeliveryFeeDesk,
                            isUrgent = isUrgent,
                            onSuccess = onListingCreated
                        )
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_create_listing_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DzGreenPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Publish, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Publier l'Annonce Maintenant", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }

    if (showWilayaSheet) {
        AlgerianWilayaBottomSheet(
            selectedWilayaCode = selectedWilaya.code,
            onWilayaSelected = { wilaya ->
                if (wilaya != null) {
                    selectedWilaya = wilaya
                    if (wilaya.communes.isNotEmpty()) {
                        communeName = wilaya.communes.first()
                    }
                }
            },
            onDismissRequest = { showWilayaSheet = false },
            showAllOption = false
        )
    }
}
