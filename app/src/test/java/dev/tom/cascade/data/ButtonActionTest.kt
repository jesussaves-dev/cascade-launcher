package dev.tom.cascade.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ButtonActionTest {
    @Test fun `round trips search and voice`() {
        assertEquals(ButtonAction.Search, decodeButtonAction(encodeButtonAction(ButtonAction.Search)))
        assertEquals(ButtonAction.Voice, decodeButtonAction(encodeButtonAction(ButtonAction.Voice)))
    }

    @Test fun `round trips an app key containing dots and slashes`() {
        val action = ButtonAction.OpenApp("com.example.camera/.CameraActivity")
        assertEquals(action, decodeButtonAction(encodeButtonAction(action)))
    }

    @Test fun `blank or malformed values fall back`() {
        assertEquals(ButtonAction.Voice, decodeButtonAction(null, ButtonAction.Voice))
        assertEquals(ButtonAction.Search, decodeButtonAction("nonsense"))
        assertEquals(ButtonAction.Search, decodeButtonAction("app:"))
    }
}
