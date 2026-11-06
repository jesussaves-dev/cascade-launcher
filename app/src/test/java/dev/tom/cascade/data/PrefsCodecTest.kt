package dev.tom.cascade.data

import dev.tom.cascade.model.Place
import dev.tom.cascade.model.Weather
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PrefsCodecTest {
    @Test fun `place round trips`() {
        val place = Place("Berlin, Germany", 52.52, 13.41)
        assertEquals(place, PrefsCodec.decodePlace(PrefsCodec.encodePlace(place)))
    }

    @Test fun `place name separators are flattened`() {
        val place = Place("A|B\nC", 1.0, 2.0)
        assertEquals("A B C", PrefsCodec.decodePlace(PrefsCodec.encodePlace(place))?.name)
    }

    @Test fun `malformed places decode null`() {
        assertNull(PrefsCodec.decodePlace(null))
        assertNull(PrefsCodec.decodePlace(""))
        assertNull(PrefsCodec.decodePlace("Berlin"))
        assertNull(PrefsCodec.decodePlace("Berlin|north|east"))
    }

    @Test fun `weather round trips`() {
        val weather = Weather(7, 61, 1_700_000_000_000)
        assertEquals(weather, PrefsCodec.decodeWeather(PrefsCodec.encodeWeather(weather)))
    }

    @Test fun `malformed weather decodes null`() {
        assertNull(PrefsCodec.decodeWeather(null))
        assertNull(PrefsCodec.decodeWeather("7|61"))
        assertNull(PrefsCodec.decodeWeather("warm|61|0"))
    }
}
