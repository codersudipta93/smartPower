
package com.example.parkingagent.utils
import java.util.UUID
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
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton
import android.os.Handler
import android.os.Looper
import com.example.parkingagent.data.local.SharedPreferenceManager

// BluetoothConnectionManager.kt
@Singleton
class BluetoothConnectionManager @Inject constructor(@ApplicationContext private val context: Context) {

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var socket: BluetoothSocket? = null

    // Set your target device MAC address here.
    private var targetMacAddress = ""

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
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
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
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
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

    //@SuppressLint("MissingPermission")
//    fun connectToDevice(device: BluetoothDevice) {
//        try {
//            if (socket == null || !socket!!.isConnected) {
//                // Use the first UUID available.
//                val uuid = device.uuids?.firstOrNull()?.uuid
//                if (uuid != null) {
//                    socket = device.createRfcommSocketToServiceRecord(uuid)
//                    socket?.connect()
//                    Log.d("BTConnectionManager", "Connected to ${device.name}")
//                    Toast.makeText(context,"Connected to ${device.name}",Toast.LENGTH_LONG).show()
//                } else {
//                    Toast.makeText(context,"No UUID available for device.",Toast.LENGTH_LONG).show()
//                    Log.e("BTConnectionManager", "No UUID available for device.")
//                }
//            }
//        } catch (e: IOException) {
//            Log.e("BTConnectionManager", "Connection failed: ${e.message}")
//        }
//    }


    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        try {
            if (socket == null || !socket!!.isConnected) {
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                socket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothAdapter?.cancelDiscovery()
                socket?.connect()
                Log.d("BTConnectionManager", "Connected to ${device.name}")
                Log.d("BTConnectionManager", "mac address ${device}")

                sharedPreferenceManager.saveLastPairedMacAddress(device.toString())
                Toast.makeText(context, "Connected to ${device.name}", Toast.LENGTH_LONG).show()

            }
        } catch (e: IOException) {
            Log.e("BTConnectionManager", "Connection failed: ${e.message}")
            Toast.makeText(context, "Connection failed: ${e.message}", Toast.LENGTH_LONG).show()
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

    fun getConnectedDeviceName(): String? {
        return socket!!.remoteDevice!!.name
    }



    @SuppressLint("MissingPermission")
    fun connectToPairedJDYDevice() {
        //targetMacAddress = sharedPreferenceManager.getLastPairedMacAddress().toString()
        // Log.d("targetMacAddress",targetMacAddress)
        val pairedDevices = bluetoothAdapter?.bondedDevices
        if (pairedDevices.isNullOrEmpty()) {
            Toast.makeText(context, "No paired devices found", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("BTManager", "Total paired devices: ${pairedDevices.size}")
        for (device in pairedDevices) {
            Log.d("BTManager", "Paired device: ${device.name} | ${device.address}")
        }

        val device = pairedDevices.find { it.address.equals(sharedPreferenceManager.getLastPairedMacAddress().toString(), ignoreCase = true) }
        Log.d("device",device.toString())
        if (device != null) {
            Log.d("BTManager", "No Paired device found. Trying to connect...")
            connectToDevice(device)
        } else {
          //  Toast.makeText(context, "No Paired device found in paired list", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendRelayCommand(turnOn: Boolean) {
        val command = if (turnOn) "ON\r\n" else "OFF\r\n"
        sendData(command.toByteArray(Charsets.UTF_8))

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("BTCommand", "Command sent: $command")
        }, 500)
    }

    fun controlRelayWithAutoOff(
        relayNumber: Int
    ) {
        // Turn ON relay
        controlRelay(relayNumber, true)

        // Schedule OFF after delay
        Handler(Looper.getMainLooper()).postDelayed({
            controlRelay(relayNumber, false)
        }, 500)
    }



    fun controlRelay(relayNumber: Int, isOn: Boolean) {
        val onOffByte = if (isOn) 0x01 else 0x00
        val checksum: Byte = when (relayNumber) {
            1 -> if (isOn) 0xA2.toByte() else 0xA1.toByte()
            2 -> if (isOn) 0xA4.toByte() else 0xA3.toByte()
            else -> return  // Unsupported relay
        }

        val command = byteArrayOf(0xA0.toByte(), relayNumber.toByte(), onOffByte.toByte(), checksum)
        sendData(command)

        Log.d(
            "BTConnectionManager",
            "Relay $relayNumber ${if (isOn) "ON" else "OFF"} command sent"
        )
    }


    fun turnOnRelay1() {
        val command = byteArrayOf(0xA0.toByte(), 0x01, 0x01, 0xA2.toByte())
        sendData(command)
        Log.d("BTConnectionManager", "Relay 1 ON command sent")
    }

    /**
     * Turn OFF Relay 1
     */
    fun turnOffRelay1() {
        val command = byteArrayOf(0xA0.toByte(), 0x01, 0x00, 0xA1.toByte())
        sendData(command)
        Log.d("BTConnectionManager", "Relay 1 OFF command sent")
    }



}


