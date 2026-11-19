package com.example.data.model

enum class DeliveryCarrier(
    val displayName: String,
    val hubName: String,
    val trackingPrefix: String,
    val avgDays: Int
) {
    YALIDINE("Yalidine Express", "Centre de Tri Yalidine Oued Smar", "YAL-DZ", 2),
    ZR_EXPRESS("ZR Express", "Hub Principal ZR Baraki", "ZR-DZ", 2),
    EMS_POSTE("EMS Algérie Poste", "Centre Colis Postaux Birtouta", "DZ-EMS", 3),
    MAYSTRO("Maystro Delivery", "Plateforme Urbaine Maystro", "MYS-DZ", 1)
}

enum class DeliveryServiceType(val labelFr: String, val labelAr: String) {
    STOP_DESK("Stop Desk (Bureau)", "استلام من المكتب"),
    HOME_DELIVERY("À Domicile", "توصيل للمنزل")
}

enum class DeliveryStatus(val labelFr: String, val labelAr: String, val stepIndex: Int) {
    ORDER_CONFIRMED("Commande Confirmée", "تم تأكيد الطلب", 0),
    PACKAGE_PICKED_UP("Colis Récupéré", "تم استلام الطرد من البائع", 1),
    AT_SORTING_HUB("En transit au centre de tri", "في مركز الفرز الولائي", 2),
    IN_TRANSIT_WILAYA("En route vers Wilaya", "في الطريق إلى ولاية الوصول", 3),
    OUT_FOR_DELIVERY("En cours de livraison", "خرج للتوصيل مع الموزع", 4),
    DELIVERED_INSPECTED("Livré & Inspecté", "تم الاستلام والمعاينة بنجاح", 5),
    RETURNED("Retourné", "تم إرجاع الطرد", -1)
}

data class TrackingCheckpoint(
    val timestamp: Long,
    val location: String,
    val status: DeliveryStatus,
    val description: String
)
