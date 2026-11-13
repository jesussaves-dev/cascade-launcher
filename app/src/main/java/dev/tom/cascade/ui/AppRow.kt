package dev.tom.cascade.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import dev.tom.cascade.model.AppEntry
import dev.tom.cascade.ui.theme.LocalHomeStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppRow(
    app: AppEntry,
    icon: ImageBitmap?,
    iconSizeDp: Int,
    hasNotification: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (interactive) Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = LocalHomeStyle.current.rowPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(icon = icon, label = app.label, sizeDp = iconSizeDp)
        Spacer(Modifier.width(LocalHomeStyle.current.iconGap))
        Text(
            text = LocalHomeStyle.current.label(app.label),
            style = LocalHomeStyle.current.appLabel,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (hasNotification) {
            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(7.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
        }
    }
}
