package dev.tom.cascade.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun formatClockTime(hour: Int, minute: Int, is24Hour: Boolean): String {
    val minutes = minute.toString().padStart(2, '0')
    if (is24Hour) return hour.toString().padStart(2, '0') + ":" + minutes
    val hour12 = hour % 12
    return (if (hour12 == 0) 12 else hour12).toString() + ":" + minutes
}

fun formatClockDate(
    millis: Long,
    zone: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault(),
): String = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
    .withLocale(locale)
    .format(Instant.ofEpochMilli(millis).atZone(zone))
