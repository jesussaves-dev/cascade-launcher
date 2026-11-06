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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.tom.cascade.data.ThemeMode
import dev.tom.cascade.model.AppEntry
import dev.tom.cascade.ui.theme.AccentColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    theme: ThemeMode,
    accentIndex: Int,
    iconSizeDp: Int,
    hiddenApps: List<AppEntry>,
    onTheme: (ThemeMode) -> Unit,
    onAccent: (Int) -> Unit,
    onIconSize: (Int) -> Unit,
    onUnhide: (String) -> Unit,
    onSetDefault: () -> Unit,
    onBack: () -> Unit,
) {
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

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            SectionTitle("Default launcher")
            OutlinedButton(onClick = onSetDefault) { Text("Set Cascade as default") }

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
