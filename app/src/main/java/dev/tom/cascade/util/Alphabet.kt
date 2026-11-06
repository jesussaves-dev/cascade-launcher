package dev.tom.cascade.util

import java.text.Normalizer
import java.util.Locale

val railLetters: List<String> = ('A'..'Z').map { it.toString() } + "#"

fun sectionOffsets(sections: List<AppSection>): Map<String, Int> {
    var offset = 0
    return buildMap {
        sections.forEach { section ->
            put(section.header, offset)
            offset += 1 + section.items.size
        }
    }
}

fun jumpTarget(letter: String, sections: List<AppSection>, leading: Int = 0): Int? {
    if (sections.isEmpty()) return null
    val offsets = sectionOffsets(sections)
    if (letter == "#") return leading + (offsets["#"] ?: 0)
    val target = railLetters.indexOf(letter)
    if (target < 0) return leading + offsets.values.last()
    val next = sections.firstOrNull { it.header != "#" && railPosition(it.header) >= target }
    return leading + (next?.let { offsets.getValue(it.header) } ?: offsets.values.last())
}

fun activeLetter(firstVisibleItemIndex: Int, sections: List<AppSection>, leading: Int = 0): String? {
    val index = firstVisibleItemIndex - leading
    if (index < 0) return null
    var offset = 0
    for (section in sections) {
        val end = offset + 1 + section.items.size
        if (index in offset until end) return section.header
        offset = end
    }
    return null
}

fun railLetterFor(header: String): String? {
    val base = Normalizer.normalize(header, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .uppercase(Locale.ROOT)
        .firstOrNull()
    return when {
        base != null && base in 'A'..'Z' -> base.toString()
        header.trim().startsWith("#") -> "#"
        else -> null
    }
}

private fun railPosition(header: String): Int =
    railLetterFor(header)?.let { railLetters.indexOf(it) } ?: railLetters.lastIndex
