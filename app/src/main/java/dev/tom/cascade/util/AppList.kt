package dev.tom.cascade.util

import dev.tom.cascade.model.AppEntry
import dev.tom.cascade.model.Folder
import java.text.Collator
import java.text.Normalizer
import java.util.Locale

sealed interface HomeEntry {
    val key: String
    val label: String

    data class AppItem(val app: AppEntry) : HomeEntry {
        override val key: String get() = app.key
        override val label: String get() = app.label
    }

    data class FolderItem(val folder: Folder) : HomeEntry {
        override val key: String get() = "folder:${folder.id}"
        override val label: String get() = folder.name
    }
}

data class AppSection(val header: String, val items: List<HomeEntry>)

fun sortApps(apps: List<AppEntry>, collator: Collator = Collator.getInstance()): List<AppEntry> =
    apps.sortedWith { a, b -> collator.compare(a.label, b.label) }

fun favoriteApps(
    apps: List<AppEntry>,
    favoriteKeys: List<String>,
    hidden: Set<String>,
    folders: List<Folder> = emptyList(),
): List<AppEntry> {
    val folded = folders.flatMap { it.appKeys }.toSet()
    val byKey = apps.associateBy { it.key }
    return favoriteKeys.mapNotNull(byKey::get)
        .filterNot { it.key in hidden || it.key in folded }
}

fun sections(
    apps: List<AppEntry>,
    favoriteKeys: List<String>,
    hidden: Set<String>,
    collator: Collator = Collator.getInstance(),
    folders: List<Folder> = emptyList(),
): List<AppSection> {
    val pinned = favoriteKeys.toSet()
    val folded = folders.flatMap { it.appKeys }.toSet()
    val visibleApps = apps.filter { it.key !in hidden && it.key !in pinned && it.key !in folded }
    val entries: List<HomeEntry> =
        visibleApps.map(HomeEntry::AppItem) + folders.map(HomeEntry::FolderItem)
    val sorted = entries.sortedWith { a, b -> collator.compare(a.label, b.label) }
    val grouped = sorted.groupBy { headerFor(it.label) }
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

fun folderApps(
    folder: Folder,
    apps: List<AppEntry>,
    hidden: Set<String>,
): List<AppEntry> {
    val byKey = apps.associateBy { it.key }
    return folder.appKeys.mapNotNull(byKey::get).filterNot { it.key in hidden }
}

/**
 * Apps matching [query], best first. Matches are tiered (name or word start, then initials,
 * then anywhere in the name, then one typo at a word start); within a tier the apps used most
 * lately come first, then A–Z. An empty query lists every app, most used first.
 */
fun searchApps(
    apps: List<AppEntry>,
    query: String,
    hidden: Set<String>,
    collator: Collator = Collator.getInstance(),
    usage: Map<String, Usage> = emptyMap(),
    now: Long = 0L,
): List<AppEntry> {
    val q = normalize(query).trim()
    val visible = apps.filterNot { it.key in hidden }
    val byUse = compareByDescending<AppEntry> { usage[it.key]?.at(now) ?: 0.0 }
        .thenComparator { a, b -> collator.compare(a.label, b.label) }
    if (q.isEmpty()) return visible.sortedWith(byUse)
    return visible
        .mapNotNull { app -> matchTier(normalize(app.label), q)?.let { it to app } }
        .sortedWith(compareBy<Pair<Int, AppEntry>> { it.first }.thenBy(byUse) { it.second })
        .map { it.second }
}

/** 0 name or word start, 1 initials, 2 anywhere, 3 one typo at a word start (4+ letters); null for no match. */
internal fun matchTier(label: String, q: String): Int? {
    val words = label.split(' ', '-', '.', '_').filter { it.isNotEmpty() }
    return when {
        label.startsWith(q) || words.any { it.startsWith(q) } -> 0
        q.length >= 2 && words.size >= q.length && initials(words).startsWith(q) -> 1
        label.contains(q) -> 2
        q.length >= 4 && words.any { nearPrefix(it, q) } -> 3
        else -> null
    }
}

private fun initials(words: List<String>): String = buildString { words.forEach { append(it[0]) } }

/** Whether some prefix of [word] is within one edit (or one swap of neighbours) of [q]. */
private fun nearPrefix(word: String, q: String): Boolean {
    for (len in (q.length - 1)..(q.length + 1)) {
        if (len < 1 || len > word.length) continue
        if (withinOneEdit(word.substring(0, len), q)) return true
    }
    return false
}

private fun withinOneEdit(a: String, b: String): Boolean {
    if (a == b) return true
    if (a.length == b.length) {
        val diffs = a.indices.filter { a[it] != b[it] }
        return diffs.size == 1 ||
            (diffs.size == 2 && diffs[1] == diffs[0] + 1 && a[diffs[0]] == b[diffs[1]] && a[diffs[1]] == b[diffs[0]])
    }
    val (short, long) = if (a.length < b.length) a to b else b to a
    if (long.length - short.length != 1) return false
    var i = 0
    while (i < short.length && short[i] == long[i]) i++
    return short.substring(i) == long.substring(i + 1)
}

private fun headerFor(label: String): String {
    val first = label.trim().firstOrNull() ?: return "#"
    return if (first.isLetter()) first.uppercaseChar().toString() else "#"
}

private fun normalize(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .lowercase(Locale.ROOT)
