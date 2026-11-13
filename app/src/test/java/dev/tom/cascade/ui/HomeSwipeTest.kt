package dev.tom.cascade.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeSwipeTest {
    private var downs = 0
    private var ups = 0
    private val swipe = HomeSwipe(thresholdPx = 100f).apply {
        onSwipeDown = { downs++ }
        onSwipeUp = { ups++ }
    }

    private fun overscroll(dy: Float) =
        swipe.onPostScroll(Offset.Zero, Offset(0f, dy), NestedScrollSource.UserInput)

    @Test fun `a long pull past the top fires once per gesture`() {
        repeat(10) { overscroll(30f) }
        assertEquals(1, downs)
        swipe.end()
        repeat(4) { overscroll(30f) }
        assertEquals(2, downs)
    }

    @Test fun `a gesture that scrolled the list never fires`() {
        swipe.onPostScroll(Offset(0f, 40f), Offset.Zero, NestedScrollSource.UserInput)
        repeat(10) { overscroll(30f) }
        assertEquals(0, downs)
    }

    @Test fun `changing direction starts the count again`() {
        overscroll(90f)
        overscroll(-20f)
        overscroll(90f)
        assertEquals(0, downs)
        repeat(3) { overscroll(-40f) }
        assertEquals(1, ups)
    }

    @Test fun `flings do not count`() {
        repeat(10) { swipe.onPostScroll(Offset.Zero, Offset(0f, 30f), NestedScrollSource.SideEffect) }
        assertEquals(0, downs)
    }
}
