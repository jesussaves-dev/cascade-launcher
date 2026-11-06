package dev.tom.cascade.util

import kotlin.math.roundToInt

/** Index the dragged favourite would land on for [offsetPx] of travel, clamped to the list. */
fun favoriteTarget(from: Int, offsetPx: Float, rowHeightPx: Float, size: Int): Int {
    if (size <= 0) return 0
    val base = from.coerceIn(0, size - 1)
    if (rowHeightPx <= 0f) return base
    return (base + (offsetPx / rowHeightPx).roundToInt()).coerceIn(0, size - 1)
}

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
