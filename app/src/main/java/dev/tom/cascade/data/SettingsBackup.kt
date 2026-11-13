package dev.tom.cascade.data

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Cascade's settings as a JSON file: favourites, folders, hidden apps, themes, icon overrides,
 * gestures, search ranking and the rest. Each value keeps its SharedPreferences type.
 */
object SettingsBackup {
    private const val FORMAT = "cascade-settings"
    private const val VERSION = 1

    /**
     * Left out: widget ids belong to this phone's widget host, the weather cache is stale by
     * the time it is restored, and the default-launcher banner is about this install.
     */
    val excluded: Set<String> = setOf("widget_ids", "weather_cache", "banner_dismissed")

    class InvalidBackup(message: String) : Exception(message)

    fun encode(values: Map<String, *>): String {
        val prefs = JSONObject()
        values.toSortedMap().forEach { (key, value) ->
            if (key in excluded) return@forEach
            val entry = when (value) {
                is String -> typed("string", value)
                is Boolean -> typed("boolean", value)
                is Int -> typed("int", value)
                is Long -> typed("long", value)
                is Float -> typed("float", value.toDouble())
                is Set<*> -> typed("set", JSONArray(value.filterIsInstance<String>().sorted()))
                else -> return@forEach
            }
            prefs.put(key, entry)
        }
        return JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("prefs", prefs)
            .toString(2)
    }

    /** The values to restore, or [InvalidBackup] if [json] isn't a backup this version can read. */
    fun decode(json: String): Map<String, Any> {
        val root = try {
            JSONObject(json)
        } catch (e: JSONException) {
            throw InvalidBackup("Not a Cascade backup")
        }
        if (root.optString("format") != FORMAT) throw InvalidBackup("Not a Cascade backup")
        val version = root.optInt("version", -1)
        if (version < 1 || version > VERSION) throw InvalidBackup("Made by a newer Cascade; update first")
        val prefs = root.optJSONObject("prefs") ?: throw InvalidBackup("The backup is empty")
        val out = LinkedHashMap<String, Any>()
        for (key in prefs.keys()) {
            if (key in excluded) continue
            val entry = prefs.optJSONObject(key) ?: continue
            val value: Any = try {
                when (entry.getString("type")) {
                    "string" -> entry.getString("value")
                    "boolean" -> entry.getBoolean("value")
                    "int" -> entry.getInt("value")
                    "long" -> entry.getLong("value")
                    "float" -> entry.getDouble("value").toFloat()
                    "set" -> entry.getJSONArray("value").let { a -> (0 until a.length()).map(a::getString).toSet() }
                    else -> continue
                }
            } catch (e: JSONException) {
                continue
            }
            out[key] = value
        }
        return out
    }

    private fun typed(type: String, value: Any): JSONObject = JSONObject().put("type", type).put("value", value)
}
