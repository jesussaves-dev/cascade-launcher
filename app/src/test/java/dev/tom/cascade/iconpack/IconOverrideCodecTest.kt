package dev.tom.cascade.iconpack

import org.junit.Assert.assertEquals
import org.junit.Test

class IconOverrideCodecTest {
    @Test fun `round trips overrides`() {
        val value = mapOf("com.a/.Main" to "icon_a", "com.b/.Main" to "icon_b")
        assertEquals(value, IconOverrideCodec.decode(IconOverrideCodec.encode(value)))
    }

    @Test fun `skips malformed records and empty values`() {
        assertEquals(emptyMap<String, String>(), IconOverrideCodec.decode(null))
        assertEquals(emptyMap<String, String>(), IconOverrideCodec.decode(""))
        assertEquals(mapOf("com.a/.Main" to "icon_a"), IconOverrideCodec.decode("com.a/.Main|icon_a;bad;||;"))
    }
}
