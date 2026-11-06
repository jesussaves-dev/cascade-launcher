package dev.tom.cascade.iconpack

object IconOverrideCodec {
    fun encode(overrides: Map<String, String>): String =
        overrides.entries.joinToString(";") { "${it.key}|${it.value}" }

    fun decode(value: String?): Map<String, String> {
        if (value.isNullOrBlank()) return emptyMap()
        val result = LinkedHashMap<String, String>()
        value.split(";").forEach { record ->
            val parts = record.split("|")
            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                result[parts[0]] = parts[1]
            }
        }
        return result
    }
}
