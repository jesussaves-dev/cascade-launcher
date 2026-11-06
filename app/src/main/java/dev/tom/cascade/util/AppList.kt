package dev.tom.cascade.util

import dev.tom.cascade.model.AppEntry
import java.text.Collator
import java.text.Normalizer
import java.util.Locale

data class AppSection(val header: String, val apps: List<AppEntry>)

fun sortApps(apps: List<AppEntry>, collator: Collator = Collator.getInstance()): List<AppEntry> =
    apps.sortedWith { a, b -> collator.compare(a.label, b.label) }

fun favoriteApps(
    apps: List<AppEntry>,
    favoriteKeys: List<String>,
    hidden: Set<String>,
): List<AppEntry> {
    val byKey = apps.associateBy { it.key }
    return favoriteKeys.mapNotNull(byKey::get).filterNot { it.key in hidden }
}

fun sections(
    apps: List<AppEntry>,
    favoriteKeys: List<String>,
    hidden: Set<String>,
    collator: Collator = Collator.getInstance(),
): List<AppSection> {
    val pinned = favoriteKeys.toSet()
    val visible = sortApps(apps.filter { it.key !in hidden && it.key !in pinned }, collator)
    val grouped = visible.groupBy { headerFor(it.label) }
    // Non-letter labels stay together at the top, letters follow locale order.
    val hash = grouped["#"].orEmpty()
    val letters = grouped.keys
        .filter { it != "#" }
        .sortedWith { a, b -> collator.compare(a, b) }
    return buildList {
        if (hash.isNotEmpty()) add(AppSection("#", hash))
        letters.forEach { header -> add(AppSection(header, grouped.getValue(header))) }
    }
}

fun searchApps(
    apps: List<AppEntry>,
    query: String,
    hidden: Set<String>,
    collator: Collator = Collator.getInstance(),
): List<AppEntry> {
    val q = normalize(query)
    val visible = apps.filterNot { it.key in hidden }
    if (q.isEmpty()) return sortApps(visible, collator)
    return visible
        .mapNotNull { app ->
            val label = normalize(app.label)
            val rank = when {
                label.startsWith(q) -> 0
                label.split(' ', '-', '.', '_').any { it.startsWith(q) } -> 0
                label.contains(q) -> 1
                else -> null
            }
            rank?.let { it to app }
        }
        .sortedWith(
            compareBy<Pair<Int, AppEntry>> { it.first }
                .thenComparator { a, b -> collator.compare(a.second.label, b.second.label) }
        )
        .map { it.second }
}

private fun headerFor(label: String): String {
    val first = label.trim().firstOrNull() ?: return "#"
    return if (first.isLetter()) first.uppercaseChar().toString() else "#"
}

private fun normalize(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .lowercase(Locale.ROOT)
