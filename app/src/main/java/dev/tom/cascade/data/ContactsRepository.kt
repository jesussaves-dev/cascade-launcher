package dev.tom.cascade.data

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Phone
import dev.tom.cascade.model.Contact
import dev.tom.cascade.util.uniqueContacts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator

class ContactsRepository(private val context: Context) {
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val loading = java.util.concurrent.atomic.AtomicBoolean(false)

    fun refresh() {
        if (!loading.compareAndSet(false, true)) return
        scope.launch {
            try {
                load()
            } finally {
                loading.set(false)
            }
        }
    }

    fun clear() {
        _contacts.value = emptyList()
    }

    private suspend fun load() = withContext(Dispatchers.IO) {
        val projection = arrayOf(Phone.CONTACT_ID, Phone.DISPLAY_NAME, Phone.NUMBER)
        val found = LinkedHashSet<Contact>()
        runCatching {
            context.contentResolver.query(Phone.CONTENT_URI, projection, null, null, null)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(Phone.CONTACT_ID)
                val nameColumn = cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME)
                val numberColumn = cursor.getColumnIndexOrThrow(Phone.NUMBER)
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameColumn)?.trim().orEmpty()
                    val number = cursor.getString(numberColumn)?.trim().orEmpty()
                    if (name.isEmpty() || number.isEmpty()) continue
                    found += Contact(cursor.getLong(idColumn), name, number)
                }
            }
        }
        val collator = Collator.getInstance()
        _contacts.value = uniqueContacts(found.toList()).sortedWith { a, b -> collator.compare(a.name, b.name) }
    }
}
