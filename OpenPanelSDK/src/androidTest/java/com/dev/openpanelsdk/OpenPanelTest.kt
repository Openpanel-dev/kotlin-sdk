package com.dev.openpanelsdk

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class OpenPanelTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val options = OpenPanel.Options(
            clientId = "0f6ceb93-ba13-4106-858e-c44d48229899", // Replace with your client ID
            clientSecret = "sec_9679b09055dcb5b91597", // Replace with your client secret
            waitForProfile = false,
            disabled = false,
            automaticTracking = false,
            verbose = true
        )
        // Initialize the OpenPanel SDK
        val openPanel = OpenPanel(
            appContext,
            options
        )
        // Test identify
        openPanel.identify(
            "test_user_123", mapOf(
                "firstName" to "Michale",
                "lastName" to "Watson",
                "email" to "michale@example.com"
            )
        )
        // Test track
        openPanel.track(
            "button_clicked_1", mapOf(
                "button_id" to "submit_form",
                "page" to "checkout"
            )
        )
        // Test global properties
        openPanel.setGlobalProperties(
            mapOf(
                "app_version" to "1.0.0",
                "platform" to "iOS"
            )
        )
        // Test track with global properties
        openPanel.track("app_opened")
        // Test increment
        openPanel.increment("test_user_123", "login_count")
        // Test decrement
        openPanel.decrement("test_user_123", "credits_remaining", 5)
        // Wait for a moment to allow async operations to complete
        Thread.sleep(2000)
        println("OpenPanel SDK test completed.")
    }
}