package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ListingEntity
import com.example.data.model.DeliveryServiceType
import com.example.data.model.DeliveryStatus
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CreateListingScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ListingDetailScreen
import com.example.ui.screens.OrdersAndTrackingScreen
import com.example.ui.screens.PaymentCheckoutSummary
import com.example.ui.screens.PaymentConfirmationScreen
import com.example.ui.screens.ProfileWalletScreen
import com.example.ui.theme.DzGold
import com.example.ui.theme.DzGreenDark
import com.example.ui.theme.DzGreenPrimary
import com.example.ui.viewmodel.MarketplaceViewModel

enum class NavDestination(
    val titleFr: String,
    val titleAr: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Accueil", "الرئيسية", Icons.Filled.Storefront, Icons.Filled.Storefront),
    ORDERS("Suivi", "تتبع الطرود", Icons.Filled.LocalShipping, Icons.Outlined.LocalShipping),
    CREATE("Vendre", "بيع", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline),
    WALLET("Portefeuille", "المحفظة", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet)
}

@Composable
fun SouqMainContainer(
    viewModel: MarketplaceViewModel,
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf(NavDestination.HOME) }
    var selectedListingForDetail by remember { mutableStateOf<ListingEntity?>(null) }
    var selectedListingForChat by remember { mutableStateOf<ListingEntity?>(null) }
    var activePaymentConfirmationSummary by remember { mutableStateOf<PaymentCheckoutSummary?>(null) }

    val orders by viewModel.allOrders.collectAsState()
    val activeDeliveriesCount = orders.count { it.deliveryStatus != DeliveryStatus.DELIVERED_INSPECTED }

    // Screen stack management
    if (activePaymentConfirmationSummary != null) {
        val summary = activePaymentConfirmationSummary!!
        PaymentConfirmationScreen(
            checkoutSummary = summary,
            onBack = { activePaymentConfirmationSummary = null },
            onPaymentConfirmed = { paymentType, transactionRef ->
                val listing = selectedListingForDetail
                    ?: viewModel.allListings.value.firstOrNull { it.id == summary.listingId }
                    ?: viewModel.allListings.value.firstOrNull()

                if (listing != null) {
                    viewModel.createOrder(
                        listing = listing,
                        buyerName = summary.buyerName,
                        buyerPhone = summary.buyerPhone,
                        buyerWilayaCode = 16,
                        buyerCommune = summary.buyerCommune,
                        buyerAddressLine = "Centre ville",
                        paymentType = paymentType,
                        carrier = summary.carrier,
                        serviceType = DeliveryServiceType.STOP_DESK,
                        paymentReference = transactionRef,
                        onOrderCreated = {
                            activePaymentConfirmationSummary = null
                            selectedListingForDetail = null
                            currentDestination = NavDestination.ORDERS
                        }
                    )
                } else {
                    activePaymentConfirmationSummary = null
                    currentDestination = NavDestination.ORDERS
                }
            }
        )
    } else if (selectedListingForChat != null) {
        ChatScreen(
            listing = selectedListingForChat!!,
            viewModel = viewModel,
            onBack = { selectedListingForChat = null },
            onBuyDirect = {
                val item = selectedListingForChat
                selectedListingForChat = null
                selectedListingForDetail = item
            }
        )
    } else if (selectedListingForDetail != null) {
        ListingDetailScreen(
            listing = selectedListingForDetail!!,
            viewModel = viewModel,
            onBack = { selectedListingForDetail = null },
            onChatClick = { listing ->
                selectedListingForDetail = null
                selectedListingForChat = listing
            },
            onProceedToPaymentConfirmation = { summary ->
                activePaymentConfirmationSummary = summary
            },
            onOrderCompleted = { order ->
                selectedListingForDetail = null
                currentDestination = NavDestination.ORDERS
            }
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavDestination.values().forEach { destination ->
                        val isSelected = currentDestination == destination

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentDestination = destination },
                            icon = {
                                if (destination == NavDestination.ORDERS && activeDeliveriesCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = DzGold) {
                                                Text(activeDeliveriesCount.toString(), color = Color.Black, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                            contentDescription = destination.titleFr
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = destination.titleFr
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = destination.titleFr,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DzGreenPrimary,
                                selectedTextColor = DzGreenPrimary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = DzGreenPrimary.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentDestination) {
                    NavDestination.HOME -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onListingClick = { listing -> selectedListingForDetail = listing },
                            onCreateListingClick = { currentDestination = NavDestination.CREATE },
                            onOrdersTrackingClick = { currentDestination = NavDestination.ORDERS }
                        )
                    }
                    NavDestination.ORDERS -> {
                        OrdersAndTrackingScreen(
                            viewModel = viewModel,
                            onBack = { currentDestination = NavDestination.HOME }
                        )
                    }
                    NavDestination.CREATE -> {
                        CreateListingScreen(
                            viewModel = viewModel,
                            onBack = { currentDestination = NavDestination.HOME },
                            onListingCreated = { currentDestination = NavDestination.HOME }
                        )
                    }
                    NavDestination.WALLET -> {
                        ProfileWalletScreen(
                            viewModel = viewModel,
                            onBack = { currentDestination = NavDestination.HOME },
                            onOpenPaymentConfirmationDemo = {
                                activePaymentConfirmationSummary = PaymentCheckoutSummary()
                            }
                        )
                    }
                }
            }
        }
    }
}
