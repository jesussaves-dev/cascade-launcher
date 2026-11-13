package dev.tom.cascade.util

import kotlin.math.pow

/**
 * How much an app is used, as one decaying score: each launch adds 1, and the total halves
 * every [HALF_LIFE_MS]. Two numbers per app, however many times it is opened.
 */
data class Usage(val score: Double, val lastMillis: Long) {
    /** The score as it stands at [now]. */
    fun at(now: Long): Double = score * decay(now - lastMillis)

    fun launched(now: Long): Usage = Usage(at(now) + 1.0, now)

    companion object {
        const val HALF_LIFE_MS = 7L * 24 * 60 * 60 * 1000

        private fun decay(ageMs: Long): Double = 0.5.pow(ageMs.coerceAtLeast(0).toDouble() / HALF_LIFE_MS)
    }
}

/** Stored as `key<TAB>score<TAB>lastMillis` lines; app keys never contain tabs or newlines. */
object UsageCodec {
    /** Scores this small are a launch or two from months ago; they are dropped on save. */
    private const val FORGET_BELOW = 0.01

    fun encode(usage: Map<String, Usage>, now: Long): String =
        usage.entries
            .filter { it.value.at(now) >= FORGET_BELOW }
            .joinToString("\n") { (key, u) -> "$key\t${u.score}\t${u.lastMillis}" }

    fun decode(value: String?): Map<String, Usage> =
        value.orEmpty().lineSequence().mapNotNull { line ->
            val parts = line.split('\t')
            if (parts.size != 3) return@mapNotNull null
            val score = parts[1].toDoubleOrNull() ?: return@mapNotNull null
            val last = parts[2].toLongOrNull() ?: return@mapNotNull null
            if (parts[0].isEmpty() || score.isNaN() || score < 0) return@mapNotNull null
            parts[0] to Usage(score, last)
        }.toMap()
}
