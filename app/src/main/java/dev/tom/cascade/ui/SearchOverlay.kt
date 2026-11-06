package dev.tom.cascade.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.tom.cascade.model.AppEntry
import dev.tom.cascade.model.Contact
import dev.tom.cascade.util.searchApps
import dev.tom.cascade.util.searchContacts

@Composable
fun SearchOverlay(
    apps: List<AppEntry>,
    contacts: List<Contact> = emptyList(),
    iconFor: (AppEntry) -> ImageBitmap?,
    iconSizeDp: Int,
    onLaunch: (AppEntry) -> Unit,
    onOpenContact: (Contact) -> Unit = {},
    onWebSearch: (String) -> Unit = {},
    onDismiss: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(apps, query) { searchApps(apps, query, emptySet()) }
    val contactResults = remember(contacts, query) { searchContacts(contacts, query) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    BackHandler(onBack = onDismiss)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = { Text("Search apps and contacts") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { results.firstOrNull()?.let(onLaunch) },
                    ),
                )
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }

            if (results.isEmpty() && contactResults.isEmpty() && query.isBlank()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No apps found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(results, key = { it.key }) { app ->
                        AppRow(
                            app = app,
                            icon = remember(app.key, iconSizeDp) { iconFor(app) },
                            iconSizeDp = iconSizeDp,
                            hasNotification = false,
                            onClick = { onLaunch(app) },
                            onLongClick = {},
                        )
                    }
                    if (contactResults.isNotEmpty()) {
                        item(key = "contacts-header") {
                            Text(
                                text = "Contacts",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            )
                        }
                        items(contactResults, key = { "contact:${it.key}" }) { contact ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics(mergeDescendants = true) {}
                                    .clickable(role = Role.Button) { onOpenContact(contact) }
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                            ) {
                                Text(text = contact.name, maxLines = 1)
                                Text(
                                    text = contact.number,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
                if (query.isNotBlank()) {
                    Text(
                        text = "Search the web for \u201C$query\u201D",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {}
                            .clickable(role = Role.Button) { onWebSearch(query) }
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                    )
                }
            }
        }
    }
}
