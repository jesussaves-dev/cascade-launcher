package dev.tom.cascade.ui

import android.os.Build
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import dev.tom.cascade.data.HomeTheme
import dev.tom.cascade.data.SwipeAction
import dev.tom.cascade.data.ThemeMode
import dev.tom.cascade.model.AppEntry
import dev.tom.cascade.iconpack.IconPackInfo
import dev.tom.cascade.model.Place
import dev.tom.cascade.ui.theme.accentChoices
import dev.tom.cascade.ui.theme.fixedMode
import dev.tom.cascade.ui.theme.style
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    theme: ThemeMode,
    homeTheme: HomeTheme,
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
    onHomeTheme: (HomeTheme) -> Unit,
    onAccent: (Int) -> Unit,
    onIconSize: (Int) -> Unit,
    themedIcons: Boolean,
    onThemedIcons: (Boolean) -> Unit,
    haptics: Boolean,
    onHaptics: (Boolean) -> Unit,
    animateGround: Boolean,
    onAnimateGround: (Boolean) -> Unit,
    swipeDown: SwipeAction,
    onSwipeDown: (SwipeAction) -> Unit,
    swipeUp: SwipeAction,
    onSwipeUp: (SwipeAction) -> Unit,
    onExportSettings: () -> Unit,
    onImportSettings: () -> Unit,
    nightTheme: HomeTheme?,
    onNightTheme: (HomeTheme?) -> Unit,
    onUnhide: (String) -> Unit,
    onSetDefault: () -> Unit,
    onBack: () -> Unit,
) {
    var choosingPack by remember { mutableStateOf(false) }
    var choosingCity by remember { mutableStateOf(false) }
    var choosingNight by remember { mutableStateOf(false) }

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

            Text("Theme", style = MaterialTheme.typography.bodyLarge)
            val feedback = LocalHapticFeedback.current
            val pickTheme = { option: HomeTheme ->
                if (option != homeTheme) feedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onHomeTheme(option)
            }
            HomeTheme.entries.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pickTheme(option) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = homeTheme == option, onClick = { pickTheme(option) })
                    Column {
                        // Each name is set in its own theme's app-label face.
                        Text(option.title, style = option.style().appLabel)
                        Text(
                            option.blurb,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { choosingNight = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("At night", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        when {
                            nightTheme == null -> "Keep ${homeTheme.title}"
                            place != null -> "${nightTheme.title}, sunset to sunrise in ${place.name}"
                            else -> "${nightTheme.title}, 9 pm to 7 am. Choose a weather city to follow sunset."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text("Change", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(16.dp))
            val fixedMode = homeTheme.fixedMode()
            Text("Light and dark", style = MaterialTheme.typography.bodyLarge)
            if (fixedMode != null) {
                Text(
                    "$fixedMode in ${homeTheme.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            listOf(
                ThemeMode.SYSTEM to "Follow system",
                ThemeMode.LIGHT to "Light",
                ThemeMode.DARK to "Dark",
            ).forEach { (mode, label) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = theme == mode,
                        onClick = { onTheme(mode) },
                        enabled = fixedMode == null,
                    )
                    Text(
                        label,
                        color = if (fixedMode == null) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Accent colour", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            val accents = homeTheme.accentChoices()
            if (accents == null) {
                Text(
                    "Set by ${homeTheme.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (accents != null) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                accents.forEachIndexed { index, color ->
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Themed icons")
                        Text(
                            "Apps with a monochrome icon take the theme's colours",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    HapticSwitch(checked = themedIcons, onCheckedChange = onThemedIcons)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Haptic feedback")
                    Text(
                        "Ticks on the alphabet rail, long presses and reordering",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HapticSwitch(checked = haptics, onCheckedChange = onHaptics)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Animated backgrounds")
                    Text(
                        "Outrun, Phosphor and Riso move gently. Off with the system's Remove animations too",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HapticSwitch(checked = animateGround, onCheckedChange = onAnimateGround)
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
                HapticSwitch(
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
                HapticSwitch(checked = showVerse, onCheckedChange = onShowVerse)
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
            SwipeChoice(
                title = "Swipe down",
                hint = "On the clock, or past the top of the list",
                selected = swipeDown,
                onSelect = onSwipeDown,
            )
            SwipeChoice(
                title = "Swipe up",
                hint = "On the clock, or past the end of the list",
                selected = swipeUp,
                onSelect = onSwipeUp,
            )

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
                HapticSwitch(
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
                HapticSwitch(checked = searchContacts, onCheckedChange = onSearchContacts)
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
            SectionTitle("Backup")
            Text(
                "Save favourites, folders, hidden apps, themes, icon choices and gestures to a file, " +
                    "or restore them on this or another phone. Widgets aren't included.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(onClick = onExportSettings) { Text("Export") }
                OutlinedButton(onClick = onImportSettings) { Text("Import") }
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            SectionTitle("About")
            Text("Cascade 1.3.0")
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
    if (choosingNight) {
        AlertDialog(
            onDismissRequest = { choosingNight = false },
            title = { Text("At night") },
            text = {
                LazyColumn(Modifier.heightIn(max = 400.dp)) {
                    item {
                        NightChoice("Keep ${homeTheme.title}", null, nightTheme == null) {
                            onNightTheme(null)
                            choosingNight = false
                        }
                    }
                    items(HomeTheme.entries.filter { it != homeTheme }, key = { it.name }) { option ->
                        NightChoice(option.title, option, nightTheme == option) {
                            onNightTheme(option)
                            choosingNight = false
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { choosingNight = false }) { Text("Close") }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeChoice(
    title: String,
    hint: String,
    selected: SwipeAction,
    onSelect: (SwipeAction) -> Unit,
) {
    val options = listOf(
        SwipeAction.NOTHING to "Nothing",
        SwipeAction.NOTIFICATIONS to "Notifications",
        SwipeAction.SEARCH to "Search",
    )
    val feedback = LocalHapticFeedback.current
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(title)
        Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            options.forEachIndexed { index, (action, label) ->
                SegmentedButton(
                    selected = selected == action,
                    onClick = {
                        if (selected != action) feedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onSelect(action)
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                ) {
                    Text(label, maxLines = 1)
                }
            }
        }
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

private val HomeTheme.title: String
    get() = when (this) {
        HomeTheme.ORIGINAL -> "Original"
        HomeTheme.INDEX -> "Index"
        HomeTheme.RUBRIC -> "Rubric"
        HomeTheme.PIXEL -> "Pixel"
        HomeTheme.TIDE -> "Tide"
        HomeTheme.NIGHT_WATCH -> "Night watch"
        HomeTheme.PAPER -> "Paper"
        HomeTheme.OUTRUN -> "Outrun"
        HomeTheme.PHOSPHOR -> "Phosphor"
        HomeTheme.POSTER -> "Poster"
        HomeTheme.BLUEPRINT -> "Blueprint"
        HomeTheme.RISO -> "Riso"
    }

private val HomeTheme.blurb: String
    get() = when (this) {
        HomeTheme.ORIGINAL -> "Cascade 1.0: Roboto, search pill, rail strip"
        HomeTheme.INDEX -> "A book's index: tall clock, serif names, large initials"
        HomeTheme.RUBRIC -> "A prayer book: Garamond with red initials"
        HomeTheme.PIXEL -> "Pixel type and amber on green-black"
        HomeTheme.TIDE -> "The time in words over deep water"
        HomeTheme.NIGHT_WATCH -> "Black and dim red for bedside use"
        HomeTheme.PAPER -> "Black on white, for e-ink and bright sun"
        HomeTheme.OUTRUN -> "A neon clock over a sunset horizon grid"
        HomeTheme.PHOSPHOR -> "A green CRT: glowing type and scanlines"
        HomeTheme.POSTER -> "Black capitals on safety yellow"
        HomeTheme.BLUEPRINT -> "A drafting sheet: monospace on a grid"
        HomeTheme.RISO -> "Two-ink print, pink slipped over blue"
    }

@Composable
private fun NightChoice(label: String, theme: HomeTheme?, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, style = theme?.style()?.appLabel ?: MaterialTheme.typography.bodyLarge)
    }
}
