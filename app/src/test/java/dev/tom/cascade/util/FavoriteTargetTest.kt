package dev.tom.cascade.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteTargetTest {
    @Test fun `target follows the finger in half rows`() {
        assertEquals(2, favoriteTarget(from = 2, offsetPx = 0f, rowHeightPx = 100f, size = 5))
        assertEquals(3, favoriteTarget(from = 2, offsetPx = 60f, rowHeightPx = 100f, size = 5))
        assertEquals(1, favoriteTarget(from = 2, offsetPx = -60f, rowHeightPx = 100f, size = 5))
    }

    @Test fun `target clamps to the list`() {
        assertEquals(0, favoriteTarget(from = 2, offsetPx = -900f, rowHeightPx = 100f, size = 5))
        assertEquals(4, favoriteTarget(from = 2, offsetPx = 900f, rowHeightPx = 100f, size = 5))
    }

    @Test fun `target survives an empty or unmeasured list`() {
        assertEquals(0, favoriteTarget(from = 0, offsetPx = 50f, rowHeightPx = 100f, size = 0))
        assertEquals(1, favoriteTarget(from = 1, offsetPx = 50f, rowHeightPx = 0f, size = 3))
    }
}
