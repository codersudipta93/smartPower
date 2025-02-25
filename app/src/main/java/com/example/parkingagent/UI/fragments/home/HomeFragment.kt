package com.example.parkingagent.UI.fragments.home

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.parkingagent.MainActivity
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.databinding.FragmentHomeBinding
import com.example.parkingagent.utils.Utils
import com.google.gson.JsonObject
import com.sunmi.printerx.PrinterSdk
import com.sunmi.printerx.style.BaseStyle
import com.sunmi.printerx.style.QrStyle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

//    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceListView: ListView
    private val devices = mutableListOf<BluetoothDevice>()
    private lateinit var deviceAdapter: ArrayAdapter<String>

//    public var socket: BluetoothSocket? = null

    private val viewModel: HomeViewModel by viewModels()
    private var selectedVehicleTypeId: String? = null

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_home
    }

    override fun initView() {
        super.initView()

//        setupBluetoothAdapter()
//        setupDeviceListView()
        setupVehicleTypeDropdown()
        setupButtonListeners()
        observeViewModel()
    }

    /**
     * Initialize Bluetooth adapter using BluetoothManager.
     */
//    private fun setupBluetoothAdapter() {
//        val bluetoothManager =
//            requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//        bluetoothAdapter = bluetoothManager.adapter ?: run {
//            Toast.makeText(requireContext(), "Bluetooth not available", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Request Bluetooth permissions if not granted
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(
//                    Manifest.permission.BLUETOOTH_CONNECT,
//                    Manifest.permission.BLUETOOTH_SCAN
//                ),
//                100
//            )
//        }
//
//        // Register BroadcastReceiver for device discovery
//        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
//        requireContext().registerReceiver(receiver, filter)
//        bluetoothAdapter.startDiscovery()
//    }

    /**
     * Initialize the ListView for displaying discovered devices.
     */
//    private fun setupDeviceListView() {
//        deviceListView = binding.deviceListView
//        deviceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1)
//        deviceListView.adapter = deviceAdapter
//
//        deviceListView.setOnItemClickListener { _, _, position, _ ->
//            val device = devices[position]
//            connectToDevice(device)
//        }
//    }

    /**
     * Set up the vehicle type dropdown.
     */
    private fun setupVehicleTypeDropdown() {
        val vehicleTypes = listOf("Two-Wheeler", "Four-Wheeler")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, vehicleTypes)
        binding.vehicleTypeDropdown.setAdapter(adapter)

        binding.vehicleTypeDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedVehicleTypeId = if (position == 0) "2" else "1"
        }
    }

    /**
     * Set up button listeners for parking the vehicle.
     */
    private fun setupButtonListeners() {
        binding.btnPark.setOnClickListener {
            val vehicleNumber = binding.edtVehicleNo.text.toString().trim()
            if (vehicleNumber.isEmpty()) {
                showToast("Please enter the vehicle number")
                return@setOnClickListener
            }
            if (selectedVehicleTypeId.isNullOrEmpty()) {
                showToast("Please select the vehicle type")
                return@setOnClickListener
            }

            viewModel.parkedVehicle(vehicleNumber, selectedVehicleTypeId!!, Utils.getDeviceId(requireContext()))
        }
    }

    /**
     * Observe ViewModel events.
     */
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mutualSharedflow.collectLatest { event ->
                    when (event) {
                        is HomeViewModel.ParkingVehicleEvents.VehicleParkingSuccessful -> {
                            showToast("Vehicle parked successfully!")

                            printReceipt(event.vehicleParkingResponse.vehicleNo?:"Unknown",event.vehicleParkingResponse.vehicleTypeId.toString(),
                                event.vehicleParkingResponse.inTime.toString()
                            )

                            (requireActivity() as MainActivity).btManager.sendData("1".toByteArray())

                        }
                        is HomeViewModel.ParkingVehicleEvents.VehicleParkingFailed -> {
                            showToast("Failed to park vehicle: ${event.message}")
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle device connection.
     */

//    @SuppressLint("MissingPermission")
//    private fun connectToDevice(device: BluetoothDevice) {
//        bluetoothAdapter.cancelDiscovery()
//        val uuid = device.uuids?.firstOrNull()?.uuid
//        if (uuid == null) {
//            showToast("No UUID available for device")
//            return
//        }
//
//        try {
//             if (socket==null || socket?.isConnected==false){
//                 socket = device.createRfcommSocketToServiceRecord(uuid)
//                 socket?.connect()
////                 socket.close()
//             }
//            showToast("Connected to ${device.name}")
//            sendIntegerToBluetoothDevice(socket!!,1)
//
//        } catch (e: IOException) {
//            Log.e("Bluetooth", "Connection failed", e)
//            showToast("Connection failed")
//        }
//    }

    fun sendIntegerToBluetoothDevice(socket: BluetoothSocket, value: Int) {
        try {
            val outputStream = socket.outputStream
//            val byteArray = byteArrayOf(value.toString().toByte())
            val ss=value.toString()
            outputStream.write(ss.toByteArray()) // Send the integer as a single byte
            outputStream.flush()
            showToast("Sent integer: $value")
        } catch (e: IOException) {
            Log.e("Bluetooth", "Failed to send data", e)
            showToast("Failed to send integer")
        }
    }

    /**
     * BroadcastReceiver to handle discovered Bluetooth devices.
     */
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        devices.add(it)
                        deviceAdapter.add("${it.name} (${it.address})")
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
//        bluetoothAdapter.cancelDiscovery()
        requireContext().unregisterReceiver(receiver)
    }


    private fun printReceipt(vehicleNumber: String, vehicleType: String, entryDateTime: String) {
        // Create JSON object
        val jsonObject = JsonObject().apply {
            addProperty("vehicleNumber", vehicleNumber)
            addProperty("vehicleType", vehicleType)
            addProperty(
                "entryDateTime",
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )
        }

        Log.d("jsonObject",jsonObject.toString())

        PrinterSdk.getInstance().getPrinter(this.context, object : PrinterSdk.PrinterListen {
            override fun onDefPrinter(p0: PrinterSdk.Printer?) {

                p0?.canvasApi()?.initCanvas(
                    BaseStyle.getStyle()
                        .setWidth(330)
                        .setHeight(380));

                p0?.canvasApi()?.renderQrCode(jsonObject.toString(),
                    QrStyle.getStyle()
                        .setPosX(5)
                        .setPosY(10).
                        setWidth(300)
                        .setHeight(300));

                p0?.canvasApi()?.printCanvas(1,null)
            }

            override fun onPrinters(p0: MutableList<PrinterSdk.Printer>?) {
                Toast.makeText(requireContext(),"Print successful",Toast.LENGTH_LONG).show();
            }

        })

        // Generate QR Code
//        val qrBitmap = generateQrCode(jsonObject.toString())
//
//        // Print using Sunmi Printer
//        PrinterSdk.getInstance().getPrinter(requireContext(), object : PrinterSdk.PrinterListen {
//            override fun onDefPrinter(printer: PrinterSdk.Printer?) {
//                printer?.lineApi()?.apply {
//                    // Print header
//                    initLine(BaseStyle.getStyle().setAlign(Align.CENTER))
//                    printText("Parking Receipt\n", null)
//                    printText("---------------\n", null)
//
//                    // Print vehicle details
//                    initLine(BaseStyle.getStyle().setAlign(Align.LEFT))
//                    printText("Vehicle Number: $vehicleNumber\n", null)
//                    printText("Vehicle Type: $vehicleType\n", null)
//                    printText(
//                        "Entry Time: ${
//                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
//                                Date()
//                            )
//                        }\n", null
//                    )
//
//                    // Print QR code
//                    if (qrBitmap != null) {
//                        initLine(BaseStyle.getStyle().setAlign(Align.CENTER))
//                        printQrCode(qrBitmap, QrStyle.getStyle())
//                    }
//
//                    // Footer
//                    printText("\nThank you for visiting!\n", null)
//                    printText("----------------------\n", null)
//                }
//            }
//
//            override fun onPrinters(printers: MutableList<PrinterSdk.Printer>?) {
//                // Log printers if needed
//            }
//        })
    }

//    private fun generateQrCode(data: String): Bitmap? {
//        return try {
//            val barcodeEncoder = BarcodeEncoder()
//            barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 300, 300)
//        } catch (e: Exception) {
//            showToast("Error generating QR Code")
//            null
//        }
//    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
