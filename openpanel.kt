import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Build
import android.webkit.WebView
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentLinkedQueue

// MARK: - DeviceInfo

internal object DeviceInfo {
    fun getUserAgent(): String {
        val webView = WebView(/* context */)
        var userAgent = webView.settings.userAgentString

        if (userAgent.isNullOrEmpty()) {
            userAgent = "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}; ${Build.MODEL}) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36"
        }

        return "$userAgent OpenPanelKotlin/${OpenPanel.sdkVersion}"
    }
}

// MARK: - Payload Types

typealias Properties = Map<String, Any>

sealed class Payload {
    abstract fun toJson(): JSONObject
}

data class TrackPayload(
    val name: String,
    val properties: Properties? = null,
    val profileId: String? = null
) : Payload() {
    override fun toJson(): JSONObject = JSONObject().apply {
        put("type", "track")
        put("name", name)
        properties?.let { put("properties", JSONObject(it)) }
        profileId?.let { put("profileId", it) }
    }
}

data class IdentifyPayload(
    val profileId: String,
    val traits: Properties? = null
) : Payload() {
    override fun toJson(): JSONObject = JSONObject().apply {
        put("type", "identify")
        put("profileId", profileId)
        traits?.let { put("traits", JSONObject(it)) }
    }
}

data class AliasPayload(
    val profileId: String,
    val alias: String
) : Payload() {
    override fun toJson(): JSONObject = JSONObject().apply {
        put("type", "alias")
        put("profileId", profileId)
        put("alias", alias)
    }
}

data class IncrementPayload(
    val profileId: String,
    val property: String,
    val value: Int? = null
) : Payload() {
    override fun toJson(): JSONObject = JSONObject().apply {
        put("type", "increment")
        put("profileId", profileId)
        put("property", property)
        value?.let { put("value", it) }
    }
}

data class DecrementPayload(
    val profileId: String,
    val property: String,
    val value: Int? = null
) : Payload() {
    override fun toJson(): JSONObject = JSONObject().apply {
        put("type", "decrement")
        put("profileId", profileId)
        put("property", property)
        value?.let { put("value", it) }
    }
}

// MARK: - OpenPanel Class

class OpenPanel(private val options: Options) {
    private val api: Api
    private var profileId: String? = null
    private val globalProperties = ConcurrentLinkedQueue<Pair<String, Any>>()
    private var queue = ConcurrentLinkedQueue<Payload>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    data class Options(
        val clientId: String,
        var clientSecret: String? = null,
        var apiUrl: String? = null,
        var waitForProfile: Boolean? = null,
        var filter: ((Payload) -> Boolean)? = null,
        var disabled: Boolean? = null,
        var automaticTracking: Boolean? = null
    )

    companion object {
        const val sdkVersion = "0.0.1"
    }

    init {
        val defaultHeaders = mutableMapOf(
            "openpanel-client-id" to options.clientId,
            "openpanel-sdk-name" to "kotlin",
            "openpanel-sdk-version" to sdkVersion,
            "user-agent" to DeviceInfo.getUserAgent()
        )

        options.clientSecret?.let { defaultHeaders["openpanel-client-secret"] = it }

        api = Api(
            Api.Config(
                baseUrl = options.apiUrl ?: "https://api.openpanel.dev",
                defaultHeaders = defaultHeaders
            )
        )

        if (options.automaticTracking == true) {
            setupAutomaticTracking()
        }
    }

    fun ready() {
        options.waitForProfile = false
        flush()
    }

    private fun send(payload: Payload) {
        if (options.disabled == true) return
        if (options.filter?.invoke(payload) == false) return
        if (options.waitForProfile == true && profileId == null && payload !is IdentifyPayload) {
            queue.add(payload)
            return
        }

        coroutineScope.launch {
            val updatedPayload = ensureProfileId(payload)
            when (val result = api.fetch("/track", updatedPayload.toJson())) {
                is Result.Success -> { /* Handle success if needed */ }
                is Result.Failure -> logError("Error sending payload: ${result.error}")
            }
        }
    }

    private fun ensureProfileId(payload: Payload): Payload {
        return when (payload) {
            is TrackPayload -> payload.copy(profileId = payload.profileId ?: this.profileId)
            else -> payload
        }
    }

    fun setGlobalProperties(properties: Properties) {
        globalProperties.addAll(properties.map { it.key to it.value })
    }

    fun track(name: String, properties: Properties? = null) {
        val mergedProperties = (globalProperties.toMap() + (properties ?: emptyMap())).toMutableMap()
        send(TrackPayload(
            name = name,
            properties = mergedProperties,
            profileId = properties?.get("profileId") as? String ?: profileId
        ))
    }

    fun identify(profileId: String, traits: Properties? = null) {
        this.profileId = profileId
        flush()

        val mergedTraits = (globalProperties.toMap() + (traits ?: emptyMap())).toMutableMap()
        send(IdentifyPayload(profileId = profileId, traits = mergedTraits))
    }

    fun alias(profileId: String, alias: String) {
        send(AliasPayload(profileId = profileId, alias = alias))
    }

    fun increment(profileId: String, property: String, value: Int? = null) {
        send(IncrementPayload(profileId = profileId, property = property, value = value))
    }

    fun decrement(profileId: String, property: String, value: Int? = null) {
        send(DecrementPayload(profileId = profileId, property = property, value = value))
    }

    fun clear() {
        profileId = null
        globalProperties.clear()
    }

    fun flush() {
        val currentQueue = queue.toList()
        queue.clear()
        currentQueue.forEach { send(it) }
    }

    private fun setupAutomaticTracking() {
        if (isAndroidEnvironment()) {
            try {
                val application = Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication")
                    .invoke(null) as? Application

                application?.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                    private var numStarted = 0

                    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
                    override fun onActivityStarted(activity: Activity) {
                        if (numStarted == 0) {
                            track("app_opened")
                        }
                        numStarted++
                    }
                    override fun onActivityResumed(activity: Activity) {}
                    override fun onActivityPaused(activity: Activity) {}
                    override fun onActivityStopped(activity: Activity) {
                        numStarted--
                        if (numStarted == 0) {
                            track("app_closed")
                        }
                    }
                    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                    override fun onActivityDestroyed(activity: Activity) {}
                })
            } catch (e: Exception) {
                logError("Failed to setup automatic tracking: ${e.message}")
            }
        }
    }

    private fun isAndroidEnvironment(): Boolean {
        return try {
            Class.forName("android.app.Activity")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    private fun logError(message: String) {
        println("OpenPanel Error: $message")
    }
}

// MARK: - Api Class

internal class Api(private val config: Config) {
    data class Config(
        val baseUrl: String,
        val defaultHeaders: Map<String, String>? = null,
        val maxRetries: Int = 3,
        val initialRetryDelay: Long = 500
    )

    private val headers = (config.defaultHeaders ?: emptyMap()).toMutableMap()

    init {
        headers["Content-Type"] = "application/json"
    }

    fun addHeader(key: String, value: String) {
        headers[key] = value
    }

    suspend fun fetch(path: String, data: JSONObject, options: Map<String, Any> = emptyMap()): Result<String> {
        return withContext(Dispatchers.IO) {
            var attempt = 0
            var lastError: Exception? = null

            while (attempt < config.maxRetries) {
                try {
                    val url = URL(config.baseUrl + path)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
                    options.forEach { (key, value) ->
                        if (value is String) connection.setRequestProperty(key, value)
                    }

                    connection.doOutput = true
                    connection.outputStream.use { it.write(data.toString().toByteArray()) }

                    val responseCode = connection.responseCode
                    if (responseCode in 200..299) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        return@withContext Result.Success(response)
                    } else {
                        throw Exception("HTTP error: $responseCode")
                    }
                } catch (e: Exception) {
                    lastError = e
                    attempt++
                    if (attempt < config.maxRetries) {
                        delay(config.initialRetryDelay * (1 shl (attempt - 1)))
                    }
                }
            }

            Result.Failure(lastError ?: Exception("Unknown error"))
        }
    }
}

sealed class Result<out T> {
    data class Success<out T>(val value: T) : Result<T>()
    data class Failure(val error: Exception) : Result<Nothing>()
}
