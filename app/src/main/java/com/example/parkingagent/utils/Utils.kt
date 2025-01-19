package com.example.parkingagent.utils
import android.os.Build
import android.provider.Settings
import android.content.Context

class Utils {
    companion object {
        fun getDeviceId(context: Context): String {
            return try {
                // Use ANDROID_ID for a unique device ID
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } catch (e: Exception) {
                e.printStackTrace()
                "unknown_device_id"
            }
        }
    }
}


