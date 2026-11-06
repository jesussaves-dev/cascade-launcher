package dev.tom.cascade.data

import dev.tom.cascade.model.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class WeatherRepository(private val prefs: Prefs) {
    suspend fun searchPlaces(query: String): List<Place> = withContext(Dispatchers.IO) {
        val name = query.trim()
        if (name.isEmpty()) return@withContext emptyList()
        val url = "https://geocoding-api.open-meteo.com/v1/search" +
            "?name=${URLEncoder.encode(name, "UTF-8")}&count=8&language=en&format=json"
        fetch(url)?.let(::parsePlaces).orEmpty()
    }

    suspend fun refreshIfStale(place: Place, maxAgeMillis: Long = 30 * 60_000) {
        withContext(Dispatchers.IO) {
            val cached = prefs.cachedWeather.value
            if (cached != null && System.currentTimeMillis() - cached.updatedAtMillis < maxAgeMillis) {
                return@withContext
            }
            val url = "https://api.open-meteo.com/v1/forecast" +
                "?latitude=${place.latitude}&longitude=${place.longitude}&current_weather=true"
            val json = fetch(url) ?: return@withContext
            parseCurrentWeather(json, System.currentTimeMillis())?.let(prefs::setCachedWeather)
        }
    }

    private fun fetch(url: String): String? = runCatching {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 5_000
            readTimeout = 5_000
            requestMethod = "GET"
        }
        try {
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }.getOrNull()
}
