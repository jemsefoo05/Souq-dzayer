package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Wilaya
import com.example.data.model.WilayaData
import com.example.ui.theme.DzGold
import com.example.ui.theme.DzGoldContainer
import com.example.ui.theme.DzGreenContainer
import com.example.ui.theme.DzGreenDark
import com.example.ui.theme.DzGreenLight
import com.example.ui.theme.DzGreenPrimary
import com.example.ui.viewmodel.MarketplaceViewModel
import com.example.util.LocationDetector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlgerianWilayaBottomSheet(
    selectedWilayaCode: Int?,
    onWilayaSelected: (Wilaya?) -> Unit,
    onDismissRequest: () -> Unit,
    showAllOption: Boolean = true,
    sheetState: SheetState = rememberModalBottomSheetState(),
    viewModel: MarketplaceViewModel? = null
) {
    val context = LocalContext.current
    val isDetecting = viewModel?.isDetectingLocation?.collectAsState()?.value ?: false
    val detectedLocation = viewModel?.detectedLocation?.collectAsState()?.value
    val locationError = viewModel?.locationError?.collectAsState()?.value

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel?.detectAndApplyUserLocation(context, autoApplyFilter = true)
        }
    }

    var searchQuery by remember { mutableStateOf("") }

    val filteredWilayas = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            WilayaData.allWilayas
        } else {
            WilayaData.allWilayas.filter {
                it.nameFr.contains(searchQuery, ignoreCase = true) ||
                        it.nameAr.contains(searchQuery, ignoreCase = true) ||
                        it.code.toString().contains(searchQuery)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sélectionner la Wilaya (58 Wilayas)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wilaya_search_input"),
                placeholder = { Text("Rechercher (ex: 16, Alger, Oran, وهران)") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = DzGreenPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                // GPS Auto-Detection Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isDetecting) {
                                if (LocationDetector.hasLocationPermission(context)) {
                                    viewModel?.detectAndApplyUserLocation(context, autoApplyFilter = true)
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            }
                            .testTag("gps_detect_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (detectedLocation != null) DzGreenLight else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (detectedLocation != null) androidx.compose.foundation.BorderStroke(1.dp, DzGreenPrimary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDetecting) DzGold else DzGreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDetecting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "GPS",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (detectedLocation != null) "Position : ${detectedLocation.cityName}" else "Détecter ma Ville (GPS)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (detectedLocation != null) DzGreenDark else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (detectedLocation != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(DzGreenPrimary)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("ACTIF", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                        }
                                    }
                                }
                                Text(
                                    text = if (isDetecting) "Recherche du signal GPS & Ville en cours..."
                                    else if (detectedLocation != null) "Wilaya : ${detectedLocation.wilaya.nameFr} (${detectedLocation.wilaya.code}) • Annonces proches"
                                    else "Utilisez votre position pour afficher les annonces près de chez vous",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            if (detectedLocation != null) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = DzGreenDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else if (!isDetecting) {
                                Icon(
                                    imageVector = Icons.Default.NearMe,
                                    contentDescription = null,
                                    tint = DzGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                if (showAllOption) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onWilayaSelected(null)
                                    onDismissRequest()
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            color = Color.Transparent
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedWilayaCode == null) DzGreenPrimary else DzGreenContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (selectedWilayaCode == null) Color.White else DzGreenDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Toute l'Algérie (58 Wilayas)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "كل الولايات الجزائريـة",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                if (selectedWilayaCode == null) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = DzGreenPrimary
                                    )
                                }
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }

                items(filteredWilayas) { wilaya ->
                    val isSelected = selectedWilayaCode == wilaya.code
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onWilayaSelected(wilaya)
                                onDismissRequest()
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) DzGreenPrimary else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format("%02d", wilaya.code),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = wilaya.nameFr,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = wilaya.nameAr,
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "Livraison: Bureau ${wilaya.defaultDeliveryFeeDesk} DA / Domicile ${wilaya.defaultDeliveryFeeHome} DA",
                                    fontSize = 11.sp,
                                    color = DzGreenDark
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = DzGreenPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
