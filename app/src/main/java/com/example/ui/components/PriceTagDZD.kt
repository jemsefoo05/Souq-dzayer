package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DzGoldContainer
import com.example.ui.theme.DzGoldOnContainer
import com.example.ui.theme.DzGreenPrimary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PriceTagDZD(
    priceDzd: Long,
    isNegotiable: Boolean = false,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val formatter = NumberFormat.getInstance(Locale.FRANCE)
    val formattedPrice = formatter.format(priceDzd)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "$formattedPrice DA",
            style = if (isLarge) MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = DzGreenPrimary
            ) else MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DzGreenPrimary
            )
        )

        if (isNegotiable) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(DzGoldContainer)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Négociable / تفاوض",
                    color = DzGoldOnContainer,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
