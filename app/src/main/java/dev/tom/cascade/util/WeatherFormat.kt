package dev.tom.cascade.util

import java.util.Locale
import kotlin.math.roundToInt

fun formatTemperature(celsius: Int, fahrenheit: Boolean): String {
    val value = if (fahrenheit) (celsius * 9 / 5f).roundToInt() + 32 else celsius
    return "$value°"
}

fun isFahrenheitLocale(locale: Locale): Boolean {
    if (locale.country.isEmpty()) return false
    return "${locale.language}_${locale.country}" in setOf("en_US", "bs_BA", "bz_BZ", "ky_KG")
}
