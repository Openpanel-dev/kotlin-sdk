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

val op = OpenPanel.create(
    context,
    OpenPanel.Options(
    clientId = "YOUR_CLIENT_ID",
    clientSecret = "YOUR_CLIENT_SECRET"
))
```

## Parameters

### `context`
- **Type**: `Context`
- **Required**: Yes
- **Description**: Android `Context` used for initializing the SDK.

### `Options`
- **Type**: `OpenPanel.Options`
- **Required**: Yes
- **Description**: Configuration options for the SDK. Contains parameters for client ID, client secret, and other settings.

#### Options Fields:
- `clientId`: Required, your OpenPanel client ID.
- `clientSecret`: Optional, your OpenPanel client secret.
- `apiUrl`: Optional, custom API URL.
- `waitForProfile`: Optional, delays sending events until profile is set.
- `filter`: Optional, filters events before sending.
- `disabled`: Optional, disables event sending.
- `automaticTracking`: Optional, enables automatic tracking.
- `verbose`: Optional, enables verbose logging.

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

### System Information

The SDK automatically gathers system information and adds it to the properties of every tracking event. This includes:

- OS details (e.g., `os`, `os_version`)
- Device manufacturer, brand, and model (e.g., `manufacturer`, `brand`, `model`)
- Screen resolution and DPI (e.g., `screen_width`, `screen_height`, `screen_dpi`)
- App version (e.g., `app_version`, `app_build_number`)
- Network details (e.g., `wifi`, `carrier`, `bluetooth_enabled`)

## Thread Safety

The OpenPanel SDK is designed to be thread-safe. You can call its methods from any thread without additional synchronization.

## Support

For any issues or feature requests, please file an issue on our [GitHub repository](https://github.com/Openpanel-dev/openpanel/issues).
