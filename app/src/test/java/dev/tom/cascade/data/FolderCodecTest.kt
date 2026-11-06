package dev.tom.cascade.data

import dev.tom.cascade.model.Folder
import org.junit.Assert.assertEquals
import org.junit.Test

class FolderCodecTest {
    @Test fun `round trips folders and keys`() {
        val folders = listOf(
            Folder("f1", "Work", listOf("slack/.Main", "mail/.Main")),
            Folder("f2", "Games", emptyList()),
        )
        assertEquals(folders, FolderCodec.decode(FolderCodec.encode(folders)))
    }

    @Test fun `names with separators and newlines survive`() {
        val folders = listOf(Folder("f1", "A\u001FB\u001EC\nD", listOf("x/.Main")))
        assertEquals("A B C D", FolderCodec.decode(FolderCodec.encode(folders)).single().name)
    }

    @Test fun `blank and malformed values decode empty or skip`() {
        assertEquals(emptyList<Folder>(), FolderCodec.decode(null))
        assertEquals(emptyList<Folder>(), FolderCodec.decode(""))
        assertEquals(emptyList<Folder>(), FolderCodec.decode("garbage"))
    }

    @Test fun `duplicate ids and app keys are dropped`() {
        val encoded = "f1\u001FWork\u001Fx/.Main\u001Fx/.Main\u001E" +
            "f1\u001FWork again\u001Fy/.Main"
        val folders = FolderCodec.decode(encoded)
        assertEquals(1, folders.size)
        assertEquals(listOf("x/.Main"), folders.single().appKeys)
    }
}
