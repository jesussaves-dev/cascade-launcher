package dev.tom.cascade.data

import dev.tom.cascade.model.Place
import dev.tom.cascade.model.Weather

object PrefsCodec {
    fun encodePlace(place: Place): String =
        "${sanitize(place.name)}|${place.latitude}|${place.longitude}"

    fun decodePlace(value: String?): Place? {
        val parts = value?.split('|') ?: return null
        if (parts.size != 3) return null
        val latitude = parts[1].toDoubleOrNull() ?: return null
        val longitude = parts[2].toDoubleOrNull() ?: return null
        return Place(parts[0], latitude, longitude)
    }

    fun encodeWeather(weather: Weather): String =
        "${weather.temperatureC}|${weather.code}|${weather.updatedAtMillis}"

    fun decodeWeather(value: String?): Weather? {
        val parts = value?.split('|') ?: return null
        if (parts.size != 3) return null
        return Weather(
            temperatureC = parts[0].toIntOrNull() ?: return null,
            code = parts[1].toIntOrNull() ?: return null,
            updatedAtMillis = parts[2].toLongOrNull() ?: return null,
        )
    }

    private fun sanitize(name: String): String =
        name.replace(Regex("[|\\r\\n]"), " ").trim()
}
