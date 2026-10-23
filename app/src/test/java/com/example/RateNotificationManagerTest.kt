package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.RateNotificationManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RateNotificationManagerTest {

    @Test
    fun testOptInStateToggleAndPersistence() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = RateNotificationManager(context)

        // Default state
        assertTrue("Default opt-in should be enabled", manager.isOptedIn.value)

        // Toggle off
        manager.setOptedIn(false)
        assertFalse("Opt-in should now be false", manager.isOptedIn.value)

        // Verify persistence in new instance
        val manager2 = RateNotificationManager(context)
        assertFalse("New instance should preserve false opt-in", manager2.isOptedIn.value)

        // Toggle back on
        manager2.setOptedIn(true)
        assertTrue("Opt-in should now be true", manager2.isOptedIn.value)
    }

    @Test
    fun testNotificationDoesNotCrashWhenOptedOut() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = RateNotificationManager(context)
        manager.setOptedIn(false)

        // Should cleanly return without crashing
        manager.sendRateUpdateNotification(
            benchmarkRate = 33.5,
            trendDelta = -0.2,
            topBrandName = "Longi",
            topBrandRate = 33.2
        )
    }
}
