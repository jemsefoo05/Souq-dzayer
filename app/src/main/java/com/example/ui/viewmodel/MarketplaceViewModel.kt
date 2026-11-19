package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ListingEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.model.DeliveryCarrier
import com.example.data.model.DeliveryServiceType
import com.example.data.model.EscrowStatus
import com.example.data.model.PaymentType
import com.example.data.model.WilayaData
import com.example.data.repository.MarketplaceRepository
import com.example.util.DetectedCityResult
import com.example.util.LocationDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MarketplaceViewModel(
    private val repository: MarketplaceRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    val allListings: StateFlow<List<ListingEntity>> = repository.allListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteListings: StateFlow<List<ListingEntity>> = repository.favoriteListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null)
    val selectedWilayaCode = MutableStateFlow<Int?>(null)
    val isUrgentOnly = MutableStateFlow(false)
    val hasDeliveryOnly = MutableStateFlow(false)

    // User Location and City Detection
    val detectedLocation = MutableStateFlow<DetectedCityResult?>(null)
    val isDetectingLocation = MutableStateFlow(false)
    val locationError = MutableStateFlow<String?>(null)
    val isNearbyBrowsingActive = MutableStateFlow(false)

    // Current selected item detail
    val selectedListingId = MutableStateFlow<String?>(null)
    val activeListingChatId = MutableStateFlow<String?>(null)
    val activeTrackingSearchNumber = MutableStateFlow("")

    data class FilterState(
        val query: String = "",
        val category: String? = null,
        val wilayaCode: Int? = null,
        val urgentOnly: Boolean = false,
        val deliveryOnly: Boolean = false
    )

    val filterState: StateFlow<FilterState> = combine(
        searchQuery,
        selectedCategory,
        selectedWilayaCode,
        isUrgentOnly,
        hasDeliveryOnly
    ) { query, category, wilaya, urgent, delivery ->
        FilterState(query, category, wilaya, urgent, delivery)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FilterState())

    val filteredListings: StateFlow<List<ListingEntity>> = combine(
        allListings,
        filterState
    ) { listings, filter ->
        listings.filter { item ->
            val matchesQuery = filter.query.isBlank() ||
                    item.title.contains(filter.query, ignoreCase = true) ||
                    item.description.contains(filter.query, ignoreCase = true) ||
                    item.wilayaName.contains(filter.query, ignoreCase = true) ||
                    item.communeName.contains(filter.query, ignoreCase = true)

            val matchesCategory = filter.category == null || item.categoryId == filter.category
            val matchesWilaya = filter.wilayaCode == null || item.wilayaCode == filter.wilayaCode
            val matchesUrgent = !filter.urgentOnly || item.isUrgent
            val matchesDelivery = !filter.deliveryOnly || item.deliverySupported

            matchesQuery && matchesCategory && matchesWilaya && matchesUrgent && matchesDelivery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(categoryId: String?) {
        selectedCategory.value = if (selectedCategory.value == categoryId) null else categoryId
    }

    fun selectWilaya(wilayaCode: Int?) {
        selectedWilayaCode.value = wilayaCode
        if (wilayaCode == null) {
            isNearbyBrowsingActive.value = false
        }
    }

    fun detectAndApplyUserLocation(context: Context, autoApplyFilter: Boolean = true) {
        viewModelScope.launch {
            isDetectingLocation.value = true
            locationError.value = null
            val result = LocationDetector.detectCity(context)
            isDetectingLocation.value = false
            result.onSuccess { cityResult ->
                detectedLocation.value = cityResult
                if (autoApplyFilter) {
                    selectedWilayaCode.value = cityResult.wilaya.code
                    isNearbyBrowsingActive.value = true
                }
            }.onFailure { error ->
                locationError.value = error.message ?: "Impossible de détecter votre position"
            }
        }
    }

    fun applyDetectedLocationFilter() {
        val detected = detectedLocation.value
        if (detected != null) {
            selectedWilayaCode.value = detected.wilaya.code
            isNearbyBrowsingActive.value = true
        }
    }

    fun clearLocationFilter() {
        selectedWilayaCode.value = null
        isNearbyBrowsingActive.value = false
    }

    fun setSimulatedLocation(wilayaCode: Int, cityName: String) {
        val wilaya = WilayaData.getWilaya(wilayaCode) ?: return
        detectedLocation.value = DetectedCityResult(
            cityName = cityName,
            wilaya = wilaya,
            latitude = 36.7538,
            longitude = 3.0588,
            isGpsAccurate = true,
            sourceDescription = "Ville sélectionnée: $cityName (${wilaya.nameFr})"
        )
        selectedWilayaCode.value = wilaya.code
        isNearbyBrowsingActive.value = true
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun toggleUrgentFilter() {
        isUrgentOnly.value = !isUrgentOnly.value
    }

    fun toggleDeliveryFilter() {
        hasDeliveryOnly.value = !hasDeliveryOnly.value
    }

    fun toggleFavorite(listing: ListingEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(listing.id, !listing.isFavorite)
        }
    }

    fun incrementViews(id: String) {
        viewModelScope.launch {
            repository.incrementViews(id)
        }
    }

    fun createListing(
        title: String,
        description: String,
        priceDzd: Long,
        isNegotiable: Boolean,
        categoryId: String,
        subcategory: String,
        condition: String,
        wilayaCode: Int,
        wilayaName: String,
        communeName: String,
        sellerName: String,
        sellerPhone: String,
        sellerBaridiRip: String,
        imageUrl: String,
        deliverySupported: Boolean,
        deliveryCarriers: List<String>,
        deliveryHomeFee: Long,
        deliveryDeskFee: Long,
        isUrgent: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val newId = "listing-${System.currentTimeMillis()}"
            val entity = ListingEntity(
                id = newId,
                title = title,
                description = description,
                priceDzd = priceDzd,
                isNegotiable = isNegotiable,
                categoryId = categoryId,
                subcategory = subcategory,
                condition = condition,
                wilayaCode = wilayaCode,
                wilayaName = wilayaName,
                communeName = communeName,
                sellerName = sellerName,
                sellerPhone = sellerPhone,
                sellerRating = 5.0f,
                sellerVerified = true,
                sellerBaridiRip = sellerBaridiRip,
                imageUrl = imageUrl,
                deliverySupported = deliverySupported,
                deliveryCarriersJson = deliveryCarriers.joinToString(","),
                deliveryHomeFee = deliveryHomeFee,
                deliveryDeskFee = deliveryDeskFee,
                isUrgent = isUrgent,
                isFeatured = false
            )
            repository.insertListing(entity)
            onSuccess()
        }
    }

    fun createOrder(
        listing: ListingEntity,
        buyerName: String,
        buyerPhone: String,
        buyerWilayaCode: Int,
        buyerCommune: String,
        buyerAddressLine: String,
        paymentType: PaymentType,
        carrier: DeliveryCarrier,
        serviceType: DeliveryServiceType,
        paymentReference: String,
        onOrderCreated: (OrderEntity) -> Unit
    ) {
        viewModelScope.launch {
            val order = repository.createOrder(
                listing = listing,
                buyerName = buyerName,
                buyerPhone = buyerPhone,
                buyerWilayaCode = buyerWilayaCode,
                buyerCommune = buyerCommune,
                buyerAddressLine = buyerAddressLine,
                paymentType = paymentType,
                carrier = carrier,
                serviceType = serviceType,
                paymentReference = paymentReference
            )
            onOrderCreated(order)
        }
    }

    fun advanceDelivery(order: OrderEntity) {
        viewModelScope.launch {
            repository.advanceDeliveryStep(order)
        }
    }

    fun releaseEscrow(orderId: String) {
        viewModelScope.launch {
            repository.releaseEscrowFunds(orderId)
        }
    }

    fun sendChatMessage(
        listingId: String,
        senderName: String,
        text: String,
        isOffer: Boolean = false,
        offerAmount: Long? = null
    ) {
        viewModelScope.launch {
            repository.sendChatMessage(
                listingId = listingId,
                senderId = "buyer",
                senderName = senderName,
                messageText = text,
                isOffer = isOffer,
                offerAmountDzd = offerAmount
            )
        }
    }

    fun respondToOffer(messageId: Long, accept: Boolean) {
        viewModelScope.launch {
            repository.updateOfferStatus(messageId, if (accept) "ACCEPTED" else "DECLINED")
        }
    }

    fun getChatForListing(listingId: String) = repository.getChatMessages(listingId)
    fun parseCheckpoints(json: String) = repository.parseCheckpoints(json)
}

class MarketplaceViewModelFactory(private val repository: MarketplaceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarketplaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MarketplaceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
