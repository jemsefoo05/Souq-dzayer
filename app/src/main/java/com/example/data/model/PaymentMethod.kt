package com.example.data.model

enum class PaymentType(
    val titleFr: String,
    val titleAr: String,
    val description: String,
    val isEscrowProtected: Boolean
) {
    BARIDIMOB(
        titleFr = "BaridiMob (Algérie Poste)",
        titleAr = "بريدي موب - بريد الجزائر",
        description = "Virement instantané via RIP / QR Code. Fonds bloqués sous séquestre sécurisé DZ Daman jusqu'à réception.",
        isEscrowProtected = true
    ),
    EDAHABIA_CIB(
        titleFr = "Carte Edahabia / CIB",
        titleAr = "البطاقة الذهبية / CIB",
        description = "Paiement en ligne sécurisé 3D-Secure SATIM. Protection acheteur intégrée.",
        isEscrowProtected = true
    ),
    CASH_ON_DELIVERY(
        titleFr = "Paiement à la livraison (COD)",
        titleAr = "الدفع عند الاستلام",
        description = "Payez en espèces au livreur après inspection du colis avec code PIN de vérification.",
        isEscrowProtected = false
    )
}

enum class EscrowStatus(val labelFr: String, val labelAr: String) {
    UNPAID("En attente de paiement", "في انتظار الدفع"),
    HELD_IN_ESCROW("Fonds sécurisés (DZ Daman)", "المبلغ محمي في الضمان"),
    RELEASED_TO_SELLER("Fonds virés au vendeur", "تم تحويل المبلغ للبائع"),
    REFUNDED("Remboursé à l'acheteur", "تم استرجاع المبلغ")
}
