package dev.tom.cascade.data

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetIdsTest {
    @Test fun `round trips ids`() {
        assertEquals(listOf(12, 3), WidgetIds.decode(WidgetIds.encode(listOf(12, 3))))
    }

    @Test fun `drops malformed, blank, negative and duplicate ids`() {
        assertEquals(listOf(7, 9), WidgetIds.decode("7, x,,9,7,-1,0"))
    }

    @Test fun `blank values decode empty`() {
        assertEquals(emptyList<Int>(), WidgetIds.decode(null))
        assertEquals(emptyList<Int>(), WidgetIds.decode(""))
        assertEquals(emptyList<Int>(), WidgetIds.decode(" , "))
    }
}
