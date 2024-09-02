package dev.openpanel.android

import org.junit.Assert.assertEquals
import org.junit.Test

class OpenPanelTest {

    @Test
    fun testPerformOperation() {
        println("test")
        val openPanel = OpenPanel(OpenPanel.Options(
            clientId = "",
            clientSecret = "",
            waitForProfile = false,
            disabled = false,
            automaticTracking = false
        ))
        openPanel.track("test_event")
        Thread.sleep(3000)
        assertEquals("Result for test", "Hej")
    }
}
