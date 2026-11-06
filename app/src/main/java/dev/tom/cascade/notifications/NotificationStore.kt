package dev.tom.cascade.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationStore {
    private val lock = Any()
    private val _byPackage = MutableStateFlow<Map<String, List<NotificationItem>>>(emptyMap())
    val byPackage: StateFlow<Map<String, List<NotificationItem>>> = _byPackage.asStateFlow()

    /** Set by the listener service so the UI can cancel a notification. */
    @Volatile
    var dismisser: ((String) -> Unit)? = null

    fun put(item: NotificationItem) {
        synchronized(lock) {
            val current = _byPackage.value
            val updated = (current[item.packageName].orEmpty().filterNot { it.key == item.key } + item)
                .sortedByDescending { it.timeMillis }
            _byPackage.value = current + (item.packageName to updated)
        }
    }

    fun remove(key: String) {
        synchronized(lock) {
            val current = _byPackage.value
            val packageName = current.entries.firstOrNull { (_, items) ->
                items.any { it.key == key }
            }?.key ?: return
            val remaining = current.getValue(packageName).filterNot { it.key == key }
            _byPackage.value = if (remaining.isEmpty()) {
                current - packageName
            } else {
                current + (packageName to remaining)
            }
        }
    }

    fun replaceAll(items: List<NotificationItem>) {
        synchronized(lock) {
            _byPackage.value = items
                .groupBy { it.packageName }
                .mapValues { (_, group) ->
                    group.distinctBy { it.key }.sortedByDescending { it.timeMillis }
                }
        }
    }

    fun clear() {
        synchronized(lock) { _byPackage.value = emptyMap() }
    }

    fun dismiss(key: String) {
        dismisser?.invoke(key)
        remove(key)
    }

    fun packagesWithNotifications(): Set<String> = byPackage.value.keys
}
