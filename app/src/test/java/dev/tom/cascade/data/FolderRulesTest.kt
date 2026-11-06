package dev.tom.cascade.data

import dev.tom.cascade.model.Folder
import org.junit.Assert.assertEquals
import org.junit.Test

class FolderRulesTest {
    private val folders = listOf(
        Folder("f1", "Work", listOf("a/.Main")),
        Folder("f2", "Play", listOf("b/.Main")),
    )

    @Test fun `adding moves an app out of its previous folder`() {
        val moved = moveApp(folders, "f2", "c/.Main")
        assertEquals(listOf("a/.Main"), moved.first { it.id == "f1" }.appKeys)
        assertEquals(listOf("b/.Main", "c/.Main"), moved.first { it.id == "f2" }.appKeys)
    }

    @Test fun `adding twice does not duplicate and pulls from other folders`() {
        val moved = moveApp(folders, "f2", "a/.Main")
        assertEquals(listOf("b/.Main", "a/.Main"), moved.first { it.id == "f2" }.appKeys)
        assertEquals(emptyList<String>(), moved.first { it.id == "f1" }.appKeys)
        assertEquals(2, moveApp(moved, "f2", "a/.Main").first { it.id == "f2" }.appKeys.size)
    }

    @Test fun `deleting removes only that folder`() {
        assertEquals(listOf("f2"), deleteFolder(folders, "f1").map { it.id })
    }
}
