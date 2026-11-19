package com.example.data.model

data class Wilaya(
    val code: Int,
    val nameFr: String,
    val nameAr: String,
    val zone: Int, // 1: Central/Near (Alger, Blida, etc.), 2: Coastal/North, 3: High Plateaus, 4: South/Sahara
    val defaultDeliveryFeeDesk: Long,
    val defaultDeliveryFeeHome: Long,
    val communes: List<String>
)

object WilayaData {
    val allWilayas: List<Wilaya> = listOf(
        Wilaya(1, "Adrar", "أدرار", 4, 850, 1300, listOf("Adrar", "Reggane", "Timimoun", "Aoulef", "Fenoughil")),
        Wilaya(2, "Chlef", "الشلف", 2, 450, 750, listOf("Chlef", "Ténès", "Boukadir", "Oued Fodda", "El Karimia")),
        Wilaya(3, "Laghouat", "الأغواط", 3, 550, 850, listOf("Laghouat", "Aflou", "Ksar El Hirane", "Hassi R'Mel")),
        Wilaya(4, "Oum El Bouaghi", "أم البواقي", 3, 500, 800, listOf("Oum El Bouaghi", "Aïn Beïda", "Aïn M'lila", "Meskiana")),
        Wilaya(5, "Batna", "باتنة", 3, 500, 800, listOf("Batna", "Barika", "Aïn Touta", "Arris", "Merouana")),
        Wilaya(6, "Béjaïa", "بجاية", 2, 450, 750, listOf("Béjaïa", "Akbou", "El Kseur", "Amizour", "Sidi Aïch", "Tichy")),
        Wilaya(7, "Biskra", "بسكرة", 3, 550, 850, listOf("Biskra", "Tolga", "Sidi Okba", "Ouled Djellal", "Zeribet El Oued")),
        Wilaya(8, "Béchar", "بشار", 4, 800, 1200, listOf("Béchar", "Abadla", "Béni Abbès", "Kenadsa", "Taghit")),
        Wilaya(9, "Blida", "البليدة", 1, 350, 550, listOf("Blida", "Boufarik", "Ouled Yaïch", "Larbaâ", "El Affroun", "Mouzaïa")),
        Wilaya(10, "Bouira", "البويرة", 2, 450, 700, listOf("Bouira", "Lakhdaria", "Sour El Ghozlane", "Aïn Bessem", "M'Chedallah")),
        Wilaya(11, "Tamanrasset", "تمنراست", 4, 1100, 1600, listOf("Tamanrasset", "In Salah", "Abalessa", "Tazrouk")),
        Wilaya(12, "Tébessa", "تبسة", 3, 550, 850, listOf("Tébessa", "Chréa", "Bir El Ater", "El Aouinet")),
        Wilaya(13, "Tlemcen", "تلمسان", 2, 500, 800, listOf("Tlemcen", "Mansourah", "Maghnia", "Ghazaouet", "Remchi", "Chetouane")),
        Wilaya(14, "Tiaret", "تيارت", 3, 500, 800, listOf("Tiaret", "Sougueur", "Frenda", "Ksar Chellala", "Mahdia")),
        Wilaya(15, "Tizi Ouzou", "تيزي وزو", 2, 450, 700, listOf("Tizi Ouzou", "Azazga", "Draâ Ben Khedda", "Boghni", "Larbaâ Nath Irathen", "Tigzirt")),
        Wilaya(16, "Alger", "الجزائر", 1, 300, 500, listOf("Alger Centre", "Bab El Oued", "Hydra", "Chéraga", "Kouba", "Bir Mourad Raïs", "Dar El Beïda", "Hussein Dey", "Zéralda", "Rouïba")),
        Wilaya(17, "Djelfa", "الجلفة", 3, 550, 850, listOf("Djelfa", "Aïn Oussera", "Messaad", "Hassi Bahbah")),
        Wilaya(18, "Jijel", "جيجل", 2, 450, 750, listOf("Jijel", "Taher", "El Milia", "El Aouana", "Ziama Mansouriah")),
        Wilaya(19, "Sétif", "سطيف", 2, 450, 750, listOf("Sétif", "El Eulma", "Aïn Oulmene", "Aïn Arnat", "Bougaa")),
        Wilaya(20, "Saïda", "سعيدة", 3, 500, 800, listOf("Saïda", "Aïn El Hadjar", "Youb", "Sidi Boubekeur")),
        Wilaya(21, "Skikda", "سكيكدة", 2, 450, 750, listOf("Skikda", "El Harrouch", "Collo", "Azzaba", "Tamalous")),
        Wilaya(22, "Sidi Bel Abbès", "سيدي بلعباس", 2, 500, 800, listOf("Sidi Bel Abbès", "Telagh", "Sfisef", "Ben Badis")),
        Wilaya(23, "Annaba", "عنابة", 2, 450, 750, listOf("Annaba", "El Bouni", "Sidi Amar", "Berrahal", "El Hadjar")),
        Wilaya(24, "Guelma", "قالمة", 2, 450, 750, listOf("Guelma", "Oued Zenati", "Bouchegouf", "Héliopolis")),
        Wilaya(25, "Constantine", "قسنطينة", 2, 450, 750, listOf("Constantine", "El Khroub", "Aïn Smara", "Hamma Bouziane", "Didouche Mourad")),
        Wilaya(26, "Médéa", "المدية", 2, 450, 700, listOf("Médéa", "Berrouaghia", "Ksar El Boukhari", "Beni Slimane")),
        Wilaya(27, "Mostaganem", "مستغانم", 2, 450, 750, listOf("Mostaganem", "Aïn Tedeles", "Hassi Mameche", "Sidi Ali")),
        Wilaya(28, "M'Sila", "المسيلة", 3, 500, 800, listOf("M'Sila", "Bou Saâda", "Sidi Aïssa", "Magra")),
        Wilaya(29, "Mascara", "معسكر", 2, 500, 800, listOf("Mascara", "Sig", "Mohammadia", "Tighennif")),
        Wilaya(30, "Ouargla", "ورقلة", 4, 750, 1100, listOf("Ouargla", "Hassi Messaoud", "Touggourt", "Rouissat")),
        Wilaya(31, "Oran", "وهران", 1, 400, 650, listOf("Oran", "Bir El Djir", "Es Senia", "Arzew", "Aïn El Turk", "Misserghin")),
        Wilaya(32, "El Bayadh", "البيض", 4, 700, 1050, listOf("El Bayadh", "Rogassa", "Brezina", "El Abiodh Sidi Cheikh")),
        Wilaya(33, "Illizi", "إليزي", 4, 1100, 1600, listOf("Illizi", "Djanet", "In Amenas")),
        Wilaya(34, "Bordj Bou Arreridj", "برج بوعريريج", 2, 450, 750, listOf("Bordj Bou Arreridj", "Ras El Oued", "Mansoura")),
        Wilaya(35, "Boumerdès", "بومرداس", 1, 350, 550, listOf("Boumerdès", "Khemis El Khechna", "Dellys", "Boudouaou", "Zemmouri")),
        Wilaya(36, "El Tarf", "الطارف", 2, 500, 800, listOf("El Tarf", "El Kala", "Ben M'Hidi", "Drean")),
        Wilaya(37, "Tindouf", "تندوف", 4, 1200, 1700, listOf("Tindouf", "Oum El Assel")),
        Wilaya(38, "Tissemsilt", "تيسمسيلت", 3, 500, 800, listOf("Tissemsilt", "Theniet El Had", "Lardjem")),
        Wilaya(39, "El Oued", "الوادي", 4, 700, 1050, listOf("El Oued", "Djamaa", "Robbah", "Guemar")),
        Wilaya(40, "Khenchela", "خنشلة", 3, 550, 850, listOf("Khenchela", "Chechar", "Kaïs", "Bouhmama")),
        Wilaya(41, "Souk Ahras", "سوق أهراس", 3, 500, 800, listOf("Souk Ahras", "Sedrata", "M'Daourouch")),
        Wilaya(42, "Tipaza", "تيبازة", 1, 350, 550, listOf("Tipaza", "Cherchell", "Koléa", "Bou Ismaïl", "Hadout", "Fouka")),
        Wilaya(43, "Mila", "ميلة", 2, 450, 750, listOf("Mila", "Chelghoum Laïd", "Tadjenanet", "Ferdjioua")),
        Wilaya(44, "Aïn Defla", "عين الدفلى", 2, 450, 700, listOf("Aïn Defla", "Khemis Miliana", "El Attaf", "Miliana")),
        Wilaya(45, "Naâma", "النعامة", 4, 700, 1050, listOf("Naâma", "Mécheria", "Aïn Sefra")),
        Wilaya(46, "Aïn Témouchent", "عين تموشنت", 2, 500, 800, listOf("Aïn Témouchent", "Béni Saf", "Hammam Bou Hadjar")),
        Wilaya(47, "Ghardaïa", "غرداية", 4, 700, 1050, listOf("Ghardaïa", "Metlili", "El Guerrara", "Béni Isguen", "Berriane")),
        Wilaya(48, "Relizane", "غليزان", 2, 450, 750, listOf("Relizane", "Oued Rhiou", "Mazouna", "Zemmora")),
        Wilaya(49, "Timimoun", "تيميمون", 4, 850, 1300, listOf("Timimoun", "Aougrout", "Charouine")),
        Wilaya(50, "Bordj Badji Mokhtar", "برج باجي مختار", 4, 1300, 1800, listOf("Bordj Badji Mokhtar", "Timiaouine")),
        Wilaya(51, "Ouled Djellal", "أولاد جلال", 3, 550, 850, listOf("Ouled Djellal", "Sidi Khaled")),
        Wilaya(52, "Béni Abbès", "بني عباس", 4, 850, 1300, listOf("Béni Abbès", "Tabelbala", "Igli")),
        Wilaya(53, "In Salah", "عين صالح", 4, 1000, 1500, listOf("In Salah", "In Ghar")),
        Wilaya(54, "In Guezzam", "عين قزام", 4, 1400, 1900, listOf("In Guezzam", "Tin Zaouatine")),
        Wilaya(55, "Touggourt", "تقرت", 4, 700, 1050, listOf("Touggourt", "Megarine", "Taibet")),
        Wilaya(56, "Djanet", "جانت", 4, 1200, 1700, listOf("Djanet", "Bordj El Haouas")),
        Wilaya(57, "El M'Ghair", "المغير", 4, 700, 1050, listOf("El M'Ghair", "Djamaa")),
        Wilaya(58, "El Meniaa", "المنيعة", 4, 800, 1200, listOf("El Meniaa", "Hassi Gara"))
    )

    fun getWilaya(code: Int): Wilaya? = allWilayas.find { it.code == code }
    fun formatWilayaDisplay(code: Int): String {
        val w = getWilaya(code) ?: return "Wilaya $code"
        return "${String.format("%02d", w.code)} - ${w.nameFr} (${w.nameAr})"
    }
}
