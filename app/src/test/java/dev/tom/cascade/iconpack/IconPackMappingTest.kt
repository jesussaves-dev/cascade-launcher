package dev.tom.cascade.iconpack

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayInputStream

class IconPackMappingTest {
    private fun parse(xml: String) = IconPackMapping.parse(ByteArrayInputStream(xml.toByteArray()))

    private fun mapping(vararg items: Pair<String, String>) =
        IconPackMapping.fromItems(items.toList())

    private val sample = """
        <resources>
          <item component="ComponentInfo{com.foo/com.foo.Main}" drawable="foo_round" />
          <item component="com.bar/com.bar.Main" drawable="bar_round" />
          <item component="com.baz/.Main" drawable="baz_round" />
          <item component="com.pkg/Short" drawable="short_round" />
          <item drawable="missing_component" />
          <item component="com.empty/.Main" />
        </resources>
    """.trimIndent()

    @Test fun `component info wrapper is understood`() {
        assertEquals("foo_round", parse(sample).drawableName("com.foo", "com.foo.Main"))
    }

    @Test fun `bare component name is understood`() {
        assertEquals("bar_round", parse(sample).drawableName("com.bar", "com.bar.Main"))
    }

    @Test fun `leading dot activity is expanded`() {
        assertEquals("baz_round", parse(sample).drawableName("com.baz", "com.baz.Main"))
        assertEquals("baz_round", parse(sample).drawableName("com.baz", ".Main"))
    }

    @Test fun `activity without a dot is qualified by the package`() {
        assertEquals("short_round", parse(sample).drawableName("com.pkg", "com.pkg.Short"))
    }

    @Test fun `exact component wins over package fallback`() {
        val map = mapping(
            "com.foo/com.foo.Main" to "main_icon",
            "com.foo/com.foo.Other" to "other_icon",
        )
        assertEquals("main_icon", map.drawableName("com.foo", "com.foo.Main"))
        assertEquals("other_icon", map.drawableName("com.foo", "com.foo.Other"))
    }

    @Test fun `unknown activity falls back to the package`() {
        val map = mapping("com.foo/com.foo.Main" to "foo_icon")
        assertEquals("foo_icon", map.drawableName("com.foo", "com.foo.Settings"))
    }

    @Test fun `unknown package returns null`() {
        assertNull(mapping("com.foo/com.foo.Main" to "foo_icon").drawableName("com.other", "com.other.Main"))
    }

    @Test fun `records without component or drawable are skipped`() {
        val parsed = parse(
            """
            <resources>
              <item drawable="no_component" />
              <item component="com.foo/com.foo.Main" />
              <item component="" drawable="blank_component" />
              <item component="com.foo/com.foo.Main" drawable="ok" />
            </resources>
            """.trimIndent(),
        )
        assertNull(parsed.drawableName("com.bar", "com.bar.Main"))
        assertEquals("ok", parsed.drawableName("com.foo", "com.foo.Main"))
    }

    @Test fun `entries round trip through fromItems`() {
        val mapping = IconPackMapping.fromItems(listOf("com.example/.Main" to "icon_a"))
        val copy = IconPackMapping.fromItems(mapping.entries())
        assertEquals("icon_a", copy.drawableName("com.example", "com.example.Main"))
    }
}
