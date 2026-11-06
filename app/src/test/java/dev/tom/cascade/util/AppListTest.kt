package dev.tom.cascade.util

import dev.tom.cascade.model.AppEntry
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.Collator
import java.util.Locale

class AppListTest {
    private val collator = Collator.getInstance(Locale.US).apply { strength = Collator.PRIMARY }

    private fun app(label: String, pkg: String = label.lowercase()) =
        AppEntry("$pkg/.Main", label, pkg, ".Main")

    private val apps = listOf(
        app("Zebra"), app("alarm"), app("Photos"), app("123 Notes"),
        app("Éclair"), app("Calendar"), app("Maps"), app("\uD83D\uDE00 Fun"),
    )

    @Test fun `sort is case insensitive`() {
        val sorted = sortApps(apps, collator).map { it.label }
        assertEquals(listOf("123 Notes", "alarm", "Calendar", "Éclair", "Maps", "Photos", "Zebra", "\uD83D\uDE00 Fun"), sorted)
    }

    @Test fun `sections use letters and hash bucket for non letters`() {
        val result = sections(apps, emptyList(), emptySet(), collator)
        assertEquals(listOf("#", "A", "C", "É", "M", "P", "Z"), result.map { it.header })
        assertEquals(listOf("123 Notes", "\uD83D\uDE00 Fun"), result.first().apps.map { it.label })
    }

    @Test fun `hidden apps never appear`() {
        val result = sections(apps, emptyList(), setOf("maps/.Main"), collator)
        assertEquals(0, result.flatMap { it.apps }.count { it.label == "Maps" })
        assertEquals(0, searchApps(apps, "maps", setOf("maps/.Main"), collator).size)
    }

    @Test fun `favorites keep pin order and are removed from sections`() {
        val favs = listOf("photos/.Main", "zebra/.Main", "gone/.Main")
        val result = sections(apps, favs, setOf("zebra/.Main"), collator)
        assertEquals(listOf("photos/.Main"), favoriteApps(apps, favs, setOf("zebra/.Main")).map { it.key })
        assertEquals(0, result.flatMap { it.apps }.count { it.key == "photos/.Main" })
    }

    @Test fun `search ranks prefix before contains and ignores accents`() {
        val result = searchApps(apps, "cal", emptySet(), collator).map { it.label }
        assertEquals(listOf("Calendar"), result)
        assertEquals(listOf("Éclair"), searchApps(apps, "eclair", emptySet(), collator).map { it.label })
        assertEquals(8, searchApps(apps, "", emptySet(), collator).size)
    }

    @Test fun `search matches every word start`() {
        val entries = listOf(app("Google Maps", "gmaps"), app("Maps", "maps"))
        assertEquals(listOf("Google Maps", "Maps"), searchApps(entries, "maps", emptySet(), collator).map { it.label })
    }

    @Test fun `headers follow locale collation`() {
        val ru = Collator.getInstance(Locale("ru"))
        val entries = listOf(app("Ёлка", "elka"), app("Ель", "el"), app("Яблоко", "yabloko"))
        assertEquals(listOf("Е", "Ё", "Я"), sections(entries, emptyList(), emptySet(), ru).map { it.header })
    }
}
