package dev.tom.cascade.util

import dev.tom.cascade.model.AppEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AlphabetTest {
    private fun app(label: String) = AppEntry("${label.lowercase()}/.Main", label, label.lowercase(), ".Main")
    private fun section(header: String, count: Int) =
        AppSection(header, (1..count).map { app("$header$it") })

    private val sections = listOf(section("A", 2), section("C", 1), section("Z", 3))

    @Test fun `offsets count headers and apps`() {
        assertEquals(mapOf("A" to 0, "C" to 3, "Z" to 5), sectionOffsets(sections))
    }

    @Test fun `jump uses exact section when present`() {
        assertEquals(3, jumpTarget("C", sections))
    }

    @Test fun `jump to missing letter goes to the next section`() {
        assertEquals(3, jumpTarget("B", sections))
        assertEquals(5, jumpTarget("X", sections))
    }

    @Test fun `jump past the last letter goes to the last section`() {
        assertEquals(5, jumpTarget("ZZ", sections))
    }

    @Test fun `jump from hash goes to top`() {
        assertEquals(0, jumpTarget("#", sections))
    }

    @Test fun `active letter follows the visible item`() {
        assertEquals("A", activeLetter(0, sections))
        assertEquals("A", activeLetter(2, sections))
        assertEquals("C", activeLetter(3, sections))
        assertEquals("Z", activeLetter(5, sections))
        assertNull(activeLetter(99, sections))
    }

    @Test fun `accented headers map to their base rail letter`() {
        assertEquals("E", railLetterFor("É"))
        assertEquals("#", railLetterFor("#"))
        assertNull(railLetterFor("Я"))
    }

    @Test fun `rail has 26 letters and hash`() {
        assertEquals(27, railLetters.size)
        assertEquals("#", railLetters.last())
        assertEquals("A", railLetters.first())
    }
}
