package dev.openpanel.android

import org.junit.Assert.assertEquals
import org.junit.Test

class OpenPanelTest {

    @Test
    fun testPerformOperation() {
        val openPanel = OpenPanel(OpenPanel.Options(
            clientId = "0f6ceb93-ba13-4106-858e-c44d48229899",
            clientSecret = "sec_9679b09055dcb5b91597",
            waitForProfile = false,
            disabled = false,
            automaticTracking = false
        ))
        openPanel.track("test_event")
        openPanel.initialize()
        println("test")
        Thread.sleep(3000)
        println("test")
        assertEquals("Result for test", "Hej")
    }
}
