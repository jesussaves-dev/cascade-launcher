package dev.tom.cascade.util

import dev.tom.cascade.model.Place
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class NightScheduleTest {
    private val utc = ZoneId.of("UTC")
    private fun millis(y: Int, m: Int, d: Int, h: Int, min: Int = 0, zone: ZoneId = utc) =
        ZonedDateTime.of(y, m, d, h, min, 0, 0, zone).toInstant().toEpochMilli()

    private val london = Place("London", 51.5074, -0.1278)
    private val tromso = Place("Tromsø", 69.6492, 18.9553)

    @Test
    fun londonMiddayAtSolsticeIsHigh() {
        // Noon elevation at the June solstice is 90 - 51.5 + 23.44 ≈ 61.9°.
        assertEquals(61.9, solarElevation(millis(2026, 6, 21, 12, 2), london.latitude, london.longitude), 0.3)
    }

    @Test
    fun londonMidnightAtWinterSolsticeIsLow() {
        // Lower culmination at the December solstice is 51.5 - 23.44 - 90 ≈ -61.9°.
        assertEquals(-61.9, solarElevation(millis(2026, 12, 21, 0, 2), london.latitude, london.longitude), 0.3)
    }

    @Test
    fun londonSunriseAndSunsetAtEquinox() {
        // 2026-03-20: sunrise ≈ 06:02 UTC, sunset ≈ 18:13 UTC.
        assertTrue(isNight(millis(2026, 3, 20, 5, 50), london))
        assertFalse(isNight(millis(2026, 3, 20, 6, 15), london))
        assertFalse(isNight(millis(2026, 3, 20, 18, 0), london))
        assertTrue(isNight(millis(2026, 3, 20, 18, 25), london))
    }

    @Test
    fun polarDayAndPolarNight() {
        // Midnight sun in June, polar night in December.
        assertFalse(isNight(millis(2026, 6, 21, 23), tromso))
        assertTrue(isNight(millis(2026, 12, 21, 11), tromso))
    }

    @Test
    fun fallbackHoursWithoutAPlace() {
        assertTrue(isNight(millis(2026, 6, 21, 21), null, utc))
        assertTrue(isNight(millis(2026, 6, 21, 6, 59), null, utc))
        assertFalse(isNight(millis(2026, 6, 21, 7), null, utc))
        assertFalse(isNight(millis(2026, 6, 21, 20, 59), null, utc))
    }
}
