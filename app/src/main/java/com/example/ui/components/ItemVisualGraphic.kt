package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ItemVisualGraphic(
    imageKey: String,
    modifier: Modifier = Modifier,
    cornerRadius: Int = 12
) {
    val (gradient, icon, iconColor) = when (imageKey) {
        "img_golf8" -> Triple(
            Brush.linearGradient(listOf(Color(0xFF1E3C72), Color(0xFF2A5298))),
            Icons.Default.DirectionsCar,
            Color.White
        )
        "img_iphone15" -> Triple(
            Brush.linearGradient(listOf(Color(0xFF434343), Color(0xFF000000))),
            Icons.Default.PhoneIphone,
            Color(0xFFE0E0E0)
        )
        "img_karakou" -> Triple(
            Brush.linearGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFFD4AF37))),
            Icons.Default.ShoppingBag,
            Color(0xFFFFD700)
        )
        "img_olive_oil" -> Triple(
            Brush.linearGradient(listOf(Color(0xFF386641), Color(0xFF6A994E))),
            Icons.Default.Grass,
            Color(0xFFE9F5DB)
        )
        "img_dates" -> Triple(
            Brush.linearGradient(listOf(Color(0xFF582F0E), Color(0xFF7F4F24))),
            Icons.Default.LocalFlorist,
            Color(0xFFFFD166)
        )
        "img_ps5" -> Triple(
            Brush.linearGradient(listOf(Color(0xFF003087), Color(0xFF001220))),
            Icons.Default.SportsEsports,
            Color.White
        )
        "img_ac" -> Triple(
            Brush.linearGradient(listOf(Color(0xFF0077B6), Color(0xFF90E0EF))),
            Icons.Default.AcUnit,
            Color.White
        )
        "img_apartment" -> Triple(
            Brush.linearGradient(listOf(Color(0xFF2B2D42), Color(0xFF8D99AE))),
            Icons.Default.Apartment,
            Color.White
        )
        else -> Triple(
            Brush.linearGradient(listOf(Color(0xFF006C35), Color(0xFF004D25))),
            Icons.Default.Storefront,
            Color.White
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor.copy(alpha = 0.9f),
            modifier = Modifier.size(54.dp)
        )
    }
}
