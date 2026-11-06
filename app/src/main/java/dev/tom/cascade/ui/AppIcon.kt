package dev.tom.cascade.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun AppIcon(
    icon: ImageBitmap?,
    label: String,
    sizeDp: Int,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(
        when {
            sizeDp >= 48 -> 16
            sizeDp >= 32 -> 12
            else -> 4
        }.dp,
    )
    if (icon != null) {
        Image(
            bitmap = icon,
            contentDescription = null,
            modifier = modifier.size(sizeDp.dp).clip(shape),
        )
    } else {
        Box(
            modifier = modifier
                .size(sizeDp.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .semantics { contentDescription = label },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label.trim().let { trimmed ->
                    if (trimmed.isEmpty()) "?" else String(Character.toChars(trimmed.codePointAt(0)))
                }.uppercase(),
                style = if (sizeDp >= 40) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.labelSmall
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
