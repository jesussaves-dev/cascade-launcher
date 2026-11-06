package dev.tom.cascade.util

import java.util.Locale
import kotlin.math.roundToInt

fun formatTemperature(celsius: Int, fahrenheit: Boolean): String {
    val value = if (fahrenheit) (celsius * 9 / 5f).roundToInt() + 32 else celsius
    return "$value°"
}

private val fahrenheitCountries = setOf("US", "BS", "BZ", "KY")

fun isFahrenheitLocale(locale: Locale): Boolean = locale.country in fahrenheitCountries
