package dev.tom.cascade.iconpack

data class PackIcon(val drawable: String, val name: String)

fun orderIcons(icons: List<PackIcon>, label: String, packageName: String): List<PackIcon> {
    val tokens = listOf(
        normalizeToken(label),
        packageName.substringAfterLast('.', packageName).let(::normalizeToken),
    ).filter { it.isNotEmpty() }
    if (tokens.isEmpty()) return icons
    return icons.sortedBy { icon ->
        val haystack = normalizeToken(icon.name) + normalizeToken(icon.drawable)
        if (tokens.any { haystack.contains(it) }) 0 else 1
    }
}

private fun normalizeToken(value: String): String =
    value.lowercase().filter { it.isLetterOrDigit() }
