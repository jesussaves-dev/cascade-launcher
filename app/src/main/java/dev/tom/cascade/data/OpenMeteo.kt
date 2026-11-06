package dev.tom.cascade.data

import dev.tom.cascade.model.Place
import dev.tom.cascade.model.Weather
import org.json.JSONObject

fun parseCurrentWeather(json: String, nowMillis: Long): Weather? = runCatching {
    val current = JSONObject(json).getJSONObject("current_weather")
    Weather(
        temperatureC = Math.round(current.getDouble("temperature")).toInt(),
        code = current.getInt("weathercode"),
        updatedAtMillis = nowMillis,
    )
}.getOrNull()

fun parsePlaces(json: String): List<Place> = runCatching {
    val results = JSONObject(json).optJSONArray("results") ?: return@runCatching emptyList()
    (0 until results.length()).mapNotNull { index ->
        val item = results.optJSONObject(index) ?: return@mapNotNull null
        val name = item.optString("name").takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val latitude = item.optDouble("latitude", Double.NaN)
        val longitude = item.optDouble("longitude", Double.NaN)
        if (latitude.isNaN() || longitude.isNaN()) return@mapNotNull null
        val country = item.optString("country").takeIf { it.isNotBlank() }
        Place(if (country == null) name else "$name, $country", latitude, longitude)
    }
}.getOrDefault(emptyList())
