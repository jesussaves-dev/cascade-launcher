package dev.tom.cascade.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class FavoritesTest {
    @Test fun `moves a key down and up`() {
        assertEquals(listOf("b", "a", "c"), moveFavorite(listOf("a", "b", "c"), 0, 1))
        assertEquals(listOf("a", "b", "c"), moveFavorite(listOf("b", "a", "c"), 1, 0))
    }

    @Test fun `ignores out of range indices`() {
        val keys = listOf("a", "b")
        assertEquals(keys, moveFavorite(keys, 0, 99))
        assertEquals(keys, moveFavorite(keys, -5, 0))
    }

    @Test fun `returns the same list for no-op moves`() {
        val keys = listOf("a", "b")
        assertSame(keys, moveFavorite(keys, 1, 1))
        assertEquals(keys, moveFavorite(keys, 7, 0))
    }
}

class FavoriteOrderMergeTest {
    @Test fun `keeps hidden stored keys while reordering the visible ones`() {
        val stored = listOf("hidden", "a", "b")
        assertEquals(listOf("hidden", "b", "a"), reorderStoredFavorites(stored, listOf("b", "a")))
    }

    @Test fun `leaves the stored list alone when the visible list does not match`() {
        val stored = listOf("a", "b")
        assertEquals(stored, reorderStoredFavorites(stored, listOf("b", "gone")))
    }
}
