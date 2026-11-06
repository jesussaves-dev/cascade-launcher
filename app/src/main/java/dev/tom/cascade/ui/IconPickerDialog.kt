package dev.tom.cascade.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.tom.cascade.iconpack.PackIcon
import dev.tom.cascade.iconpack.orderIcons

@Composable
fun IconPickerDialog(
    appLabel: String,
    packageName: String,
    icons: List<PackIcon>,
    hasOverride: Boolean,
    iconFor: (PackIcon) -> ImageBitmap?,
    onPick: (PackIcon) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = orderIcons(icons, appLabel, packageName).filter { icon ->
        query.isBlank() ||
            icon.name.contains(query, ignoreCase = true) ||
            icon.drawable.contains(query, ignoreCase = true)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose icon") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    placeholder = { Text("Search icons") },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (icons.isEmpty()) {
                    Text(
                        "Icons are still loading",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                } else if (filtered.isEmpty()) {
                    Text(
                        "No icons match",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier.heightIn(max = 480.dp).padding(top = 12.dp),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(filtered, key = { it.drawable }) { icon ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { onPick(icon) }
                                    .semantics { contentDescription = icon.name }
                                    .padding(vertical = 4.dp),
                            ) {
                                AppIcon(icon = iconFor(icon), label = icon.name, sizeDp = 48)
                                Text(
                                    text = icon.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        dismissButton = {
            if (hasOverride) {
                TextButton(onClick = onReset) { Text("Reset to pack icon") }
            }
        },
    )
}
