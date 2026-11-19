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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryStatus
import com.example.ui.components.DeliveryTrackerView
import com.example.ui.theme.DzGreenContainer
import com.example.ui.theme.DzGreenDark
import com.example.ui.theme.DzGreenPrimary
import com.example.ui.viewmodel.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersAndTrackingScreen(
    viewModel: MarketplaceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.allOrders.collectAsState()
    var searchTrackingCode by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableStateOf(0) }

    val filteredOrders = remember(orders, searchTrackingCode, selectedTabIndex) {
        val base = if (searchTrackingCode.isBlank()) {
            orders
        } else {
            orders.filter {
                it.trackingNumber.contains(searchTrackingCode, ignoreCase = true) ||
                        it.orderId.contains(searchTrackingCode, ignoreCase = true) ||
                        it.itemTitle.contains(searchTrackingCode, ignoreCase = true)
            }
        }
        if (selectedTabIndex == 0) {
            base.filter { it.deliveryStatus != DeliveryStatus.DELIVERED_INSPECTED }
        } else {
            base.filter { it.deliveryStatus == DeliveryStatus.DELIVERED_INSPECTED }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Suivi des Livraisons / تتبع الطرود", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("orders_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar for tracking code
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchTrackingCode,
                    onValueChange = { searchTrackingCode = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tracking_code_search_input"),
                    placeholder = { Text("Rechercher N° de suivi (ex: YAL-DZ-16-784920)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = DzGreenPrimary)
                    },
                    trailingIcon = {
                        if (searchTrackingCode.isNotEmpty()) {
                            IconButton(onClick = { searchTrackingCode = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Effacer")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Tabs for Active vs Completed
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = DzGreenPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = DzGreenPrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            "En Cours (${orders.count { it.deliveryStatus != DeliveryStatus.DELIVERED_INSPECTED }})",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            "Livrées & Terminées (${orders.count { it.deliveryStatus == DeliveryStatus.DELIVERED_INSPECTED }})",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            // Orders list
            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(DzGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = DzGreenDark, modifier = Modifier.size(32.dp))
                        }
                        Text(
                            text = if (selectedTabIndex == 0) "Aucune livraison active en cours" else "Aucune commande livrée pour le moment",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Achetez un article avec DZ Daman pour suivre son acheminement en temps réel.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredOrders) { order ->
                        val checkpoints = viewModel.parseCheckpoints(order.checkpointsJson)
                        DeliveryTrackerView(
                            order = order,
                            checkpoints = checkpoints,
                            onAdvanceDelivery = { viewModel.advanceDelivery(order) },
                            onReleaseEscrow = { viewModel.releaseEscrow(order.orderId) }
                        )
                    }
                }
            }
        }
    }
}
