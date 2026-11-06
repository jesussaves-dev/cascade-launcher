package dev.tom.cascade.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import dev.tom.cascade.data.glyphFor
import dev.tom.cascade.model.Verse
import dev.tom.cascade.model.Weather
import dev.tom.cascade.util.formatClockDate
import dev.tom.cascade.util.formatClockTime
import dev.tom.cascade.util.formatTemperature
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId

@Composable
fun ClockHeader(
    weather: Weather? = null,
    showWeather: Boolean = false,
    verse: Verse? = null,
    fahrenheit: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            val secondsIntoMinute = now / 1000 % 60
            delay((60 - secondsIntoMinute) * 1000)
        }
    }
    val local = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault())
    // delay() resumes late after deep sleep; a resume always re-reads the wall clock.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { now = System.currentTimeMillis() }
    val compact = LocalConfiguration.current.screenHeightDp < 600
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = if (compact) 8.dp else 16.dp),
    ) {
        Text(
            text = formatClockTime(local.hour, local.minute, DateFormat.is24HourFormat(context)),
            style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.displayLarge,
        )
        if (!compact) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatClockDate(now),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (showWeather && weather != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        WeatherGlyphIcon(glyph = glyphFor(weather.code))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = formatTemperature(weather.temperatureC, fahrenheit),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (verse != null) {
                Text(
                    text = verse.reference,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    text = verse.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
