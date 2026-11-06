package dev.tom.cascade.util

import java.text.Normalizer
import java.util.Locale

val railLetters: List<String> = ('A'..'Z').map { it.toString() } + "#"

fun sectionOffsets(sections: List<AppSection>): Map<String, Int> {
    var offset = 0
    return buildMap {
        sections.forEach { section ->
            put(section.header, offset)
            offset += 1 + section.apps.size
        }
    }
}

fun jumpTarget(letter: String, sections: List<AppSection>): Int? {
    if (sections.isEmpty()) return null
    val offsets = sectionOffsets(sections)
    if (letter == "#") return offsets["#"] ?: 0
    val target = railLetters.indexOf(letter)
    if (target < 0) return offsets.values.last()
    val next = sections.firstOrNull { it.header != "#" && railPosition(it.header) >= target }
    return next?.let { offsets.getValue(it.header) } ?: offsets.values.last()
}

fun activeLetter(firstVisibleItemIndex: Int, sections: List<AppSection>): String? {
    var offset = 0
    for (section in sections) {
        val end = offset + 1 + section.apps.size
        if (firstVisibleItemIndex in offset until end) return section.header
        offset = end
    }
    return null
}

private fun railPosition(header: String): Int {
    val base = Normalizer.normalize(header, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .uppercase(Locale.ROOT)
        .firstOrNull() ?: return railLetters.lastIndex
    return if (base in 'A'..'Z') railLetters.indexOf(base.toString()) else railLetters.lastIndex
}
