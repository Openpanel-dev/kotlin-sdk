package dev.openpanel

import org.json.JSONObject
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.net.HttpURLConnection
import java.net.URL

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
        put("payload", JSONObject().apply {
            put("name", name)
            properties?.let { put("properties", JSONObject(it)) }
            profileId?.let { put("profileId", it) }
        })
    }
}

data class IdentifyPayload(
    val profileId: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val avatar: String? = null,
    val properties: Properties? = null
) : Payload() {
    override fun toJson(): JSONObject = JSONObject().apply {
        put("type", "identify")
        put("payload", JSONObject().apply {
            put("profileId", profileId)
            firstName?.let { put("firstName", it) }
            lastName?.let { put("lastName", it) }
            email?.let { put("email", it) }
            avatar?.let { put("avatar", it) }
            properties?.let { put("properties", JSONObject(it)) }
        })
    }
}

data class AliasPayload(
    val profileId: String,
    val alias: String
) : Payload() {
    override fun toJson(): JSONObject = JSONObject().apply {
        put("type", "alias")
        put("payload", JSONObject().apply {
            put("profileId", profileId)
            put("alias", alias)
        })
    }
}

data class IncrementPayload(
    val profileId: String,
    val property: String,
    val value: Int? = null
) : Payload() {
    override fun toJson(): JSONObject = JSONObject().apply {
        put("type", "increment")
        put("payload", JSONObject().apply {
            put("profileId", profileId)
            put("property", property)
            value?.let { put("value", it) }
        })
    }
}

data class DecrementPayload(
    val profileId: String,
    val property: String,
    val value: Int? = null
) : Payload() {
    override fun toJson(): JSONObject = JSONObject().apply {
        put("type", "decrement")
        put("payload", JSONObject().apply {
            put("profileId", profileId)
            put("property", property)
            value?.let { put("value", it) }
        })
    }
}

// MARK: - OpenPanel Class

open class OpenPanel(protected val options: Options) {
    protected val api: Api
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
        var automaticTracking: Boolean? = null,
        var verbose: Boolean = false  // Add this line
    )

    companion object {
        const val sdkVersion = "0.0.1"

        fun create(options: Options): OpenPanel {
            return OpenPanel(options)
        }

        private fun isAndroidEnvironment(): Boolean {
            return try {
                Class.forName("android.app.Activity")
                true
            } catch (e: ClassNotFoundException) {
                false
            }
        }
    }

    open fun getUserAgent(): String {
        // Remove WebView-specific code
        return "OpenPanelKotlin/${OpenPanel.sdkVersion}"
    }

    init {
        val defaultHeaders = mutableMapOf(
            "openpanel-client-id" to options.clientId,
            "openpanel-sdk-name" to "kotlin",
            "openpanel-sdk-version" to sdkVersion,
            "user-agent" to getUserAgent()
        )

        options.clientSecret?.let { defaultHeaders["openpanel-client-secret"] = it }

        api = Api(
            Api.Config(
                baseUrl = options.apiUrl ?: "https://api.openpanel.dev",
                defaultHeaders = defaultHeaders,
                verbose = options.verbose  // Add this line
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
        send(IdentifyPayload(
            profileId = profileId,
            firstName = mergedTraits["firstName"] as? String,
            lastName = mergedTraits["lastName"] as? String,
            email = mergedTraits["email"] as? String,
            avatar = mergedTraits["avatar"] as? String,
            properties = mergedTraits.filterKeys { it !in setOf("firstName", "lastName", "email", "avatar") }
        ))
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

    open fun setupAutomaticTracking() {
        // Base implementation (empty)
    }

    open fun isAndroidEnvironment(): Boolean = false

    private fun logVerbose(message: String) {
        if (options.verbose) {
            println("OpenPanel: $message")
        }
    }

    private fun logError(message: String) {
        println("OpenPanel Error: $message")
    }
}

// MARK: - Api Class

class Api(private val config: Config) {
    data class Config(
        val baseUrl: String,
        val defaultHeaders: Map<String, String>? = null,
        val maxRetries: Int = 3,
        val initialRetryDelay: Long = 500,
        val verbose: Boolean = false  // Add this line
    )

    private val headers = (config.defaultHeaders ?: emptyMap()).toMutableMap()

    init {
        headers["Content-Type"] = "application/json"
    }

    fun addHeader(key: String, value: String) {
        headers[key] = value
    }

    private fun logVerbose(message: String) {
        if (config.verbose) {
            println("OpenPanel: $message")
        }
    }

    suspend fun fetch(path: String, data: JSONObject, options: Map<String, Any> = emptyMap()): Result<String> {
        logVerbose("Fetching data from $path")
        return withContext(Dispatchers.IO) {
            var attempt = 0
            var lastError: Exception? = null

            while (attempt < config.maxRetries) {
                try {
                    logVerbose("Attempt ${attempt + 1} of ${config.maxRetries}")
                    val url = URL(config.baseUrl + path)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.connectTimeout = 5000 // 5 seconds timeout
                    connection.readTimeout = 5000 // 5 seconds timeout
                    headers.forEach { (key, value) -> 
                        connection.setRequestProperty(key, value)
                        logVerbose("Setting header $key: $value")
                    }
                    options.forEach { (key, value) ->
                        if (value is String) {
                            connection.setRequestProperty(key, value)
                            logVerbose("Setting option $key: $value")
                        }
                    }

                    connection.doOutput = true
                    connection.outputStream.use { it.write(data.toString().toByteArray()) }
                    logVerbose("Sending data: ${data.toString()}")

                    val responseCode = connection.responseCode
                    logVerbose("Response code: $responseCode")
                    if (responseCode in 200..299) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        return@withContext Result.Success(response)
                    } else {
                        val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        logVerbose("Error response: $errorResponse")
                        throw Exception("HTTP error: $responseCode, Error response: $errorResponse")
                    }
                } catch (e: Exception) {
                    lastError = e
                    attempt++
                    logVerbose("Error occurred: ${e.message}")
                    e.printStackTrace() // Keep this for debugging purposes
                    if (attempt < config.maxRetries) {
                        val delayTime = config.initialRetryDelay * (1 shl (attempt - 1))
                        logVerbose("Retrying in $delayTime ms")
                        delay(delayTime)
                    }
                }
            }

            logVerbose("All attempts failed")
            Result.Failure(lastError ?: Exception("Unknown error"))
        }
    }
}

sealed class Result<out T> {
    data class Success<out T>(val value: T) : Result<T>()
    data class Failure(val error: Exception) : Result<Nothing>()
}
