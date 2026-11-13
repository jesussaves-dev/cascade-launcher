package dev.tom.cascade.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsBackupTest {
    private val values: Map<String, Any> = mapOf(
        "home_theme" to "RISO",
        "haptics" to false,
        "icon_size" to 44,
        "some_long" to 1_700_000_000_000L,
        "some_float" to 0.5f,
        "hidden" to setOf("b/.Main", "a/.Main"),
    )

    @Test fun `every preference type round-trips`() {
        assertEquals(values, SettingsBackup.decode(SettingsBackup.encode(values)))
    }

    @Test fun `device-bound keys are never exported or restored`() {
        val withDevice = values + mapOf("widget_ids" to "12,13", "weather_cache" to "x", "banner_dismissed" to true)
        val json = SettingsBackup.encode(withDevice)
        SettingsBackup.excluded.forEach { assertTrue(it !in json) }
        // Even a hand-edited file can't smuggle them in.
        val edited = json.replace("\"prefs\": {", "\"prefs\": {\"widget_ids\": {\"type\": \"string\", \"value\": \"99\"},")
        assertEquals(values, SettingsBackup.decode(edited))
    }

    @Test fun `other files are refused`() {
        assertThrows(SettingsBackup.InvalidBackup::class.java) { SettingsBackup.decode("not json") }
        assertThrows(SettingsBackup.InvalidBackup::class.java) { SettingsBackup.decode("{\"format\":\"other\"}") }
        assertThrows(SettingsBackup.InvalidBackup::class.java) {
            SettingsBackup.decode("{\"format\":\"cascade-settings\",\"version\":99,\"prefs\":{}}")
        }
    }

    @Test fun `damaged entries are skipped, the rest restored`() {
        val json = """
            {"format":"cascade-settings","version":1,"prefs":{
              "home_theme":{"type":"string","value":"PIXEL"},
              "icon_size":{"type":"int","value":"big"},
              "mystery":{"type":"blob","value":1},
              "loose":"no type"
            }}
        """.trimIndent()
        assertEquals(mapOf("home_theme" to "PIXEL"), SettingsBackup.decode(json))
    }
}
