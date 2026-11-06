package dev.tom.cascade.util

import dev.tom.cascade.model.Contact
import java.text.Normalizer

fun searchContacts(contacts: List<Contact>, query: String, limit: Int = 5): List<Contact> {
    val q = normalize(query)
    if (q.isBlank()) return emptyList()
    val digits = q.filter { it.isDigit() }
    return contacts
        .mapNotNull { contact -> rank(contact, q, digits)?.let { contact to it } }
        .sortedWith(compareBy({ it.second }, { it.first.name.lowercase() }))
        .take(limit)
        .map { it.first }
}

private fun rank(contact: Contact, query: String, digits: String): Int? {
    val name = normalize(contact.name)
    return when {
        name.startsWith(query) -> 0
        name.split(' ').any { it.startsWith(query) } -> 1
        name.contains(query) -> 2
        digits.isNotEmpty() && contact.number.filter { it.isDigit() }.contains(digits) -> 3
        else -> null
    }
}

private fun normalize(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .lowercase()
        .trim()
