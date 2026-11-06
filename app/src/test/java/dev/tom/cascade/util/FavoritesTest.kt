package dev.tom.cascade.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class FavoritesTest {
    @Test fun `moves a key down and up`() {
        assertEquals(listOf("b", "a", "c"), moveFavorite(listOf("a", "b", "c"), 0, 1))
        assertEquals(listOf("b", "a", "c"), moveFavorite(listOf("b", "a", "c"), 1, 0))
    }

    @Test fun `clamps out of range targets`() {
        assertEquals(listOf("b", "a"), moveFavorite(listOf("a", "b"), 0, 99))
        assertEquals(listOf("b", "a"), moveFavorite(listOf("a", "b"), -5, 0))
    }

    @Test fun `returns the same list for no-op moves`() {
        val keys = listOf("a", "b")
        assertSame(keys, moveFavorite(keys, 1, 1))
        assertEquals(keys, moveFavorite(keys, 7, 0))
    }
}
