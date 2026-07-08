package com.dusht.calstuff.ui.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dusht.calstuff.R
import com.dusht.calstuff.ui.theme.FontSize
import com.dusht.calstuff.ui.theme.calStuffColors

/**
 * App-wide "nothing to show yet" placeholder — used wherever a screen has no data for the
 * current context (no meals for a day, no data for a chart, etc).
 */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    imageSize: Dp = 120.dp,
) {
    val colors = MaterialTheme.calStuffColors
    Column(
        modifier = modifier.padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.img_empty_state),
            contentDescription = null,
            modifier = Modifier.size(imageSize),
        )
        Text(
            text = title,
            color = colors.textSecondary,
            fontSize = FontSize.medium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = colors.textSecondary.copy(alpha = 0.7f),
                fontSize = FontSize.small,
                textAlign = TextAlign.Center,
            )
        }
    }
}
