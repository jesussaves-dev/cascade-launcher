package dev.tom.cascade.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ClockFormatTest {
    @Test fun `24 hour pads minutes`() {
        assertEquals("13:05", formatClockTime(13, 5, is24Hour = true))
        assertEquals("00:00", formatClockTime(0, 0, is24Hour = true))
    }

    @Test fun `12 hour uses midnight and noon as twelve`() {
        assertEquals("1:05", formatClockTime(13, 5, is24Hour = false))
        assertEquals("12:05", formatClockTime(0, 5, is24Hour = false))
        assertEquals("12:00", formatClockTime(12, 0, is24Hour = false))
    }

    @Test fun `words read the time as spoken`() {
        assertEquals("ten to one", formatClockWords(12, 50))
        assertEquals("quarter past three", formatClockWords(15, 15))
        assertEquals("half past nine", formatClockWords(9, 30))
        assertEquals("twelve o'clock", formatClockWords(0, 0))
        assertEquals("twenty-nine to twelve", formatClockWords(23, 31))
        assertEquals("one past eleven", formatClockWords(11, 1))
        assertEquals("quarter to twelve", formatClockWords(11, 45))
    }

    @Test fun `date is the full local date`() {
        val millis = java.time.ZonedDateTime.of(2026, 11, 6, 9, 0, 0, 0, java.time.ZoneId.of("UTC"))
            .toInstant().toEpochMilli()
        assertEquals(
            "Friday, 6 November 2026",
            formatClockDate(millis, java.time.ZoneId.of("UTC"), java.util.Locale.UK),
        )
    }
}
