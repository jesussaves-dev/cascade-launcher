package dev.tom.cascade.iconpack

import org.junit.Assert.assertEquals
import org.junit.Test

class IconSuggestionsTest {
    @Test fun `matches label and package tokens before the rest`() {
        val icons = listOf(
            PackIcon("icon_alpha", "Alpha"),
            PackIcon("icon_playstore", "Play Store"),
            PackIcon("icon_beta", "Beta"),
        )
        val ordered = orderIcons(icons, label = "Play Store", packageName = "com.android.vending")
        assertEquals("icon_playstore", ordered.first().drawable)
        assertEquals(3, ordered.size)
    }

    @Test fun `package token also matches`() {
        val icons = listOf(
            PackIcon("icon_alpha", "Alpha"),
            PackIcon("icon_vending", "Store"),
        )
        assertEquals(
            "icon_vending",
            orderIcons(icons, label = "Shop", packageName = "com.android.vending").first().drawable,
        )
    }
}
