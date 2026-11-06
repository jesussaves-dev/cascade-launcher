package dev.tom.cascade.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.ImageBitmap
import dev.tom.cascade.data.ThemeMode
import dev.tom.cascade.model.AppEntry
import dev.tom.cascade.iconpack.IconPackInfo
import dev.tom.cascade.model.Place
import dev.tom.cascade.ui.theme.AccentColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    theme: ThemeMode,
    accentIndex: Int,
    iconSizeDp: Int,
    hiddenApps: List<AppEntry>,
    iconPacks: List<IconPackInfo>,
    activeIconPack: String?,
    packIconFor: (String) -> ImageBitmap?,
    onIconPack: (String?) -> Unit,
    notificationsEnabled: Boolean,
    showPreviews: Boolean,
    onShowPreviews: (Boolean) -> Unit,
    onOpenNotificationAccess: () -> Unit,
    widgets: List<Pair<Int, String>>,
    onAddWidget: () -> Unit,
    onRemoveWidget: (Int) -> Unit,
    searchContacts: Boolean,
    onSearchContacts: (Boolean) -> Unit,
    showWeather: Boolean,
    place: Place?,
    onShowWeather: (Boolean) -> Unit,
    onPickPlace: (Place?) -> Unit,
    showVerse: Boolean,
    onShowVerse: (Boolean) -> Unit,
    onSearchPlaces: suspend (String) -> List<Place>,
    adminActive: Boolean,
    onToggleAdmin: () -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onAccent: (Int) -> Unit,
    onIconSize: (Int) -> Unit,
    onUnhide: (String) -> Unit,
    onSetDefault: () -> Unit,
    onBack: () -> Unit,
) {
    var choosingPack by remember { mutableStateOf(false) }
    var choosingCity by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            SectionTitle("Appearance")

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = theme == ThemeMode.SYSTEM, onClick = { onTheme(ThemeMode.SYSTEM) })
                Text("Follow system")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = theme == ThemeMode.LIGHT, onClick = { onTheme(ThemeMode.LIGHT) })
                Text("Light")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = theme == ThemeMode.DARK, onClick = { onTheme(ThemeMode.DARK) })
                Text("Dark")
            }

            Spacer(Modifier.height(16.dp))
            Text("Accent colour", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AccentColors.forEachIndexed { index, color ->
                    val selected = index == accentIndex
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape,
                                )
                                .clickable { onAccent(index) },
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Icon size: ${iconSizeDp}dp", style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = iconSizeDp.toFloat(),
                onValueChange = { onIconSize(it.toInt()) },
                valueRange = 24f..64f,
                steps = 9,
            )

            Spacer(Modifier.height(16.dp))
            Text("Icon pack", style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { choosingPack = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = iconPacks.firstOrNull { it.packageName == activeIconPack }?.label ?: "System icons",
                    modifier = Modifier.weight(1f),
                )
                Text("Change", color = MaterialTheme.colorScheme.primary)
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            SectionTitle("Header")
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Show weather")
                    Text(
                        if (place == null) "Choose a city first" else "On the date line",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = showWeather && place != null,
                    onCheckedChange = onShowWeather,
                    enabled = place != null,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { choosingCity = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(place?.name ?: "Choose a city", modifier = Modifier.weight(1f))
                Text(if (place == null) "Choose" else "Change", color = MaterialTheme.colorScheme.primary)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Show a daily Bible verse")
                    Text(
                        "King James Version",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = showVerse, onCheckedChange = onShowVerse)
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            SectionTitle("Gestures")
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Double-tap the clock to lock")
                    Text(
                        text = if (adminActive) "On" else "Off — needs device admin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = onToggleAdmin) {
                    Text(if (adminActive) "Turn off" else "Turn on")
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            SectionTitle("Default launcher")
            OutlinedButton(onClick = onSetDefault) { Text("Set Cascade as default") }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            SectionTitle("Notifications")
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Notification access — ${if (notificationsEnabled) "On" else "Off"}",
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onOpenNotificationAccess) { Text("Open") }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Show notification previews")
                    Text(
                        "Off shows dots only",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = showPreviews && notificationsEnabled,
                    onCheckedChange = onShowPreviews,
                    enabled = notificationsEnabled,
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            SectionTitle("Widgets")
            OutlinedButton(onClick = onAddWidget) { Text("Add widget") }
            if (widgets.isEmpty()) {
                Text(
                    "No widgets",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                widgets.forEach { (id, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(label, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onRemoveWidget(id) }) { Text("Remove") }
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            SectionTitle("Search")
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Include contacts")
                    Text(
                        "Names and numbers, in memory only",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = searchContacts, onCheckedChange = onSearchContacts)
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            SectionTitle("Hidden apps")
            if (hiddenApps.isEmpty()) {
                Text(
                    "No hidden apps",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                hiddenApps.forEach { app ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(app.label, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onUnhide(app.key) }) { Text("Unhide") }
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            SectionTitle("About")
            Text("Cascade 0.1.0")
            Text("MIT licence", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))
        }
    }

    if (choosingPack) {
        AlertDialog(
            onDismissRequest = { choosingPack = false },
            title = { Text("Icon pack") },
            text = {
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    item {
                        PackChoice(
                            label = "System icons",
                            icon = null,
                            selected = activeIconPack == null,
                            onClick = {
                                onIconPack(null)
                                choosingPack = false
                            },
                        )
                    }
                    items(iconPacks, key = { it.packageName }) { pack ->
                        PackChoice(
                            label = pack.label,
                            icon = packIconFor(pack.packageName),
                            selected = activeIconPack == pack.packageName,
                            onClick = {
                                onIconPack(pack.packageName)
                                choosingPack = false
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { choosingPack = false }) { Text("Close") }
            },
        )
    }
    if (choosingCity) {
        CityDialog(
            hasPlace = place != null,
            onSearch = onSearchPlaces,
            onPick = {
                onPickPlace(it)
                choosingCity = false
            },
            onClear = {
                onPickPlace(null)
                choosingCity = false
            },
            onDismiss = { choosingCity = false },
        )
    }
}

@Composable
private fun CityDialog(
    hasPlace: Boolean,
    onSearch: suspend (String) -> List<Place>,
    onPick: (Place) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Place>>(emptyList()) }
    var searched by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun runSearch() {
        val name = query.trim()
        if (name.isEmpty()) return
        busy = true
        scope.launch {
            results = onSearch(name)
            searched = true
            busy = false
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Weather city") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    label = { Text("City") },
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(onClick = ::runSearch, enabled = query.isNotBlank() && !busy) {
                    Text(if (busy) "Searching…" else "Search")
                }
                if (results.isNotEmpty()) {
                    LazyColumn(Modifier.heightIn(max = 260.dp)) {
                        items(results, key = { it }) { place ->
                            Text(
                                text = place.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPick(place) }
                                    .padding(vertical = 10.dp),
                            )
                        }
                    }
                } else if (searched && !busy) {
                    Text("No places found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        dismissButton = if (hasPlace) {
            { TextButton(onClick = onClear) { Text("Clear") } }
        } else {
            null
        },
    )
}

@Composable
private fun PackChoice(
    label: String,
    icon: ImageBitmap?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(icon = icon, label = label, sizeDp = 40)
        Spacer(Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f))
        RadioButton(selected = selected, onClick = onClick)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}
