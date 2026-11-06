package dev.tom.cascade.util

import dev.tom.cascade.model.Verse
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyVerseTest {
    private val verses = listOf(
        Verse("Genesis 1:1", "In the beginning"),
        Verse("Psalm 23:1", "The Lord is my shepherd"),
        Verse("John 3:16", "For God so loved the world"),
    )

    @Test fun `verse follows the day and wraps`() {
        assertEquals("Genesis 1:1", verseForDay(0, verses).reference)
        assertEquals("Psalm 23:1", verseForDay(1, verses).reference)
        assertEquals("Genesis 1:1", verseForDay(3, verses).reference)
        assertEquals("Psalm 23:1", verseForDay(4, verses).reference)
        assertEquals("John 3:16", verseForDay(5, verses).reference)
    }

    @Test fun `the same day always gives the same verse`() {
        assertEquals(verseForDay(20_000, verses), verseForDay(20_000, verses))
    }

    @Test fun `days before the epoch still resolve`() {
        assertEquals("John 3:16", verseForDay(-1, verses).reference)
    }
}
