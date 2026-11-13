package dev.tom.cascade.util

import dev.tom.cascade.model.Place
import java.time.Instant
import java.time.ZoneId
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** The sun's centre is this far below the horizon at sunrise and sunset (refraction plus radius). */
private const val HORIZON_DEG = -0.833

/** Used when no weather city is set: night runs from 21:00 to 07:00 local time. */
const val FALLBACK_NIGHT_START_HOUR = 21
const val FALLBACK_NIGHT_END_HOUR = 7

/**
 * The sun's elevation in degrees at [epochMillis] seen from [latitude], [longitude].
 * Low-precision NOAA/Almanac formulae, good to a few hundredths of a degree.
 */
fun solarElevation(epochMillis: Long, latitude: Double, longitude: Double): Double {
    val n = epochMillis / 86_400_000.0 + 2_440_587.5 - 2_451_545.0
    // Angles are left unreduced; only their sines and cosines are used.
    val meanLongitude = 280.460 + 0.9856474 * n
    val anomaly = Math.toRadians(357.528 + 0.9856003 * n)
    val eclipticLongitude = Math.toRadians(
        meanLongitude + 1.915 * sin(anomaly) + 0.020 * sin(2 * anomaly),
    )
    val obliquity = Math.toRadians(23.439 - 0.0000004 * n)
    val rightAscension = atan2(cos(obliquity) * sin(eclipticLongitude), cos(eclipticLongitude))
    val declination = asin(sin(obliquity) * sin(eclipticLongitude))
    val siderealDeg = (18.697374558 + 24.06570982441908 * n) * 15.0
    val hourAngle = Math.toRadians(siderealDeg + longitude) - rightAscension
    val lat = Math.toRadians(latitude)
    return Math.toDegrees(
        asin(sin(lat) * sin(declination) + cos(lat) * cos(declination) * cos(hourAngle)),
    )
}

/** Night is sunset to sunrise at [place], or 21:00 to 07:00 local time without one. */
fun isNight(epochMillis: Long, place: Place?, zone: ZoneId = ZoneId.systemDefault()): Boolean {
    if (place != null) return solarElevation(epochMillis, place.latitude, place.longitude) < HORIZON_DEG
    val hour = Instant.ofEpochMilli(epochMillis).atZone(zone).hour
    return hour >= FALLBACK_NIGHT_START_HOUR || hour < FALLBACK_NIGHT_END_HOUR
}
