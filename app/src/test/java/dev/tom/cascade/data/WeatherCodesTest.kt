package dev.tom.cascade.data

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherCodesTest {
    @Test fun `wmo codes map to glyphs`() {
        assertEquals(WeatherGlyph.Sun, glyphFor(0))
        assertEquals(WeatherGlyph.PartlyCloudy, glyphFor(1))
        assertEquals(WeatherGlyph.PartlyCloudy, glyphFor(2))
        assertEquals(WeatherGlyph.Cloud, glyphFor(3))
        assertEquals(WeatherGlyph.Fog, glyphFor(45))
        assertEquals(WeatherGlyph.Fog, glyphFor(48))
        assertEquals(WeatherGlyph.Rain, glyphFor(61))
        assertEquals(WeatherGlyph.Rain, glyphFor(80))
        assertEquals(WeatherGlyph.Snow, glyphFor(71))
        assertEquals(WeatherGlyph.Storm, glyphFor(95))
        assertEquals(WeatherGlyph.Cloud, glyphFor(999))
    }
}
