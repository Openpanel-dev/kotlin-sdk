# OpenPanel Kotlin SDK

The OpenPanel Kotlin SDK allows you to track user behavior in your Kotlin applications. This guide provides instructions for installing and using the Kotlin SDK in your project.

## Installation

> ⚠️ This package is not yet published. So you cannot install it with `gradle`

Add the OpenPanel SDK to your project's dependencies:

```gradle
dependencies {
    implementation 'dev.openpanel:openpanel:0.0.1'
}
```

## Usage

### Initialization

First, import the SDK and initialize it with your client ID:

```kotlin
import dev.openpanel.OpenPanel

val op = OpenPanel(OpenPanel.Options(
    clientId = "YOUR_CLIENT_ID",
    clientSecret = "YOUR_CLIENT_SECRET"
))
```

### Options

When initializing the SDK, you can provide several options:

- `clientId` (required): Your OpenPanel client ID.
- `clientSecret` (optional): Your OpenPanel client secret.
- `apiUrl` (optional): Custom API URL if you're not using the default OpenPanel API.
- `waitForProfile` (optional): Wait for a profile to be set before sending events.
- `filter` (optional): A function to filter events before sending.
- `disabled` (optional): Set to `true` to disable event sending.
- `automaticTracking` (optional): Set to `true` to enable automatic app lifecycle tracking.

### Tracking Events

To track an event:

```kotlin
op.track("button_clicked", mapOf("button_id" to "submit_form"))
```

### Identifying Users

To identify a user:

```kotlin
op.identify("user123", mapOf(
    "firstName" to "John",
    "lastName" to "Doe",
    "email" to "john@example.com",
    "customAttribute" to "value"
))
```

### Setting Global Properties

To set properties that will be sent with every event:

```kotlin
op.setGlobalProperties(mapOf(
    "app_version" to "1.0.2",
    "environment" to "production"
))
```

### Creating Aliases

To create an alias for a user:

```kotlin
op.alias("user123", "john_doe")
```

### Incrementing Properties

To increment a numeric property on a user profile:

```kotlin
op.increment("user123", "login_count", 1)
```

### Decrementing Properties

To decrement a numeric property on a user profile:

```kotlin
op.decrement("user123", "credits", 5)
```

### Clearing User Data

To clear the current user's data:

```kotlin
op.clear()
```

## Advanced Usage

### Custom Event Filtering

You can set up custom event filtering:

```kotlin
val op = OpenPanel(OpenPanel.Options(
    clientId = "YOUR_CLIENT_ID",
    filter = { payload ->
        // Your custom filtering logic here
        true // or false to filter out the event
    }
))
```

### Disabling Tracking

You can temporarily disable tracking:

```kotlin
val op = OpenPanel(OpenPanel.Options(
    clientId = "YOUR_CLIENT_ID",
    disabled = true
))
```

## Automatic Tracking

The SDK can automatically track app lifecycle events if `automaticTracking` is set to `true`. This will track "app_opened" and "app_closed" events.

## Thread Safety

The OpenPanel SDK is designed to be thread-safe. You can call its methods from any thread without additional synchronization.

## Support

For any issues or feature requests, please file an issue on our [GitHub repository](https://github.com/Openpanel-dev/openpanel/issues).
