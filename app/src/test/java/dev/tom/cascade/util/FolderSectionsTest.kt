package dev.tom.cascade.util

import dev.tom.cascade.model.AppEntry
import dev.tom.cascade.model.Folder
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.Collator
import java.util.Locale

class FolderSectionsTest {
    private val collator = Collator.getInstance(Locale.US).apply { strength = Collator.PRIMARY }
    private fun app(label: String) = AppEntry("${label.lowercase()}/.Main", label, label.lowercase(), ".Main")

    private val apps = listOf(app("Slack"), app("Maps"), app("Zoom"))
    private val work = Folder("f1", "Work", listOf("slack/.Main"))

    @Test fun `folder takes its apps out of the alphabetical list`() {
        val result = sections(apps, emptyList(), emptySet(), collator, listOf(work))
        assertEquals(listOf("M", "W", "Z"), result.map { it.header })
        assertEquals(listOf("Maps", "Work", "Zoom"), result.flatMap { it.items }.map { it.label })
    }

    @Test fun `favourite pin does not duplicate a folded app`() {
        val result = sections(apps, listOf("slack/.Main"), emptySet(), collator, listOf(work))
        assertEquals(0, result.flatMap { it.items }.count { it.label == "Slack" })
        assertEquals(
            0,
            favoriteApps(apps, listOf("slack/.Main"), emptySet(), listOf(work)).count { it.key == "slack/.Main" },
        )
    }

    @Test fun `folder popup filters hidden and stale keys`() {
        val stale = Folder("f2", "Old", listOf("gone/.Main", "maps/.Main"))
        assertEquals(
            listOf("Maps"),
            folderApps(stale, apps, setOf("slack/.Main")).map { it.label },
        )
    }

    @Test fun `search still finds an app inside a folder`() {
        assertEquals(listOf("Slack"), searchApps(apps, "slack", emptySet(), collator).map { it.label })
    }
}
