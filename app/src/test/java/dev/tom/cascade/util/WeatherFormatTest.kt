package dev.tom.cascade.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class WeatherFormatTest {
    @Test fun `celsius and fahrenheit round as expected`() {
        assertEquals("7°", formatTemperature(7, fahrenheit = false))
        assertEquals("45°", formatTemperature(7, fahrenheit = true))
        assertEquals("-3°", formatTemperature(-3, fahrenheit = false))
        assertEquals("27°", formatTemperature(-3, fahrenheit = true))
    }

    @Test fun `only the four fahrenheit locales use fahrenheit`() {
        assertTrue(isFahrenheitLocale(Locale.US))
        assertTrue(isFahrenheitLocale(Locale("bs", "BA")))
        assertTrue(isFahrenheitLocale(Locale("bz", "BZ")))
        assertTrue(isFahrenheitLocale(Locale("ky", "KG")))
        assertFalse(isFahrenheitLocale(Locale.UK))
        assertFalse(isFahrenheitLocale(Locale.GERMANY))
    }
}
