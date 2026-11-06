package dev.tom.cascade.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationStoreTest {
    private fun item(key: String, pkg: String, time: Long = 0L, dismissible: Boolean = true) =
        NotificationItem(key, pkg, "Title", "Text", time, ongoing = false, dismissible = dismissible)

    @Test fun `items are grouped by package in newest-first order`() {
        NotificationStore.clear()
        NotificationStore.put(item("a", "com.a", 1))
        NotificationStore.put(item("b", "com.a", 2))
        NotificationStore.put(item("c", "com.b", 3))
        val grouped = NotificationStore.byPackage.value
        assertEquals(listOf("b", "a"), grouped.getValue("com.a").map { it.key })
        assertEquals(setOf("com.a", "com.b"), NotificationStore.packagesWithNotifications())
    }

    @Test fun `same key replaces instead of duplicating`() {
        NotificationStore.clear()
        NotificationStore.put(item("a", "com.a", 1))
        NotificationStore.put(item("a", "com.a", 9))
        assertEquals(1, NotificationStore.byPackage.value.getValue("com.a").size)
        assertEquals(9L, NotificationStore.byPackage.value.getValue("com.a").first().timeMillis)
    }

    @Test fun `removing the last item drops the package`() {
        NotificationStore.clear()
        NotificationStore.put(item("a", "com.a"))
        NotificationStore.remove("a")
        assertTrue(NotificationStore.byPackage.value.isEmpty())
        assertTrue(NotificationStore.packagesWithNotifications().isEmpty())
    }

    @Test fun `replaceAll rebuilds from the active set`() {
        NotificationStore.clear()
        NotificationStore.put(item("old", "com.a"))
        NotificationStore.replaceAll(listOf(item("new", "com.b")))
        assertEquals(setOf("com.b"), NotificationStore.packagesWithNotifications())
    }

    @Test fun `dismiss removes optimistically and calls the listener`() {
        NotificationStore.clear()
        var dismissed: String? = null
        NotificationStore.dismisser = { dismissed = it }
        NotificationStore.put(item("a", "com.a"))
        NotificationStore.dismiss("a")
        assertEquals("a", dismissed)
        assertTrue(NotificationStore.byPackage.value.isEmpty())
        NotificationStore.dismisser = null
    }
}
