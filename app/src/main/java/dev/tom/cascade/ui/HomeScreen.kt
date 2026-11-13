package dev.tom.cascade.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.tom.cascade.ui.theme.LocalHomeStyle
import dev.tom.cascade.ui.theme.SearchShape
import dev.tom.cascade.ui.theme.SectionMark
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dev.tom.cascade.model.AppEntry
import dev.tom.cascade.model.Folder
import dev.tom.cascade.model.Weather
import dev.tom.cascade.util.AppSection
import dev.tom.cascade.util.HomeEntry
import dev.tom.cascade.util.activeLetter
import dev.tom.cascade.util.jumpTarget
import dev.tom.cascade.util.favoriteTarget
import dev.tom.cascade.util.moveFavorite
import dev.tom.cascade.util.railLetterFor
import dev.tom.cascade.util.railLetters
import kotlinx.coroutines.launch

private fun reorderFavorites(list: List<AppEntry>, from: Int, to: Int): List<AppEntry> {
    val byKey = list.associateBy { it.key }
    return moveFavorite(list.map { it.key }, from, to).mapNotNull(byKey::get)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    sections: List<AppSection>,
    favorites: List<AppEntry>,
    onFavoriteMoved: (List<String>) -> Unit = {},
    showBanner: Boolean,
    onDismissBanner: () -> Unit,
    onSetDefault: () -> Unit,
    onLaunch: (AppEntry) -> Unit,
    onLongPress: (AppEntry) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    iconFor: (AppEntry) -> ImageBitmap?,
    iconSizeDp: Int,
    notifiedPackages: Set<String> = emptySet(),
    folderContents: Map<String, List<AppEntry>> = emptyMap(),
    onFolderOpen: (Folder) -> Unit = {},
    onFolderLongPress: (Folder) -> Unit = {},
    widgets: @Composable () -> Unit = {},
    weather: Weather? = null,
    showWeather: Boolean = false,
    showVerse: Boolean = false,
    fahrenheit: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    onClockDoubleTap: () -> Unit = {},
    onClockLongPress: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var favoriteOrder by remember(favorites) { mutableStateOf(favorites) }
    // The list order stays frozen while a row is lifted; only the visuals move.
    var dragKey by remember { mutableStateOf<String?>(null) }
    var dragFrom by remember { mutableIntStateOf(0) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var dragRowHeight by remember { mutableFloatStateOf(0f) }
    val style = LocalHomeStyle.current
    val ground = style.ground
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (ground != null) {
                    Modifier.background(ground, alpha = style.scrim)
                } else {
                    Modifier.background(MaterialTheme.colorScheme.background.copy(alpha = style.scrim))
                },
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        // An unfolded Fold (or a phone on its side) gets two pages: clock left, apps right.
        val twoPane = maxWidth >= TwoPaneMinWidth
        val searchBar: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val rule = MaterialTheme.colorScheme.outline
            val field = when (style.searchShape) {
                SearchShape.Pill -> Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onOpenSearch, onClickLabel = "Search apps")
                    .padding(horizontal = 16.dp)
                SearchShape.Box -> Modifier
                    .border(2.dp, rule, RectangleShape)
                    .clickable(onClick = onOpenSearch, onClickLabel = "Search apps")
                    .padding(horizontal = 12.dp)
                SearchShape.Underline -> Modifier
                    .clickable(onClick = onOpenSearch, onClickLabel = "Search apps")
                    .drawBehind {
                        val y = size.height - 1.dp.toPx() / 2
                        drawLine(rule, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                    }
                    .padding(horizontal = 4.dp)
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .then(field),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(if (style.searchShape == SearchShape.Pill) 24.dp else 20.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Search apps",
                    style = style.search,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onOpenSettings) {
                Icon(
                    if (style.searchShape == SearchShape.Pill && style.railStrip) {
                        Icons.Filled.Settings
                    } else {
                        Icons.Outlined.Settings
                    },
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        }
        val banner: @Composable () -> Unit = {
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

        }
        val header: @Composable () -> Unit = {
        ClockHeader(
            weather = weather,
            showWeather = showWeather,
            showVerse = showVerse,
            fahrenheit = fahrenheit,
            onDoubleTap = onClockDoubleTap,
            onLongPress = onClockLongPress,
        )

        }
        val appList: @Composable () -> Unit = {
        if (sections.isEmpty() && favorites.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No apps found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            // Only the header stuck to the top needs a ground; the rest sit on the wallpaper.
            val pinnedHeader by remember(sections, listState, favorites.isEmpty(), favorites.size) {
                derivedStateOf {
                    val leading = if (favorites.isEmpty()) 0 else favorites.size + 1
                    activeLetter(listState.firstVisibleItemIndex, sections, leading)
                }
            }
            LazyColumn(
                state = listState,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                if (favorites.isNotEmpty()) {
                    item(key = "favorites-header") {
                        if (!style.favoritesAsStar) {
                            SectionHeader("Favorites", pinned = false)
                            return@item
                        }
                        // The same star as the rail, so the two read as one control.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, top = 4.dp, bottom = 4.dp)
                                .semantics { heading(); contentDescription = "Favourites" },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    itemsIndexed(
                        favoriteOrder,
                        key = { _, app -> "fav:${app.key}" },
                        contentType = { _, _ -> "favorite" },
                    ) { index, app ->
                        val lifted = dragKey == app.key
                        val target = favoriteTarget(dragFrom, dragOffset, dragRowHeight, favoriteOrder.size)
                        val shift = when {
                            dragKey == null -> 0f
                            lifted -> dragOffset
                            dragFrom < target && index in (dragFrom + 1)..target -> -dragRowHeight
                            dragFrom > target && index in target until dragFrom -> dragRowHeight
                            else -> 0f
                        }
                        val placeShift by animateFloatAsState(shift, label = "favoriteShift")
                        FavoriteRow(
                            app = app,
                            icon = remember(app.key, iconSizeDp) { iconFor(app) },
                            iconSizeDp = iconSizeDp,
                            hasNotification = app.packageName in notifiedPackages,
                            showHandle = dragKey != null,
                            dragging = lifted,
                            offsetPx = if (lifted) dragOffset else placeShift,
                            onDragStart = { height ->
                                dragKey = app.key
                                dragFrom = index
                                dragOffset = 0f
                                dragRowHeight = height
                            },
                            onDrag = { dy -> dragOffset += dy },
                            onDragEnd = {
                                val to = favoriteTarget(dragFrom, dragOffset, dragRowHeight, favoriteOrder.size)
                                if (to != dragFrom) {
                                    favoriteOrder = reorderFavorites(favoriteOrder, dragFrom, to)
                                    onFavoriteMoved(favoriteOrder.map { it.key })
                                }
                                dragKey = null
                                dragOffset = 0f
                            },
                            onClick = { onLaunch(app) },
                            onLongClick = { onLongPress(app) },
                            onMoveUp = {
                                val from = favoriteOrder.indexOfFirst { it.key == app.key }
                                if (from > 0) {
                                    favoriteOrder = reorderFavorites(favoriteOrder, from, from - 1)
                                    onFavoriteMoved(favoriteOrder.map { it.key })
                                }
                            },
                            onMoveDown = {
                                val from = favoriteOrder.indexOfFirst { it.key == app.key }
                                if (from in 0 until favoriteOrder.lastIndex) {
                                    favoriteOrder = reorderFavorites(favoriteOrder, from, from + 1)
                                    onFavoriteMoved(favoriteOrder.map { it.key })
                                }
                            },
                            // No placement animation while lifted, so slots snap instead of lagging.
                            modifier = if (dragKey != null) {
                                Modifier.zIndex(if (lifted) 1f else 0f)
                            } else {
                                Modifier.animateItem()
                            },
                        )
                    }
                }
                if (sections.isEmpty()) {
                    item(key = "empty") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("No apps found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                sections.forEach { section ->
                    stickyHeader(key = "header-${section.header}") {
                        SectionHeader(section.header, pinned = section.header == pinnedHeader)
                    }
                    items(section.items, key = { it.key }) { entry ->
                        when (entry) {
                            is HomeEntry.AppItem -> AppRow(
                                app = entry.app,
                                icon = remember(entry.app.key, iconSizeDp) { iconFor(entry.app) },
                                iconSizeDp = iconSizeDp,
                                hasNotification = entry.app.packageName in notifiedPackages,
                                onClick = { onLaunch(entry.app) },
                                onLongClick = { onLongPress(entry.app) },
                            )
                            is HomeEntry.FolderItem -> FolderRow(
                                folder = entry.folder,
                                apps = folderContents[entry.folder.id].orEmpty(),
                                iconFor = iconFor,
                                onClick = { onFolderOpen(entry.folder) },
                                onLongClick = { onFolderLongPress(entry.folder) },
                            )
                        }
                    }
                }
            }
        }
        }
        val rail: @Composable BoxScope.() -> Unit = {
        val presentLetters = remember(sections) {
            sections.mapNotNull { railLetterFor(it.header) }.toSet()
        }
        val leadingItems = if (favorites.isEmpty()) 0 else favorites.size + 1
        if (presentLetters.isNotEmpty()) {
            val activeHeader by remember(sections, listState, leadingItems) {
                derivedStateOf {
                    activeLetter(listState.firstVisibleItemIndex, sections, leadingItems)
                }
            }
            val active = activeHeader?.let(::railLetterFor)
            AlphabetRail(
                letters = railLetters,
                presentLetters = presentLetters,
                active = active,
                onFavorites = { scope.launch { listState.animateScrollToItem(0) } },
                onSelect = { letter ->
                    jumpTarget(letter, sections, leadingItems)?.let { index ->
                        scope.launch { listState.animateScrollToItem(index) }
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
        }

        if (twoPane) {
            Row(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 24.dp),
                ) {
                    header()
                    widgets()
                }
                // The gutter between the two pages, where the hinge runs.
                Box(
                    Modifier
                        .fillMaxHeight()
                        .padding(vertical = 32.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    Column(Modifier.fillMaxSize().padding(end = 44.dp)) {
                        searchBar()
                        banner()
                        appList()
                    }
                    rail()
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxSize()
                    .align(Alignment.TopCenter),
            ) {
                Column(Modifier.fillMaxSize().padding(end = 44.dp)) {
                    searchBar()
                    banner()
                    header()
                    widgets()
                    appList()
                }
                rail()
            }
        }
    }
}

/** Wide enough for two readable pages; the Fold's inner screen and most phones on their side. */
private val TwoPaneMinWidth = 600.dp

/** A section's letter in the theme's manner: 1.0 label, large initial, `> A` prompt, or ruled. */
@Composable
private fun SectionHeader(letter: String, pinned: Boolean) {
    val style = LocalHomeStyle.current
    val background = MaterialTheme.colorScheme.background
    val accent = MaterialTheme.colorScheme.primary
    val mark = style.section
    val ground = if (mark == SectionMark.Label) {
        // 1.0 kept every header on a solid strip.
        Modifier.background(background)
    } else {
        val g = background.copy(alpha = if (pinned) 1f else 0f)
        // Solid behind the letter, fading out so rows slide under it softly.
        Modifier.background(Brush.verticalGradient(0.7f to g, 1f to g.copy(alpha = 0f)))
    }
    val padding = if (mark == SectionMark.Label) {
        Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    } else {
        Modifier.padding(start = 20.dp, top = 14.dp, bottom = 6.dp)
    }
    Box(Modifier.fillMaxWidth().then(ground).then(padding).semantics(mergeDescendants = true) { heading() }) {
        Text(
            text = if (mark == SectionMark.Prompt) "> $letter" else letter,
            style = style.initial,
            color = accent,
            modifier = if (mark == SectionMark.Ruled) {
                val ink = MaterialTheme.colorScheme.onBackground
                Modifier
                    .drawBehind {
                        val y = size.height + 2.dp.toPx()
                        drawLine(ink, Offset(0f, y), Offset(size.width, y), strokeWidth = 2.dp.toPx())
                    }
                    .padding(end = 24.dp)
            } else {
                Modifier
            },
        )
    }
}
