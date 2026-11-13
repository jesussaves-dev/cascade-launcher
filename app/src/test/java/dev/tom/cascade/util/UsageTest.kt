package dev.tom.cascade.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UsageTest {
    private val week = Usage.HALF_LIFE_MS

    @Test fun `a score halves every half-life`() {
        val u = Usage(4.0, 0L)
        assertEquals(2.0, u.at(week), 1e-9)
        assertEquals(1.0, u.at(2 * week), 1e-9)
    }

    @Test fun `a launch decays the old score then adds one`() {
        val u = Usage(4.0, 0L).launched(week)
        assertEquals(3.0, u.score, 1e-9)
        assertEquals(week, u.lastMillis)
    }

    @Test fun `recent light use can overtake old heavy use`() {
        val old = Usage(10.0, 0L)
        val recent = Usage(3.0, 4 * week)
        assertTrue(recent.at(4 * week) > old.at(4 * week))
    }

    @Test fun `codec round-trips and forgets faded apps`() {
        val now = 10 * week
        val usage = mapOf(
            "a/.Main" to Usage(2.5, now),
            "b/.Main" to Usage(1.0, 0L), // 1/1024 after ten half-lives
        )
        assertEquals(mapOf("a/.Main" to Usage(2.5, now)), UsageCodec.decode(UsageCodec.encode(usage, now)))
    }

    @Test fun `codec skips damaged lines`() {
        val decoded = UsageCodec.decode("ok/.Main\t1.5\t42\nbroken\nx\tnan\t1\ny\t-1\t1\n\tz\t1")
        assertEquals(mapOf("ok/.Main" to Usage(1.5, 42L)), decoded)
    }
}
