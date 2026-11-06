package dev.tom.cascade.util

/** Returns [keys] with the entry at [from] moved to [to]; untouched when either index is out of range. */
fun moveFavorite(keys: List<String>, from: Int, to: Int): List<String> {
    if (from !in keys.indices || to !in keys.indices || from == to) return keys
    return keys.toMutableList().apply { add(to, removeAt(from)) }
}
