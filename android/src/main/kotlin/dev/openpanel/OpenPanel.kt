package dev.openpanel.android

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.webkit.WebView
// Add these imports
import dev.openpanel.OpenPanel as BaseOpenPanel
import dev.openpanel.OpenPanel.Options

class OpenPanel(options: Options) : BaseOpenPanel(options) {

    // Add 'override' keyword
    override fun setupAutomaticTracking() {
        val application = getApplication()
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
    }

    private fun getApplication(): Application? {
        return try {
            Class.forName("android.app.ActivityThread")
                .getMethod("currentApplication")
                .invoke(null) as? Application
        } catch (e: Exception) {
            null
        }
    }

    override fun getUserAgent(): String {
        val application = getApplication()
        val userAgent = if (application != null) {
            val webView = WebView(application)
            webView.settings.userAgentString
        } else {
            null
        }

        return if (!userAgent.isNullOrEmpty()) {
            "$userAgent OpenPanelKotlin/${BaseOpenPanel.sdkVersion}"
        } else {
            val defaultUserAgent = "Mozilla/5.0 (Linux; Android ${android.os.Build.VERSION.RELEASE}; ${android.os.Build.MODEL}) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36"
            "$defaultUserAgent OpenPanelKotlin/${BaseOpenPanel.sdkVersion}"
        }
    }

    init {
        api.addHeader("user-agent", getUserAgent())

        if (options.automaticTracking == true) {
            setupAutomaticTracking()
        }
    }

    companion object {
        fun create(options: Options): OpenPanel {
            return OpenPanel(options)
        }
    }
}