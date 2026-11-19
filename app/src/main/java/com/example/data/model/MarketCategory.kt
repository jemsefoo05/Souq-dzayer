package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Build
import androidx.compose.ui.graphics.vector.ImageVector

data class MarketCategory(
    val id: String,
    val nameFr: String,
    val nameAr: String,
    val icon: ImageVector,
    val subcategories: List<String>
)

object CategoryData {
    val allCategories: List<MarketCategory> = listOf(
        MarketCategory(
            id = "phones",
            nameFr = "Téléphones & Tech",
            nameAr = "هواتف وتكنولوجيا",
            icon = Icons.Default.PhoneAndroid,
            subcategories = listOf("Smartphones", "Tablettes", "PC Portables", "Smartwatches", "Accessoires", "Composants PC")
        ),
        MarketCategory(
            id = "vehicles",
            nameFr = "Véhicules",
            nameAr = "مركبات وسيارات",
            icon = Icons.Default.DirectionsCar,
            subcategories = listOf("Voitures", "Motos & Scooters", "Utilitaires & Camions", "Pièces détachées", "Accessoires Auto")
        ),
        MarketCategory(
            id = "home",
            nameFr = "Maison & Électroménager",
            nameAr = "كهرومنزلية وأثاث",
            icon = Icons.Default.Weekend,
            subcategories = listOf("Réfrigérateurs", "Téléviseurs", "Climatiseurs & Chauffages", "Meubles", "Cuisine", "Machines à laver")
        ),
        MarketCategory(
            id = "fashion",
            nameFr = "Mode & Beauté",
            nameAr = "أزياء وتجميل",
            icon = Icons.Default.ShoppingBag,
            subcategories = listOf("Robes & Karakou", "Gandouras & Djellabas", "Chaussures", "Parfums & Cosmétiques", "Montres & Bijoux", "Hommes", "Femmes")
        ),
        MarketCategory(
            id = "artisanal",
            nameFr = "Artisanat & Terroir",
            nameAr = "صناعة تقليدية ومحلية",
            icon = Icons.Default.WorkspacePremium,
            subcategories = listOf("Huile d'olive pure", "Dattes Deglet Nour", "Bijoux en argent Béni Yéni", "Poterie kabyle", "Tapis & Tissages", "Miel naturel")
        ),
        MarketCategory(
            id = "real_estate",
            nameFr = "Immobilier",
            nameAr = "عقارات",
            icon = Icons.Default.Home,
            subcategories = listOf("Appartements F3/F4", "Villas", "Terrains", "Locations vacances", "Locaux commerciaux")
        ),
        MarketCategory(
            id = "gaming",
            nameFr = "Jeux & Consoles",
            nameAr = "ألعاب وفيديو جيم",
            icon = Icons.Default.SportsEsports,
            subcategories = listOf("PlayStation 5 / PS4", "Xbox", "Nintendo Switch", "Jeux originaux", "Chaises & Setup Gaming")
        ),
        MarketCategory(
            id = "tools",
            nameFr = "Outillage & Industrie",
            nameAr = "معدات وأدوات",
            icon = Icons.Default.Build,
            subcategories = listOf("Outillage électroportatif", "Matériel agricole", "Bricolage", "Générateurs", "Sécurité")
        )
    )
}
