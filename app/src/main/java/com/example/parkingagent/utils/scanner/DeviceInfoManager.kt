package com.example.parkingagent.utils.scanner

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

class DeviceInfoManager(private val fragment: Fragment) {

    private val REQUEST_CODE_PERMISSION = 101

    fun checkAndRequestPermissions() {
        if (ActivityCompat.checkSelfPermission(
                fragment.requireContext(), Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            fragment.requestPermissions(
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                REQUEST_CODE_PERMISSION
            )
        } else {
            fetchDeviceInfo()
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchDeviceInfo()
            } else {
                Toast.makeText(fragment.requireContext(), "Permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchDeviceInfo() {
        val deviceId = getAndroidId(fragment.requireContext())
        val imei = getDeviceImei(fragment.requireContext())

        (fragment as? DeviceInfoListener)?.onDeviceInfoFetched(deviceId, imei)
    }

    private fun getAndroidId(context: Context): String? {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun getDeviceImei(context: Context): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                telephonyManager.deviceId
            } else {
                null
            }
        }
        return null
    }

    interface DeviceInfoListener {
        fun onDeviceInfoFetched(deviceId: String?, imei: String?)
    }
}
