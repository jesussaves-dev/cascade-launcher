package dev.tom.cascade.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.tom.cascade.util.formatClockDate
import dev.tom.cascade.util.formatClockTime
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId

@Composable
fun ClockHeader(modifier: Modifier = Modifier) {
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = formatClockTime(local.hour, local.minute, DateFormat.is24HourFormat(context)),
            style = MaterialTheme.typography.displayLarge,
        )
        Text(
            text = formatClockDate(now),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
