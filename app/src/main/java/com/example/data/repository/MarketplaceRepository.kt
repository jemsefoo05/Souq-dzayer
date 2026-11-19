package com.example.data.repository

import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.ListingDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ListingEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.model.DeliveryCarrier
import com.example.data.model.DeliveryServiceType
import com.example.data.model.DeliveryStatus
import com.example.data.model.EscrowStatus
import com.example.data.model.PaymentType
import com.example.data.model.TrackingCheckpoint
import com.example.data.model.WilayaData
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class MarketplaceRepository(
    private val listingDao: ListingDao,
    private val orderDao: OrderDao,
    private val chatDao: ChatDao
) {
    val allListings: Flow<List<ListingEntity>> = listingDao.getAllListings()
    val favoriteListings: Flow<List<ListingEntity>> = listingDao.getFavoriteListings()
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()

    fun getListingById(id: String): Flow<ListingEntity?> = listingDao.getListingById(id)
    fun getOrderById(orderId: String): Flow<OrderEntity?> = orderDao.getOrderById(orderId)
    fun getOrderByTracking(trackingNumber: String): Flow<OrderEntity?> = orderDao.getOrderByTracking(trackingNumber)
    fun getChatMessages(listingId: String): Flow<List<ChatMessageEntity>> = chatDao.getMessagesForListing(listingId)

    suspend fun toggleFavorite(id: String, isFav: Boolean) = listingDao.toggleFavorite(id, isFav)
    suspend fun incrementViews(id: String) = listingDao.incrementViews(id)
    suspend fun insertListing(listing: ListingEntity) = listingDao.insertListing(listing)
    suspend fun deleteListing(id: String) = listingDao.deleteListingById(id)

    suspend fun sendChatMessage(
        listingId: String,
        senderId: String,
        senderName: String,
        messageText: String,
        isOffer: Boolean = false,
        offerAmountDzd: Long? = null
    ) {
        val entity = ChatMessageEntity(
            listingId = listingId,
            senderId = senderId,
            senderName = senderName,
            messageText = messageText,
            isOffer = isOffer,
            offerAmountDzd = offerAmountDzd,
            offerStatus = if (isOffer) "PENDING" else null
        )
        chatDao.insertMessage(entity)
    }

    suspend fun updateOfferStatus(messageId: Long, status: String) {
        chatDao.updateOfferStatus(messageId, status)
    }

    suspend fun createOrder(
        listing: ListingEntity,
        buyerName: String,
        buyerPhone: String,
        buyerWilayaCode: Int,
        buyerCommune: String,
        buyerAddressLine: String,
        paymentType: PaymentType,
        carrier: DeliveryCarrier,
        serviceType: DeliveryServiceType,
        paymentReference: String
    ): OrderEntity {
        val buyerWilaya = WilayaData.getWilaya(buyerWilayaCode)
        val deliveryFee = if (serviceType == DeliveryServiceType.HOME_DELIVERY) {
            buyerWilaya?.defaultDeliveryFeeHome ?: listing.deliveryHomeFee
        } else {
            buyerWilaya?.defaultDeliveryFeeDesk ?: listing.deliveryDeskFee
        }

        val totalAmount = listing.priceDzd + deliveryFee
        val randomDigits = (100000..999999).random()
        val trackingNumber = "${carrier.trackingPrefix}-$buyerWilayaCode-$randomDigits"
        val orderId = "ORD-DZ-${(1000..9999).random()}"
        val pinCode = (1000..9999).random().toString()

        val initialCheckpoints = JSONArray().apply {
            put(JSONObject().apply {
                put("timestamp", System.currentTimeMillis())
                put("location", "${listing.wilayaName} (${listing.communeName})")
                put("status", DeliveryStatus.ORDER_CONFIRMED.name)
                put("description", "Commande enregistrée et sécurisée avec succès. En attente de remise au livreur ${carrier.displayName}.")
            })
        }

        val escrowStatus = when (paymentType) {
            PaymentType.BARIDIMOB, PaymentType.EDAHABIA_CIB -> EscrowStatus.HELD_IN_ESCROW
            PaymentType.CASH_ON_DELIVERY -> EscrowStatus.UNPAID
        }

        val order = OrderEntity(
            orderId = orderId,
            listingId = listing.id,
            itemTitle = listing.title,
            itemPriceDzd = listing.priceDzd,
            deliveryFeeDzd = deliveryFee,
            totalAmountDzd = totalAmount,
            itemImageUrl = listing.imageUrl,
            sellerName = listing.sellerName,
            sellerPhone = listing.sellerPhone,
            sellerBaridiRip = listing.sellerBaridiRip,
            buyerName = buyerName,
            buyerPhone = buyerPhone,
            buyerWilayaCode = buyerWilayaCode,
            buyerWilayaName = buyerWilaya?.nameFr ?: "Wilaya $buyerWilayaCode",
            buyerCommune = buyerCommune,
            buyerAddressLine = buyerAddressLine,
            paymentType = paymentType,
            paymentReference = paymentReference,
            escrowStatus = escrowStatus,
            carrier = carrier,
            serviceType = serviceType,
            trackingNumber = trackingNumber,
            deliveryStatus = DeliveryStatus.ORDER_CONFIRMED,
            pinSecurityCode = pinCode,
            checkpointsJson = initialCheckpoints.toString(),
            estimatedDeliveryDays = carrier.avgDays
        )

        orderDao.insertOrder(order)
        return order
    }

    suspend fun advanceDeliveryStep(order: OrderEntity) {
        val currentCheckpoints = parseCheckpoints(order.checkpointsJson).toMutableList()
        val nextStatus = when (order.deliveryStatus) {
            DeliveryStatus.ORDER_CONFIRMED -> DeliveryStatus.PACKAGE_PICKED_UP
            DeliveryStatus.PACKAGE_PICKED_UP -> DeliveryStatus.AT_SORTING_HUB
            DeliveryStatus.AT_SORTING_HUB -> DeliveryStatus.IN_TRANSIT_WILAYA
            DeliveryStatus.IN_TRANSIT_WILAYA -> DeliveryStatus.OUT_FOR_DELIVERY
            DeliveryStatus.OUT_FOR_DELIVERY -> DeliveryStatus.DELIVERED_INSPECTED
            DeliveryStatus.DELIVERED_INSPECTED -> DeliveryStatus.DELIVERED_INSPECTED
            DeliveryStatus.RETURNED -> DeliveryStatus.RETURNED
        }

        val location = when (nextStatus) {
            DeliveryStatus.PACKAGE_PICKED_UP -> "Agence ${order.carrier.displayName} - ${order.sellerName}"
            DeliveryStatus.AT_SORTING_HUB -> order.carrier.hubName
            DeliveryStatus.IN_TRANSIT_WILAYA -> "Centre Régional ${order.buyerWilayaName}"
            DeliveryStatus.OUT_FOR_DELIVERY -> "${order.buyerWilayaName} - ${order.buyerCommune} (Livreur en route)"
            DeliveryStatus.DELIVERED_INSPECTED -> "${order.buyerAddressLine}, ${order.buyerCommune}"
            else -> order.buyerWilayaName
        }

        val desc = when (nextStatus) {
            DeliveryStatus.PACKAGE_PICKED_UP -> "Le colis a été scellé et récupéré auprès du vendeur."
            DeliveryStatus.AT_SORTING_HUB -> "Arrivé au hub de tri principal. Vérification du code-barres effectuée."
            DeliveryStatus.IN_TRANSIT_WILAYA -> "Expédié par transport express vers la wilaya de destination (${order.buyerWilayaName})."
            DeliveryStatus.OUT_FOR_DELIVERY -> "Le livreur a pris en charge votre colis pour remise en main propre."
            DeliveryStatus.DELIVERED_INSPECTED -> "Colis remis au destinataire et inspecté avec succès."
            else -> ""
        }

        currentCheckpoints.add(
            TrackingCheckpoint(
                timestamp = System.currentTimeMillis(),
                location = location,
                status = nextStatus,
                description = desc
            )
        )

        val updatedJson = serializeCheckpoints(currentCheckpoints)
        orderDao.updateDeliveryStatus(order.orderId, nextStatus, updatedJson)

        if (nextStatus == DeliveryStatus.DELIVERED_INSPECTED && order.paymentType == PaymentType.CASH_ON_DELIVERY) {
            orderDao.updateEscrowStatus(order.orderId, EscrowStatus.RELEASED_TO_SELLER)
        }
    }

    suspend fun releaseEscrowFunds(orderId: String) {
        orderDao.updateEscrowStatus(orderId, EscrowStatus.RELEASED_TO_SELLER)
    }

    fun parseCheckpoints(jsonStr: String): List<TrackingCheckpoint> {
        val list = mutableListOf<TrackingCheckpoint>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TrackingCheckpoint(
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        location = obj.optString("location", ""),
                        status = try {
                            DeliveryStatus.valueOf(obj.optString("status", DeliveryStatus.ORDER_CONFIRMED.name))
                        } catch (e: Exception) {
                            DeliveryStatus.ORDER_CONFIRMED
                        },
                        description = obj.optString("description", "")
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        return list
    }

    private fun serializeCheckpoints(list: List<TrackingCheckpoint>): String {
        val array = JSONArray()
        list.forEach { cp ->
            array.put(JSONObject().apply {
                put("timestamp", cp.timestamp)
                put("location", cp.location)
                put("status", cp.status.name)
                put("description", cp.description)
            })
        }
        return array.toString()
    }

    suspend fun checkAndSeedInitialData() {
        if (listingDao.getCount() > 0) return

        val sampleListings = listOf(
            ListingEntity(
                id = "item-1",
                title = "Volkswagen Golf 8 R-Line 2.0 TDI 2023",
                description = "Golf 8 R-Line importée d'Allemagne, boîte automatique DSG7, toit ouvrant panoramique, tableau digital cockpit pro, jantes 18 Bergamo, sièges sport alcantara. 00 retouches, carnet d'entretien à jour. Vente ou échange possible.",
                priceDzd = 6850000,
                isNegotiable = true,
                categoryId = "vehicles",
                subcategory = "Voitures",
                condition = "Comme neuf",
                wilayaCode = 16,
                wilayaName = "Alger",
                communeName = "Hydra",
                sellerName = "Mourad Auto Plus",
                sellerPhone = "0550 12 34 56",
                sellerRating = 4.9f,
                sellerVerified = true,
                sellerBaridiRip = "00799999001234567890",
                imageUrl = "img_golf8",
                deliverySupported = true,
                deliveryCarriersJson = "Yalidine,ZR Express",
                deliveryHomeFee = 1500,
                deliveryDeskFee = 800,
                isUrgent = true,
                isFeatured = true,
                viewsCount = 1420
            ),
            ListingEntity(
                id = "item-2",
                title = "iPhone 15 Pro Max 256GB Titane Naturel",
                description = "iPhone 15 Pro Max original, batterie 100%, avec boîte et câble d'origine. Débloqué tous opérateurs (Djezzy, Mobilis, Ooredoo). Facture d'achat disponible. Possibilité de livraison express 58 wilayas avec garantie Daman DZ.",
                priceDzd = 238000,
                isNegotiable = true,
                categoryId = "phones",
                subcategory = "Smartphones",
                condition = "Comme neuf",
                wilayaCode = 31,
                wilayaName = "Oran",
                communeName = "Bir El Djir",
                sellerName = "Sofiane iStore",
                sellerPhone = "0661 98 76 54",
                sellerRating = 4.8f,
                sellerVerified = true,
                sellerBaridiRip = "00799999002345678901",
                imageUrl = "img_iphone15",
                deliverySupported = true,
                deliveryCarriersJson = "Yalidine,ZR Express,EMS Algérie Poste",
                deliveryHomeFee = 650,
                deliveryDeskFee = 400,
                isUrgent = false,
                isFeatured = true,
                viewsCount = 890
            ),
            ListingEntity(
                id = "item-3",
                title = "Karakou Algérois Traditionnel Brodé au Majboud & Chaara",
                description = "Authentique Karakou algérois fait main en velours de soie véritable bleu nuit, broderie traditionnelle au fil d'or pur (Fetla & Medjboud), accompagné de sa Seroual Mdouwer en satin duchesse. Idéal pour mariages et fêtes traditionnelles.",
                priceDzd = 65000,
                isNegotiable = false,
                categoryId = "fashion",
                subcategory = "Robes & Karakou",
                condition = "Neuf",
                wilayaCode = 13,
                wilayaName = "Tlemcen",
                communeName = "Mansourah",
                sellerName = "Dar El Caftan & Karakou",
                sellerPhone = "0770 45 67 89",
                sellerRating = 5.0f,
                sellerVerified = true,
                sellerBaridiRip = "00799999003456789012",
                imageUrl = "img_karakou",
                deliverySupported = true,
                deliveryCarriersJson = "Yalidine,EMS Algérie Poste",
                deliveryHomeFee = 700,
                deliveryDeskFee = 450,
                isUrgent = false,
                isFeatured = true,
                viewsCount = 530
            ),
            ListingEntity(
                id = "item-4",
                title = "Huile d'Olive Extra Vierge Kabylie Pure (Bidon 5L)",
                description = "Huile d'olive biologique récolte de la saison des montagnes de Djurdjura, première pression à froid artisanale, acidité inférieure à 0.4%. Goût fruité intense, sans aucun additif. Certifiée qualité supérieure.",
                priceDzd = 6200,
                isNegotiable = false,
                categoryId = "artisanal",
                subcategory = "Huile d'olive pure",
                condition = "Neuf",
                wilayaCode = 15,
                wilayaName = "Tizi Ouzou",
                communeName = "Larbaâ Nath Irathen",
                sellerName = "Huilerie Traditionnelle Aït",
                sellerPhone = "0555 33 22 11",
                sellerRating = 4.95f,
                sellerVerified = true,
                sellerBaridiRip = "00799999004567890123",
                imageUrl = "img_olive_oil",
                deliverySupported = true,
                deliveryCarriersJson = "Yalidine,ZR Express",
                deliveryHomeFee = 600,
                deliveryDeskFee = 400,
                isUrgent = false,
                isFeatured = true,
                viewsCount = 1120
            ),
            ListingEntity(
                id = "item-5",
                title = "Dattes Deglet Nour Biskra - Boîte Cadeau 5KG",
                description = "Dattes royales Deglet Nour de Tolga (Biskra) 1ère qualité branchée, transparentes couleur miel, très tendres et moelleuses. Emballage sous vide pour conservation longue durée.",
                priceDzd = 4800,
                isNegotiable = false,
                categoryId = "artisanal",
                subcategory = "Dattes Deglet Nour",
                condition = "Neuf",
                wilayaCode = 7,
                wilayaName = "Biskra",
                communeName = "Tolga",
                sellerName = "Oasis Dattes Tolga",
                sellerPhone = "0663 21 43 65",
                sellerRating = 4.9f,
                sellerVerified = true,
                sellerBaridiRip = "00799999005678901234",
                imageUrl = "img_dates",
                deliverySupported = true,
                deliveryCarriersJson = "Yalidine,ZR Express,EMS Algérie Poste",
                deliveryHomeFee = 650,
                deliveryDeskFee = 450,
                isUrgent = false,
                isFeatured = false,
                viewsCount = 680
            ),
            ListingEntity(
                id = "item-6",
                title = "PlayStation 5 Slim 1TB + 2 Manettes DualSense + FC 25",
                description = "PS5 Slim neuve sous emballage scellé avec 2 manettes originales et jeu EA Sports FC 25 inclus. Garantie 1 an magasin. Livraison rapide assurée avec suivi Yalidine.",
                priceDzd = 114000,
                isNegotiable = true,
                categoryId = "gaming",
                subcategory = "PlayStation 5 / PS4",
                condition = "Neuf",
                wilayaCode = 25,
                wilayaName = "Constantine",
                communeName = "El Khroub",
                sellerName = "Gamer Zone DZ",
                sellerPhone = "0771 88 99 00",
                sellerRating = 4.85f,
                sellerVerified = true,
                sellerBaridiRip = "00799999006789012345",
                imageUrl = "img_ps5",
                deliverySupported = true,
                deliveryCarriersJson = "Yalidine,ZR Express",
                deliveryHomeFee = 650,
                deliveryDeskFee = 450,
                isUrgent = true,
                isFeatured = true,
                viewsCount = 940
            ),
            ListingEntity(
                id = "item-7",
                title = "Climatiseur Brandt Inverter 18000 BTU Tropicalisé",
                description = "Climatiseur Brandt Inverter chaud/froid gaz R410a, compresseur tropicalisé résistant jusqu'à 55°C, mode Eco silencieux, filtration antibactérienne. Avec garantie Brandt Algérie 24 mois.",
                priceDzd = 92000,
                isNegotiable = false,
                categoryId = "home",
                subcategory = "Climatiseurs & Chauffages",
                condition = "Neuf",
                wilayaCode = 19,
                wilayaName = "Sétif",
                communeName = "El Eulma",
                sellerName = "Électro Sétif Marché",
                sellerPhone = "0550 77 66 55",
                sellerRating = 4.75f,
                sellerVerified = true,
                sellerBaridiRip = "00799999007890123456",
                imageUrl = "img_ac",
                deliverySupported = true,
                deliveryCarriersJson = "ZR Express,EMS Algérie Poste",
                deliveryHomeFee = 1200,
                deliveryDeskFee = 800,
                isUrgent = false,
                isFeatured = false,
                viewsCount = 470
            ),
            ListingEntity(
                id = "item-8",
                title = "Appartement F4 Vue sur Mer - Chéraga Alger",
                description = "Superbe appartement F4 haut standing 135 m² avec acte notarié et livret foncier. Cuisine équipée italienne, suite parentale, chauffage central, climatisation centralisée, garage sous-sol avec box privé.",
                priceDzd = 32000000,
                isNegotiable = true,
                categoryId = "real_estate",
                subcategory = "Appartements F3/F4",
                condition = "Neuf",
                wilayaCode = 16,
                wilayaName = "Alger",
                communeName = "Chéraga",
                sellerName = "Agence Immobilière El Bahdja",
                sellerPhone = "0660 11 22 33",
                sellerRating = 5.0f,
                sellerVerified = true,
                sellerBaridiRip = "00799999008901234567",
                imageUrl = "img_apartment",
                deliverySupported = false,
                deliveryCarriersJson = "Hand-to-Hand",
                deliveryHomeFee = 0,
                deliveryDeskFee = 0,
                isUrgent = false,
                isFeatured = true,
                viewsCount = 2100
            )
        )
        listingDao.insertAllListings(sampleListings)

        // Seed a sample order with live tracking checkpoints to showcase the delivery tracker
        val sampleOrderCheckpoints = JSONArray().apply {
            put(JSONObject().apply {
                put("timestamp", System.currentTimeMillis() - 86400000 * 2)
                put("location", "Oran (Bir El Djir)")
                put("status", DeliveryStatus.ORDER_CONFIRMED.name)
                put("description", "Paiement BaridiMob vérifié avec succès. Fonds retenus sous séquestre sécurisé DZ Daman.")
            })
            put(JSONObject().apply {
                put("timestamp", System.currentTimeMillis() - 86400000)
                put("location", "Hub Yalidine Oran Es-Senia")
                put("status", DeliveryStatus.PACKAGE_PICKED_UP.name)
                put("description", "Colis expédié par le vendeur et scellé avec code-barres.")
            })
            put(JSONObject().apply {
                put("timestamp", System.currentTimeMillis() - 3600000 * 8)
                put("location", "Centre de Tri Yalidine Oued Smar - Alger")
                put("status", DeliveryStatus.AT_SORTING_HUB.name)
                put("description", "En cours de tri pour distribution vers Wilaya 16 (Alger).")
            })
            put(JSONObject().apply {
                put("timestamp", System.currentTimeMillis() - 3600000 * 2)
                put("location", "Agence Yalidine Chéraga (Alger)")
                put("status", DeliveryStatus.OUT_FOR_DELIVERY.name)
                put("description", "Colis en route avec le livreur Karim (0552 44 33 22). Préparez votre code PIN de confirmation.")
            })
        }

        val sampleOrder = OrderEntity(
            orderId = "ORD-DZ-8941",
            listingId = "item-2",
            itemTitle = "iPhone 15 Pro Max 256GB Titane Naturel",
            itemPriceDzd = 238000,
            deliveryFeeDzd = 500,
            totalAmountDzd = 238500,
            itemImageUrl = "img_iphone15",
            sellerName = "Sofiane iStore",
            sellerPhone = "0661 98 76 54",
            sellerBaridiRip = "00799999002345678901",
            buyerName = "Amine Benali",
            buyerPhone = "0554 12 87 65",
            buyerWilayaCode = 16,
            buyerWilayaName = "Alger",
            buyerCommune = "Chéraga",
            buyerAddressLine = "Résidence El Wouroud, Bâtiment C, N° 12",
            paymentType = PaymentType.BARIDIMOB,
            paymentReference = "BMOB-TX-998271034",
            escrowStatus = EscrowStatus.HELD_IN_ESCROW,
            carrier = DeliveryCarrier.YALIDINE,
            serviceType = DeliveryServiceType.HOME_DELIVERY,
            trackingNumber = "YAL-DZ-16-784920",
            deliveryStatus = DeliveryStatus.OUT_FOR_DELIVERY,
            pinSecurityCode = "5842",
            checkpointsJson = sampleOrderCheckpoints.toString(),
            estimatedDeliveryDays = 2
        )
        orderDao.insertOrder(sampleOrder)

        // Seed initial sample chat for item-2
        val sampleChat = listOf(
            ChatMessageEntity(
                listingId = "item-2",
                senderId = "buyer",
                senderName = "Amine",
                messageText = "Salam alaykoum kho, l'iPhone est toujours disponible ? batterie 100% officielle ?",
                timestamp = System.currentTimeMillis() - 7200000
            ),
            ChatMessageEntity(
                listingId = "item-2",
                senderId = "seller",
                senderName = "Sofiane iStore",
                messageText = "Wa alaykoum salam! Oui kho encore disponible, 100% officiel jamais ouvert avec facture et garantie.",
                timestamp = System.currentTimeMillis() - 7000000
            ),
            ChatMessageEntity(
                listingId = "item-2",
                senderId = "buyer",
                senderName = "Amine",
                messageText = "Tse3edni fih à 235,000 DA avec livraison Yalidine vers Alger Chéraga ?",
                isOffer = true,
                offerAmountDzd = 235000,
                offerStatus = "ACCEPTED",
                timestamp = System.currentTimeMillis() - 6500000
            ),
            ChatMessageEntity(
                listingId = "item-2",
                senderId = "seller",
                senderName = "Sofiane iStore",
                messageText = "Mabrouk alik kho, j'ai accepté votre offre. Vous pouvez procéder au paiement sécurisé BaridiMob ou CIB avec protection DZ Daman.",
                timestamp = System.currentTimeMillis() - 6200000
            )
        )
        sampleChat.forEach { chatDao.insertMessage(it) }
    }
}
