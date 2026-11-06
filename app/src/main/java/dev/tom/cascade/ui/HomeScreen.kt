package dev.tom.cascade.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import dev.tom.cascade.model.AppEntry
import dev.tom.cascade.util.AppSection
import dev.tom.cascade.util.activeLetter
import dev.tom.cascade.util.jumpTarget
import dev.tom.cascade.util.railLetterFor
import dev.tom.cascade.util.railLetters
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    sections: List<AppSection>,
    favorites: List<AppEntry>,
    showBanner: Boolean,
    onDismissBanner: () -> Unit,
    onSetDefault: () -> Unit,
    onLaunch: (AppEntry) -> Unit,
    onLongPress: (AppEntry) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onButtonTap: () -> Unit,
    onButtonSwipeUp: () -> Unit,
    iconFor: (AppEntry) -> ImageBitmap?,
    iconSizeDp: Int,
    notifiedPackages: Set<String> = emptySet(),
) {
    val openSearch by rememberUpdatedState(onOpenSearch)
    val threshold = with(LocalDensity.current) { 48.dp.toPx() }
    var pulled by remember { mutableFloatStateOf(0f) }
    val pullToSearch = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source == NestedScrollSource.UserInput) {
                    if (available.y > 0f) {
                        pulled += available.y
                        if (pulled >= threshold) {
                            pulled = 0f
                            openSearch()
                        }
                    } else if (available.y < 0f) {
                        pulled = 0f
                    }
                }
                return Offset.Zero
            }
        }
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .align(Alignment.TopCenter),
        ) {
    Column(modifier = Modifier.fillMaxSize().padding(end = 30.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                onClick = onOpenSearch,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Search apps",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }

        if (showBanner) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Set Cascade as your default launcher",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onSetDefault) { Text("Set") }
                TextButton(onClick = onDismissBanner) { Text("Dismiss") }
            }
        }

        FavoriteGrid(
            favorites = favorites,
            wide = LocalConfiguration.current.let {
                it.screenWidthDp >= 600 && it.screenHeightDp >= 600
            },
            notifiedPackages = notifiedPackages,
            iconFor = iconFor,
            onLaunch = onLaunch,
            onLongPress = onLongPress,
        )

        if (sections.isEmpty() && favorites.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No apps found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pullToSearch),
            ) {
                sections.forEach { section ->
                    stickyHeader(key = "header-${section.header}") {
                        Text(
                            text = section.header,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                        )
                    }
                    items(section.apps, key = { it.key }) { app ->
                        AppRow(
                            app = app,
                            icon = remember(app.key, iconSizeDp) { iconFor(app) },
                            iconSizeDp = iconSizeDp,
                            hasNotification = app.packageName in notifiedPackages,
                            onClick = { onLaunch(app) },
                            onLongClick = { onLongPress(app) },
                        )
                    }
                }
            }
        }
            }
        CascadeButton(
            onTap = onButtonTap,
            onSwipeUp = onButtonSwipeUp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        )
        val presentLetters = remember(sections) {
            sections.mapNotNull { railLetterFor(it.header) }.toSet()
        }
        if (presentLetters.isNotEmpty()) {
            val activeHeader by remember(sections, listState) {
                derivedStateOf { activeLetter(listState.firstVisibleItemIndex, sections) }
            }
            val active = activeHeader?.let(::railLetterFor)
            AlphabetRail(
                letters = railLetters,
                presentLetters = presentLetters,
                active = active,
                onSelect = { letter ->
                    jumpTarget(letter, sections)?.let { index ->
                        scope.launch { listState.animateScrollToItem(index) }
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
        }
    }
}


