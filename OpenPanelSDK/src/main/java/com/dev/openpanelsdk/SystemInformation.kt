package com.dev.openpanelsdk

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager

class SystemInformation private constructor(private val context: Context) {

    val appVersionName: String?
    val appVersionCode: Long?
    val appName: String
    val hasNFC: Boolean?
    val hasTelephony: Boolean?
    val displayMetrics: DisplayMetrics = DisplayMetrics()

    init {
        val packageManager = context.packageManager

        var foundAppVersionName: String? = null
        var foundAppVersionCode: Long? = null

        try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(context.packageName, 0)
            foundAppVersionName = packageInfo.versionName
            foundAppVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            }else{
                packageInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(LOGTAG, "System information constructed with a context that apparently doesn't exist.")
        }

        val applicationInfo: ApplicationInfo = context.applicationInfo
        val appNameStringId = applicationInfo.labelRes

        appVersionName = foundAppVersionName
        appVersionCode = foundAppVersionCode
        appName = if (appNameStringId == 0) {
            applicationInfo.nonLocalizedLabel?.toString() ?: "Misc"
        } else {
            context.getString(appNameStringId)
        }

        // We can't count on these features being available on all devices.
        var foundNFC: Boolean? = null
        var foundTelephony: Boolean? = null

        try {
            val hasSystemFeatureMethod = packageManager::class.java.getMethod("hasSystemFeature", String::class.java)
            foundNFC = hasSystemFeatureMethod.invoke(packageManager, "android.hardware.nfc") as Boolean
            foundTelephony = hasSystemFeatureMethod.invoke(packageManager, "android.hardware.telephony") as Boolean
        } catch (e: Exception) {
            Log.w(LOGTAG, "System version supports PackageManager.hasSystemFeature, but we were unable to call it.")
        }

        hasNFC = foundNFC
        hasTelephony = foundTelephony

        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        display.getMetrics(displayMetrics)
    }

    fun getCurrentNetworkOperator(): String? {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        return telephonyManager?.networkOperatorName
    }

    @SuppressLint("MissingPermission")
    fun isWifiConnected(): Boolean? {
        if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
            val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connManager.activeNetworkInfo
            return networkInfo?.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
        }
        return null
    }

    @SuppressLint("MissingPermission")
    fun isBluetoothEnabled(): Boolean? {
        return try {
            if (context.packageManager.checkPermission(Manifest.permission.BLUETOOTH, context.packageName) == PackageManager.PERMISSION_GRANTED) {
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                bluetoothAdapter?.isEnabled
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getBluetoothVersion(): String {
        return when {
            context.packageManager.hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE) -> "ble"
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) -> "classic"
            else -> "none"
        }
    }

    companion object {
        private var sInstance: SystemInformation? = null
        private val sInstanceLock = Any()
        private const val LOGTAG = "OpenPanel.SysInfo"

        fun getInstance(context: Context): SystemInformation {
            synchronized(sInstanceLock) {
                if (sInstance == null) {
                    sInstance = SystemInformation(context.applicationContext)
                }
                return sInstance!!
            }
        }
    }
}