package dev.tom.cascade.util

import dev.tom.cascade.model.Contact
import org.junit.Assert.assertEquals
import org.junit.Test

class ContactSearchTest {
    private fun c(id: Long, name: String, number: String) = Contact(id, name, number)

    @Test fun `matches accents and ranks prefixes first`() {
        val list = listOf(c(1, "Joanne", "111"), c(2, "Élodie", "222"))
        assertEquals(listOf("Élodie"), searchContacts(list, "elodie").map { it.name })
    }

    @Test fun `word start beats contains`() {
        val list = listOf(c(1, "Marjane", "1"), c(2, "Mary Jane", "2"))
        assertEquals(listOf("Mary Jane", "Marjane"), searchContacts(list, "jane").map { it.name })
    }

    @Test fun `digits match the number`() {
        assertEquals(listOf("Joanne"), searchContacts(listOf(c(1, "Joanne", "+44 7700 900123")), "900123").map { it.name })
    }

    @Test fun `limits results and ignores blank queries`() {
        val list = (1..9).map { c(it.toLong(), "Ann $it", "1") }
        assertEquals(5, searchContacts(list, "ann").size)
        assertEquals(emptyList<Contact>(), searchContacts(list, "  "))
    }

    @Test fun `one row per contact even with several numbers`() {
        val list = listOf(c(1, "Dad", "111"), c(1, "Dad", "222"), c(2, "Ann", "333"))
        assertEquals(listOf("Dad", "Ann"), uniqueContacts(list).map { it.name })
        assertEquals("111", uniqueContacts(list).first().number)
    }
}
