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

private val numberWords = listOf(
    "twelve", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
    "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
    "seventeen", "eighteen", "nineteen", "twenty",
)

private fun minuteWords(m: Int): String = when {
    m == 15 -> "quarter"
    m == 30 -> "half"
    m <= 20 -> numberWords[m]
    else -> "twenty-" + numberWords[m - 20]
}

/** The time as it is spoken: "ten to one", "quarter past three", "nine o'clock". */
fun formatClockWords(hour: Int, minute: Int): String {
    val thisHour = numberWords[hour % 12]
    val nextHour = numberWords[(hour + 1) % 12]
    return when {
        minute == 0 -> "$thisHour o'clock"
        minute <= 30 -> "${minuteWords(minute)} past $thisHour"
        else -> "${minuteWords(60 - minute)} to $nextHour"
    }
}
