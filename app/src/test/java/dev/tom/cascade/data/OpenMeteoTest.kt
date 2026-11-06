package dev.tom.cascade.data

import dev.tom.cascade.model.Place
import dev.tom.cascade.model.Weather
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenMeteoTest {
    @Test fun `parses current weather`() {
        val json = """{"current_weather":{"temperature":7.4,"weathercode":61}}"""
        assertEquals(Weather(7, 61, 1_000L), parseCurrentWeather(json, 1_000L))
    }

    @Test fun `malformed weather json is null`() {
        assertNull(parseCurrentWeather("", 0L))
        assertNull(parseCurrentWeather("{}", 0L))
        assertNull(parseCurrentWeather("""{"current_weather":{"temperature":"warm"}}""", 0L))
        assertNull(parseCurrentWeather("not json", 0L))
    }

    @Test fun `parses geocoding results`() {
        val json = """
            {"results":[
              {"name":"Berlin","country":"Germany","latitude":52.52,"longitude":13.41},
              {"name":"Berlin","country":"United States","latitude":39.0,"longitude":-79.0}
            ]}
        """.trimIndent()
        assertEquals(
            listOf(Place("Berlin, Germany", 52.52, 13.41), Place("Berlin, United States", 39.0, -79.0)),
            parsePlaces(json),
        )
    }

    @Test fun `geocoding with no results is empty`() {
        assertTrue(parsePlaces("""{"results":[]}""").isEmpty())
        assertTrue(parsePlaces("{}").isEmpty())
        assertTrue(parsePlaces("garbage").isEmpty())
    }
}
