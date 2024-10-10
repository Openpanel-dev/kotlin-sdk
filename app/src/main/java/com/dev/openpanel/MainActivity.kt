package com.dev.openpanel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dev.openpanel.databinding.ActivityMainBinding
import com.dev.openpanelsdk.OpenPanel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var openPanel: OpenPanel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        openPanel = OpenPanel(this, OpenPanel.Options(
            clientId = "0f6ceb93-ba13-4106-858e-c44d48229899", // replace with your client id
            clientSecret = "sec_9679b09055dcb5b91597", // replace with your client secret
            automaticTracking = true,
            verbose = true
        ))
        openPanel.setGlobalProperties(
            mapOf(
                "environment" to if (BuildConfig.DEBUG) "development" else "production"
            )
        )
        openPanel.identify(
            "test_user", mapOf(
                "firstName" to "John",
                "lastName" to "Doe",
                "email" to "john@example.com"
            )
        )
        setContentView(binding.root)
        handleClick()
    }

    private fun handleClick() {
        binding.btnTrackEvent.setOnClickListener {
            openPanel.track("track_button_clicked", mapOf(
                "property 1" to "This will log this property in the property 1 field",
                "property 2" to "This will log this property in the property 2 field",
            ))
        }
        binding.btnClearUserData.setOnClickListener {
            openPanel.clear()
        }
    }
}