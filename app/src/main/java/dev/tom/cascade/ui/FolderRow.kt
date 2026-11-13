package dev.tom.cascade.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.tom.cascade.ui.theme.LocalHomeStyle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.tom.cascade.model.AppEntry
import dev.tom.cascade.model.Folder

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderRow(
    folder: Folder,
    apps: List<AppEntry>,
    iconFor: (AppEntry) -> ImageBitmap?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 20.dp, vertical = LocalHomeStyle.current.rowPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FolderPreview(apps = apps.take(4), iconFor = iconFor)
        Spacer(Modifier.width(LocalHomeStyle.current.iconGap))
        Text(
            text = LocalHomeStyle.current.label(folder.name),
            style = LocalHomeStyle.current.appLabel,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FolderPreview(apps: List<AppEntry>, iconFor: (AppEntry) -> ImageBitmap?) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (apps.isEmpty()) {
            Box(
                Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                apps.chunked(2).forEach { rowApps ->
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        rowApps.forEach { app ->
                            val icon = remember(app.key) { iconFor(app) }
                            AppIcon(icon = icon, label = app.label, sizeDp = 16)
                        }
                    }
                }
            }
        }
    }
}
