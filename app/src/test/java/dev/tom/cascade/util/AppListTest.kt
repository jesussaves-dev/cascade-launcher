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

    private fun List<HomeEntry>.apps(): List<AppEntry> =
        mapNotNull { (it as? HomeEntry.AppItem)?.app }

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
        assertEquals(listOf("123 Notes", "\uD83D\uDE00 Fun"), result.first().items.apps().map { it.label })
    }

    @Test fun `hidden apps never appear`() {
        val result = sections(apps, emptyList(), setOf("maps/.Main"), collator)
        assertEquals(0, result.flatMap { it.items.apps() }.count { it.label == "Maps" })
        assertEquals(0, searchApps(apps, "maps", setOf("maps/.Main"), collator).size)
    }

    @Test fun `favorites keep pin order and are removed from sections`() {
        val favs = listOf("photos/.Main", "zebra/.Main", "gone/.Main")
        val result = sections(apps, favs, setOf("zebra/.Main"), collator)
        assertEquals(listOf("photos/.Main"), favoriteApps(apps, favs, setOf("zebra/.Main")).map { it.key })
        assertEquals(0, result.flatMap { it.items.apps() }.count { it.key == "photos/.Main" })
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

    @Test fun `all pinned keys come back in pin order`() {
        val entries = (1..20).map { app("App$it", "p$it") }
        val keys = entries.map { it.key }
        assertEquals(keys, favoriteApps(entries, keys, emptySet()).map { it.key })
    }

    @Test fun `headers follow locale collation`() {
        val ru = Collator.getInstance(Locale("ru"))
        val entries = listOf(app("Ёлка", "elka"), app("Ель", "el"), app("Яблоко", "yabloko"))
        assertEquals(listOf("Е", "Ё", "Я"), sections(entries, emptyList(), emptySet(), ru).map { it.header })
    }
    @Test fun `initials find multi-word apps`() {
        val entries = listOf(app("Google Maps", "gmaps"), app("Gmail", "gmail"), app("Maps", "maps"))
        // "gm" is a prefix of Gmail (tier 0) and the initials of Google Maps (tier 1).
        assertEquals(listOf("Gmail", "Google Maps"), searchApps(entries, "gm", emptySet(), collator).map { it.label })
    }

    @Test fun `one typo still finds an app once the query is long enough`() {
        val entries = listOf(app("Spotify", "spotify"), app("Settings", "settings"))
        assertEquals(listOf("Spotify"), searchApps(entries, "sptoi", emptySet(), collator).map { it.label })
        assertEquals(listOf("Spotify"), searchApps(entries, "spotfy", emptySet(), collator).map { it.label })
        // Three letters is too short to guess at.
        assertEquals(emptyList<String>(), searchApps(entries, "spx", emptySet(), collator).map { it.label })
    }

    @Test fun `exact matches outrank typo matches regardless of use`() {
        val entries = listOf(app("Clock", "clock"), app("Clack", "clack"))
        val usage = mapOf("clack/.Main" to Usage(50.0, 0L))
        assertEquals(listOf("Clock", "Clack"), searchApps(entries, "clock", emptySet(), collator, usage, 0L).map { it.label })
    }

    @Test fun `within a tier the most used app comes first`() {
        val entries = listOf(app("Camera", "camera"), app("Calendar", "calendar"), app("Calculator", "calc"))
        val usage = mapOf("calc/.Main" to Usage(3.0, 0L), "calendar/.Main" to Usage(1.0, 0L))
        assertEquals(
            listOf("Calculator", "Calendar", "Camera"),
            searchApps(entries, "ca", emptySet(), collator, usage, 0L).map { it.label },
        )
        // An empty query puts used apps first, then the rest A–Z.
        assertEquals(
            listOf("Calculator", "Calendar", "Camera"),
            searchApps(entries, "", emptySet(), collator, usage, 0L).map { it.label },
        )
    }
}
