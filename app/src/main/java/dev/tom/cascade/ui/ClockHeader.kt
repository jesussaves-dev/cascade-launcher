package dev.tom.cascade.ui

import android.text.format.DateFormat
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import dev.tom.cascade.ui.theme.LocalHomeStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import dev.tom.cascade.data.glyphFor
import dev.tom.cascade.model.Weather
import dev.tom.cascade.util.formatClockDate
import dev.tom.cascade.data.kjvVerses
import dev.tom.cascade.util.formatClockTime
import dev.tom.cascade.util.formatClockWords
import dev.tom.cascade.util.formatTemperature
import dev.tom.cascade.util.verseForDay
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId

@Composable
fun ClockHeader(
    weather: Weather? = null,
    showWeather: Boolean = false,
    showVerse: Boolean = false,
    fahrenheit: Boolean = false,
    onDoubleTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val doubleTap by rememberUpdatedState(onDoubleTap)
    val longPress by rememberUpdatedState(onLongPress)
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
    val verse = if (!compact && showVerse) {
        verseForDay(local.toLocalDate().toEpochDay(), kjvVerses)
    } else {
        null
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction("Lock screen") { doubleTap(); true },
                    CustomAccessibilityAction("Change wallpaper") { longPress(); true },
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDoubleTap() },
                    onLongPress = { onLongPress() },
                )
            }
            .padding(start = 20.dp, end = 20.dp, top = if (compact) 4.dp else 8.dp, bottom = if (compact) 8.dp else 20.dp),
    ) {
        val style = LocalHomeStyle.current
        Text(
            text = if (style.clockInWords) {
                formatClockWords(local.hour, local.minute)
            } else {
                formatClockTime(local.hour, local.minute, DateFormat.is24HourFormat(context))
            },
            style = if (compact) style.clockCompact else style.clock,
            color = if (style.clockInAccent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.offset(x = style.clockNudge),
        )
        if (!compact) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp),
            ) {
                Text(
                    text = formatClockDate(now),
                    style = style.date,
                    // 1.0 (the only non-epigraph theme) muted the date.
                    color = if (style.verseAsEpigraph) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (showWeather && weather != null) {
                    Spacer(Modifier.width(14.dp))
                    WeatherGlyphIcon(glyph = glyphFor(weather.code))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = formatTemperature(weather.temperatureC, fahrenheit),
                        style = style.date,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            val referenceColor = if (style.referenceInAccent) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
            if (verse != null && !style.verseAsEpigraph) {
                // 1.0: the reference as a small label above the text.
                Text(
                    text = verse.reference,
                    style = style.verseReference,
                    color = referenceColor,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    text = verse.text,
                    style = style.verse,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            } else if (verse != null) {
                // Set as an epigraph: italic text, reference hung beneath on the right.
                Column(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .widthIn(max = 340.dp),
                ) {
                    Text(
                        text = verse.text,
                        style = style.verse,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.86f),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "\u2013 ${verse.reference}",
                        style = style.verseReference,
                        color = referenceColor,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                    )
                }
            }
        }
    }
}
