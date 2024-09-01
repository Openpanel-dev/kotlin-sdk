package dev.openpanel

fun main() {
    val openPanel = OpenPanel(OpenPanel.Options(
        clientId = "0f6ceb93-ba13-4106-858e-c44d48229899",
        clientSecret = "sec_9679b09055dcb5b91597",
        waitForProfile = false,
        disabled = false,
        automaticTracking = false
    ))

    // Test identify
    openPanel.identify("test_user_123", mapOf(
        "name" to "John Doe",
        "email" to "john@example.com"
    ))

    // Test track
    openPanel.track("button_clicked_1", mapOf(
        "button_id" to "submit_form",
        "page" to "checkout"
    ))

    // Test global properties
    openPanel.setGlobalProperties(mapOf(
        "app_version" to "1.0.0",
        "platform" to "iOS"
    ))

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