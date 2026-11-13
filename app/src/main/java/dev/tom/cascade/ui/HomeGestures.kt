package dev.tom.cascade.ui

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Velocity

/**
 * Turns a long vertical pull on home into a swipe action. As a [NestedScrollConnection] it
 * hears only what the list could not scroll, so a pull fires at the top (down) or the end (up)
 * of the list, and never partway through a gesture that was scrolling it.
 */
class HomeSwipe(private val thresholdPx: Float) : NestedScrollConnection {
    var onSwipeDown: (() -> Unit)? = null
    var onSwipeUp: (() -> Unit)? = null

    private var pull = 0f
    /** Fired already, or the gesture scrolled something; either way it is spent. */
    private var spent = false

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        if (source == NestedScrollSource.UserInput) {
            if (consumed.y != 0f) spent = true else drag(available.y)
        }
        return Offset.Zero
    }

    // Scrollables call this when the finger lifts, fling or not.
    override suspend fun onPreFling(available: Velocity): Velocity {
        end()
        return Velocity.Zero
    }

    fun drag(dy: Float) {
        if (spent || dy == 0f) return
        // A change of direction starts the count again.
        if (pull != 0f && (pull > 0f) != (dy > 0f)) pull = 0f
        pull += dy
        val action = when {
            pull > thresholdPx -> onSwipeDown
            pull < -thresholdPx -> onSwipeUp
            else -> return
        }
        spent = true
        action?.invoke()
    }

    fun end() {
        pull = 0f
        spent = false
    }
}

/** Feeds vertical drags on a part of home that does not scroll (the clock) into [swipe]. */
fun Modifier.homeSwipeArea(swipe: HomeSwipe): Modifier = pointerInput(swipe) {
    detectVerticalDragGestures(
        onDragEnd = swipe::end,
        onDragCancel = swipe::end,
    ) { change, dy ->
        change.consume()
        swipe.drag(dy)
    }
}
