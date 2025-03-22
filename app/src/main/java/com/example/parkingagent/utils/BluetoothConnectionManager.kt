package com.example.parkingagent.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// BluetoothConnectionManager.kt
@Singleton
class BluetoothConnectionManager @Inject constructor(@ApplicationContext private val context: Context) {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var socket: BluetoothSocket? = null

    // Set your target device MAC address here.
    private val targetMacAddress = "00:20:10:08:68:DF"

    // Listener interface for discovered devices.
    interface DeviceDiscoveredListener {
        fun onDeviceDiscovered(device: BluetoothDevice)
    }

    private var deviceDiscoveredListener: DeviceDiscoveredListener? = null

    fun setDeviceDiscoveredListener(listener: DeviceDiscoveredListener) {
        deviceDiscoveredListener = listener
    }

    // BroadcastReceiver to monitor discovered devices.
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {

                    deviceDiscoveredListener?.onDeviceDiscovered(it)

                    if (it.address.equals(targetMacAddress, ignoreCase = true)) {
                        // Found the target device – connect automatically.
                        connectToDevice(it)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun initialize() {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Log.e("BTConnectionManager", "Bluetooth is not supported on this device.")
            return
        }
        // Register the receiver for discovery.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(receiver, filter)
        bluetoothAdapter?.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        try {
            if (socket == null || !socket!!.isConnected) {
                // Use the first UUID available.
                val uuid = device.uuids?.firstOrNull()?.uuid
                if (uuid != null) {
                    socket = device.createRfcommSocketToServiceRecord(uuid)
                    socket?.connect()
                    Log.d("BTConnectionManager", "Connected to ${device.name}")
                    Toast.makeText(context,"Connected to ${device.name}",Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context,"No UUID available for device.",Toast.LENGTH_LONG).show()
                    Log.e("BTConnectionManager", "No UUID available for device.")
                }
            }
        } catch (e: IOException) {
            Log.e("BTConnectionManager", "Connection failed: ${e.message}")
        }
    }

    /**
     * Send data to the connected device.
     */
    fun sendData(data: ByteArray) {
        try {
            if (socket != null && socket!!.isConnected) {
                socket!!.outputStream.write(data)
                socket!!.outputStream.flush()
                Log.d("BTConnectionManager", "Data sent successfully")
            } else {
                Log.e("BTConnectionManager", "No connected socket found")
            }
        } catch (e: IOException) {
            Log.e("BTConnectionManager", "Error sending data: ${e.message}")
        }
    }

    /**
     * Clean up when done.
     */
    fun closeConnection() {
        try {
            socket?.close()
            socket = null
        } catch (e: IOException) {
            Log.e("BTConnectionManager", "Error closing socket: ${e.message}")
        }
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            // Receiver may have been already unregistered.
        }
    }

    fun isConnected(): Boolean {
        return socket != null && socket!!.isConnected
    }
}
