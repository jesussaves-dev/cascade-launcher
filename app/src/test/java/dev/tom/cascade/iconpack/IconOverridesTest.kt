package dev.tom.cascade.iconpack

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IconOverridesTest {
    private val overrides = mapOf("a/.Main" to "icon_a")

    @Test fun `override applies with an active pack`() {
        assertEquals("icon_a", overrideDrawable(overrides, "a/.Main", "pack.one"))
    }

    @Test fun `no override means null`() {
        assertNull(overrideDrawable(overrides, "b/.Main", "pack.one"))
    }

    @Test fun `override is ignored without a pack`() {
        assertNull(overrideDrawable(overrides, "a/.Main", null))
    }

    @Test fun `override for another app is ignored`() {
        assertNull(overrideDrawable(overrides, "c/.Main", "pack.one"))
    }
}
