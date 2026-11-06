package dev.tom.cascade.data

object WidgetIds {
    fun encode(ids: List<Int>): String = ids.joinToString(",")

    fun decode(value: String?): List<Int> = value.orEmpty()
        .split(',')
        .mapNotNull { it.trim().toIntOrNull() }
        .filter { it > 0 }
        .distinct()
}
