package dev.tom.cascade.util

/** Returns [keys] with the entry at [from] moved to [to]; untouched when either index is out of range. */
fun moveFavorite(keys: List<String>, from: Int, to: Int): List<String> {
    if (from !in keys.indices || to !in keys.indices || from == to) return keys
    return keys.toMutableList().apply { add(to, removeAt(from)) }
}

/**
 * Rewrites the visible favourites inside [stored] without moving hidden or
 * stale keys, so a drag cannot unpin something the user cannot see.
 */
fun reorderStoredFavorites(stored: List<String>, visibleOrder: List<String>): List<String> {
    val slots = stored.indices.filter { stored[it] in visibleOrder }
    if (slots.size != visibleOrder.size) return stored
    val next = stored.toMutableList()
    slots.forEachIndexed { i, slot -> next[slot] = visibleOrder[i] }
    return next
}
