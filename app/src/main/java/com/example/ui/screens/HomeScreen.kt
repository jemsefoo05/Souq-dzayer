package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ListingEntity
import com.example.data.model.CategoryData
import com.example.data.model.WilayaData
import com.example.ui.components.AlgerianWilayaBottomSheet
import com.example.ui.components.ListingCard
import com.example.ui.theme.DzGold
import com.example.ui.theme.DzGoldContainer
import com.example.ui.theme.DzGreenContainer
import com.example.ui.theme.DzGreenDark
import com.example.ui.theme.DzGreenLight
import com.example.ui.theme.DzGreenPrimary
import com.example.ui.theme.DzRedUrgent
import com.example.ui.viewmodel.MarketplaceViewModel
import com.example.util.LocationDetector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MarketplaceViewModel,
    onListingClick: (ListingEntity) -> Unit,
    onCreateListingClick: () -> Unit,
    onOrdersTrackingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listings by viewModel.filteredListings.collectAsState()
    val allListings by viewModel.allListings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedWilayaCode by viewModel.selectedWilayaCode.collectAsState()
    val isUrgentOnly by viewModel.isUrgentOnly.collectAsState()
    val hasDeliveryOnly by viewModel.hasDeliveryOnly.collectAsState()
    val orders by viewModel.allOrders.collectAsState()
    val detectedLocation by viewModel.detectedLocation.collectAsState()
    val isDetectingLocation by viewModel.isDetectingLocation.collectAsState()
    val isNearbyBrowsingActive by viewModel.isNearbyBrowsingActive.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.detectAndApplyUserLocation(context, autoApplyFilter = true)
        }
    }

    var showWilayaSheet by remember { mutableStateOf(false) }

    val urgentListings = remember(allListings) {
        allListings.filter { it.isUrgent }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateListingClick,
                containerColor = DzGreenPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_create_listing")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Vendre")
                    Text("Vendre / بيع", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top App Bar / Brand Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DzGreenPrimary)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = "Logo",
                                    tint = DzGreenPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "SouqDZ • سوق الجزائر",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Achat & Vente Sécurisés • 58 Wilayas",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Orders / Tracking badge shortcut
                        IconButton(
                            onClick = onOrdersTrackingClick,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .testTag("top_bar_orders_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (orders.isNotEmpty()) {
                                        Badge(containerColor = DzGold) {
                                            Text(orders.size.toString(), color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = "Commandes & Suivi",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Wilaya Selector Button on top bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showWilayaSheet = true }
                            .testTag("wilaya_selector_button"),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (detectedLocation != null && selectedWilayaCode == detectedLocation?.wilaya?.code) Icons.Default.Place else Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = DzGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                val wilayaText = if (detectedLocation != null && selectedWilayaCode == detectedLocation?.wilaya?.code) {
                                    "📍 ${detectedLocation?.cityName} (${detectedLocation?.wilaya?.nameFr})"
                                } else if (selectedWilayaCode != null) {
                                    WilayaData.formatWilayaDisplay(selectedWilayaCode!!)
                                } else {
                                    "Toute l'Algérie (58 Wilayas) / كل الولايات"
                                }
                                Text(
                                    text = wilayaText,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Quick GPS Locate button
                                IconButton(
                                    onClick = {
                                        if (LocationDetector.hasLocationPermission(context)) {
                                            viewModel.detectAndApplyUserLocation(context, autoApplyFilter = true)
                                        } else {
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (isDetectingLocation) DzGold else Color.White.copy(alpha = 0.25f))
                                        .testTag("quick_gps_locate_button")
                                ) {
                                    if (isDetectingLocation) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = "Détecter GPS",
                                            tint = if (detectedLocation != null) DzGold else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Changer",
                                    color = DzGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input"),
                        placeholder = { Text("Rechercher auto, téléphone, meuble...", fontSize = 13.sp, color = Color.DarkGray) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = DzGreenPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Effacer")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                }
            }

            // Quick Filter Chips Row (Urgent, Delivery 58w, All)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isUrgentOnly,
                        onClick = { viewModel.toggleUrgentFilter() },
                        label = { Text("🔥 Urgent / عاجل") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DzRedUrgent,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_urgent_chip")
                    )

                    FilterChip(
                        selected = hasDeliveryOnly,
                        onClick = { viewModel.toggleDeliveryFilter() },
                        label = { Text("📦 Livraison 58w") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DzGreenContainer,
                            selectedLabelColor = DzGreenDark
                        ),
                        modifier = Modifier.testTag("filter_delivery_chip")
                    )

                    if (selectedWilayaCode != null) {
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.selectWilaya(null) },
                            label = { Text("Wilaya ${selectedWilayaCode} ✕") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DzGreenDark,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Categories Horizontal Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PaddingValues(horizontal = 16.dp).let {
                        Text(
                            text = "Catégories / الأقسام",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(CategoryData.allCategories) { category ->
                            val isSelected = selectedCategory == category.id
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.selectCategory(category.id) }
                                    .padding(6.dp)
                                    .testTag("category_chip_${category.id}"),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) DzGreenPrimary else MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = category.nameFr,
                                        tint = if (isSelected) Color.White else DzGreenDark,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Text(
                                    text = category.nameFr,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) DzGreenPrimary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = category.nameAr,
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Urgent Deals Carousel if not filtered out
            if (urgentListings.isNotEmpty() && !isUrgentOnly) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(DzRedUrgent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "Affaires Urgentes / عروض عاجلة",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(urgentListings) { item ->
                                Box(modifier = Modifier.width(260.dp)) {
                                    ListingCard(
                                        listing = item,
                                        onClick = { onListingClick(item) },
                                        onFavoriteToggle = { viewModel.toggleFavorite(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Localized Marketplace Browsing Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("localized_browsing_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (detectedLocation != null && selectedWilayaCode == detectedLocation?.wilaya?.code) {
                            DzGreenLight
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    border = if (detectedLocation != null && selectedWilayaCode == detectedLocation?.wilaya?.code) {
                        androidx.compose.foundation.BorderStroke(1.dp, DzGreenPrimary.copy(alpha = 0.5f))
                    } else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (detectedLocation != null) DzGreenPrimary else DzGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (detectedLocation != null) Icons.Default.Place else Icons.Default.MyLocation,
                                        contentDescription = null,
                                        tint = if (detectedLocation != null) Color.White else Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (detectedLocation != null && selectedWilayaCode == detectedLocation?.wilaya?.code) {
                                            "📍 Marché Local : ${detectedLocation?.cityName}"
                                        } else if (selectedWilayaCode != null) {
                                            "📍 Wilaya sélectionnée : ${WilayaData.getWilaya(selectedWilayaCode!!)?.nameFr ?: "Wilaya $selectedWilayaCode"}"
                                        } else {
                                            "📍 Détection de Ville & Marché Local"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (detectedLocation != null && selectedWilayaCode == detectedLocation?.wilaya?.code) DzGreenDark else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (detectedLocation != null && selectedWilayaCode == detectedLocation?.wilaya?.code) {
                                            "Annonces dans la Wilaya de ${detectedLocation?.wilaya?.nameFr} (${detectedLocation?.wilaya?.code}) • Remise en main propre"
                                        } else {
                                            "Détectez votre ville pour voir les annonces immédiates autour de vous"
                                        },
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // Action Buttons / GPS Trigger & Reset
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (LocationDetector.hasLocationPermission(context)) {
                                        viewModel.detectAndApplyUserLocation(context, autoApplyFilter = true)
                                    } else {
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                enabled = !isDetectingLocation,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DzGreenPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_detect_gps_city")
                            ) {
                                if (isDetectingLocation) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Détection...", fontSize = 12.sp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (detectedLocation != null) "Actualiser GPS" else "Détecter ma Ville (GPS)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (selectedWilayaCode != null) {
                                OutlinedButton(
                                    onClick = { viewModel.clearLocationFilter() },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("btn_reset_wilaya_filter")
                                ) {
                                    Text("Toute l'Algérie (58W)", fontSize = 11.sp)
                                }
                            }
                        }

                        // Quick Simulated Algerian Cities row for instant testing and manual localization
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Villes populaires / مدن سريعة :",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val popularCities = listOf(
                                    Triple(16, "Alger Centre", "16 - Alger"),
                                    Triple(31, "Oran Ville", "31 - Oran"),
                                    Triple(25, "Constantine", "25 - Constantine"),
                                    Triple(19, "Sétif Ville", "19 - Sétif"),
                                    Triple(23, "Annaba", "23 - Annaba"),
                                    Triple(9, "Blida", "09 - Blida")
                                )
                                popularCities.forEach { (code, city, label) ->
                                    val isCurrent = selectedWilayaCode == code
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                viewModel.setSimulatedLocation(code, city)
                                            }
                                            .testTag("city_quick_chip_$code"),
                                        color = if (isCurrent) DzGreenPrimary else Color.White,
                                        border = if (!isCurrent) androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray) else null
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) Color.White else Color.DarkGray,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section Title for Main Feed
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Toutes les Annonces (${listings.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (selectedCategory != null) {
                        Text(
                            text = "Réinitialiser",
                            color = DzGreenPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { viewModel.selectCategory(null) }
                        )
                    }
                }
            }

            // Listings Grid / Feed
            if (listings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Aucune annonce trouvée",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Essayez d'élargir votre recherche ou de changer de wilaya.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                items(listings) { item ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ListingCard(
                            listing = item,
                            onClick = { onListingClick(item) },
                            onFavoriteToggle = { viewModel.toggleFavorite(item) }
                        )
                    }
                }
            }
        }
    }

    if (showWilayaSheet) {
        AlgerianWilayaBottomSheet(
            selectedWilayaCode = selectedWilayaCode,
            onWilayaSelected = { wilaya ->
                viewModel.selectWilaya(wilaya?.code)
            },
            onDismissRequest = { showWilayaSheet = false },
            viewModel = viewModel
        )
    }
}
