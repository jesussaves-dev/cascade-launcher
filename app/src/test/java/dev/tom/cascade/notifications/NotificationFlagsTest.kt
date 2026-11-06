package dev.tom.cascade.notifications

import android.app.Notification
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationFlagsTest {
    @Test fun `ongoing and foreground notifications are ongoing`() {
        assertTrue(isOngoing(Notification.FLAG_ONGOING_EVENT))
        assertTrue(isOngoing(Notification.FLAG_FOREGROUND_SERVICE))
        assertFalse(isOngoing(0))
    }

    @Test fun `plain notifications are dismissible`() {
        assertTrue(isDismissible(0))
    }

    @Test fun `ongoing or no-clear notifications are not dismissible`() {
        assertFalse(isDismissible(Notification.FLAG_ONGOING_EVENT))
        assertFalse(isDismissible(Notification.FLAG_FOREGROUND_SERVICE))
        assertFalse(isDismissible(Notification.FLAG_NO_CLEAR))
    }

    @Test fun `group summaries are detected`() {
        assertTrue(isGroupSummary(Notification.FLAG_GROUP_SUMMARY))
        assertFalse(isGroupSummary(Notification.FLAG_ONGOING_EVENT))
    }
}
