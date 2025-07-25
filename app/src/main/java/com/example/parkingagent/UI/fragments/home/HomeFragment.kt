package com.example.parkingagent.UI.fragments.home
//QR IN Page
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
import android.widget.AdapterView
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
import com.sunmi.printerx.style.TextStyle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.parkingagent.data.local.SharedPreferenceManager
import android.content.SharedPreferences
import org.json.JSONObject
import javax.inject.Inject
import android.text.Editable
import android.text.TextWatcher
import com.example.parkingagent.UI.fragments.qrInOut.QrOutFragment.Companion.ACTION_DATA_CODE_RECEIVED
import com.example.parkingagent.UI.fragments.qrInOut.QrOutFragment.Companion.DATA
import com.example.parkingagent.UI.fragments.qrInOut.QrOutFragment.Companion.TAG
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONArray

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

//    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceListView: ListView
    private val devices = mutableListOf<BluetoothDevice>()
    private lateinit var deviceAdapter: ArrayAdapter<String>
    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager
//    public var socket: BluetoothSocket? = null

    private val viewModel: HomeViewModel by viewModels()
    private var selectedVehicleTypeId: String? = null
    private val vehicleTypeList = mutableListOf<String>()
    private val vehicleTypeIdList = mutableListOf<String>()

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_home
    }

    override fun initView() {
        super.initView()
        Log.d("Page name", "QR IN")
//        setupBluetoothAdapter()
//        setupDeviceListView()
        setupVehicleTypeDropdown()
        setupButtonListeners()
        observeViewModel()
        registerReceiver()
        getDropdown()
    }

    private fun registerReceiver() {
        requireActivity().registerReceiver(
            br,
            IntentFilter(ACTION_DATA_CODE_RECEIVED),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    private val br = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Set the processing flag to true
            //isProcessing = true
            // Disable both vehicle input and search button
            binding.edtVehicleNo.isEnabled = false
            binding.vehicleTypeDropdown.isEnabled = false
            binding.btnGetVehicle.isEnabled = false
            binding.btnPark.isEnabled =false
            binding.edtVehicleNo.setText("")
            (requireActivity() as MainActivity).binding.loading.visibility=View.VISIBLE
            when (intent?.action) {
                ACTION_DATA_CODE_RECEIVED -> {
                    val code = intent.getStringExtra(DATA)
                    if (code != null) {
                        Log.d(TAG, "Scanned QR Code Data: $code")
                        binding.root.postDelayed({
                            if (isAdded) {
                                binding.edtVehicleNo.isEnabled = true
                                binding.vehicleTypeDropdown.isEnabled = true
                                binding.btnGetVehicle.isEnabled = true
                                binding.btnPark.isEnabled = true
                            }
                            (requireActivity() as MainActivity).binding.loading.visibility=View.GONE
                        }, 5000) // delay in milliseconds (adjust as needed)
                    } else {
                        showToast("QR Code scan failed.")
                        // Re-enable UI elements if scan fails
                        binding.edtVehicleNo.isEnabled = true
                        binding.vehicleTypeDropdown.isEnabled = true
                        binding.btnGetVehicle.isEnabled = true
                        binding.btnPark.isEnabled = true
                    }
                }
                else -> {
                    // Handle other broadcast actions if needed
                    binding.edtVehicleNo.isEnabled = true
                    binding.vehicleTypeDropdown.isEnabled = true
                    binding.btnGetVehicle.isEnabled = true
                    binding.btnPark.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(br)
    }


//    fun getDropdown (){
//        (requireActivity() as MainActivity).binding.loading.visibility = View.VISIBLE
//        val client = OkHttpClient()
//
//        val json = JSONObject().apply {
//            put("UserId", sharedPreferenceManager.getUserId().toString())
//            put("Token", sharedPreferenceManager.getAccessToken().toString())
//        }
//
//        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
//        Log.d("dropdown param", sharedPreferenceManager.getUserId().toString())
//        Log.d("dropdown param",  sharedPreferenceManager.getAccessToken().toString())
//
//        val request = Request.Builder()
//            .url("${sharedPreferenceManager.getBaseUrl()}Device/GetVehicleType")
//            //.addHeader("Authorization", "Bearer ${sharedPreferenceManager.getAccessToken()}")
//            .addHeader("Content-Type", "application/json")
//            .post(requestBody)
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.e("API_ERROR", "Failed to call API", e)
//                (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
//                requireActivity().runOnUiThread {
//                    Toast.makeText(requireContext(), "API call failed", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                val responseBody = response.body?.string()
//
//                Log.d("Raw API Response", responseBody ?: "Null response");
//
//
//                if (response.isSuccessful && responseBody != null) {
//                    try {
//
//                        requireActivity().runOnUiThread {
//                            (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
//                            Toast.makeText(requireContext(), "dropdown fetch Successful", Toast.LENGTH_SHORT).show()
//                            val vehicleTypes = responseBody
//
//                            (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
//                        }
//
//
//                    } catch (e: JSONException) {
//                        Log.e("JSON_PARSE_ERROR", "Error parsing response JSON", e)
//                        (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
//                    }
//                } else {
//                    requireActivity().runOnUiThread {
//                        (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
//                        Toast.makeText(requireContext(), "Report fetch failed!", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        })
//    }


//    private fun setupVehicleTypeDropdown() {
//        (requireActivity() as MainActivity).binding.loading.visibility=View.VISIBLE
//        val vehicleTypes = listOf("Two Wheeler", "Four Wheeler")
//        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, vehicleTypes)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        binding.vehicleTypeDropdown.adapter = adapter
//
//        binding.vehicleTypeDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                // Map the position to the appropriate vehicle type ID
//                selectedVehicleTypeId = if (position == 0) "2" else "1"
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//                // Optionally, set a default value or leave it empty
//            }
//        }
//    }








    fun getDropdown() {
        (requireActivity() as MainActivity).binding.loading.visibility = View.VISIBLE
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("UserId", sharedPreferenceManager.getUserId().toString())
            put("Token", sharedPreferenceManager.getAccessToken().toString())
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${sharedPreferenceManager.getBaseUrl()}Device/GetVehicleType")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Failed to call API", e)
                requireActivity().runOnUiThread {
                    (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
                    Toast.makeText(requireContext(), "API call failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("Raw API Response", responseBody ?: "Null response")

                if (response.isSuccessful && responseBody != null) {
                    try {
                        val rootObject = JSONObject(responseBody)
                        val jsonArray = rootObject.getJSONArray("Data")

                        vehicleTypeList.clear()
                        vehicleTypeIdList.clear()

                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            if (obj.getBoolean("Status")) {
                                val vehicleType = obj.getString("VehicleType")
                                val vehicleTypeId = obj.getInt("VehicleTypeId").toString()

                                vehicleTypeList.add(vehicleType)
                                vehicleTypeIdList.add(vehicleTypeId)
                            }
                        }

                        requireActivity().runOnUiThread {
                            (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
                            setupVehicleTypeDropdown()
                        }

                    } catch (e: JSONException) {
                        Log.e("JSON_PARSE_ERROR", "Error parsing response JSON", e)
                        Log.d("Invalid JSON", responseBody ?: "null")
                        requireActivity().runOnUiThread {
                            (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
                            Toast.makeText(requireContext(), "Parsing failed!", Toast.LENGTH_SHORT).show()
                        }
                    }

                } else {
                    requireActivity().runOnUiThread {
                        (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
                        Toast.makeText(requireContext(), "Vehicle type fetch failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun setupVehicleTypeDropdown() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, vehicleTypeList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.vehicleTypeDropdown.adapter = adapter

        binding.vehicleTypeDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedVehicleTypeId = vehicleTypeIdList.getOrNull(position)
                Log.d("SelectedVehicleTypeId", selectedVehicleTypeId ?: "null")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedVehicleTypeId = null
            }
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

            Log.d("vehicleNumber", vehicleNumber.toString())
            Log.d("selectedVehicleTypeId", selectedVehicleTypeId.toString())
            (requireActivity() as MainActivity).binding.loading.visibility=View.VISIBLE
            viewModel.parkedVehicle(vehicleNumber, selectedVehicleTypeId!!, Utils.getDeviceId(requireContext()),"")
        }

        binding.btnGetVehicle.setOnClickListener {
            (requireActivity() as MainActivity).binding.loading.visibility=View.VISIBLE
            viewModel.getLatestVehicleData()
        }

    }

    /**
     * Observe ViewModel events.
     */

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d("stage", "1")
                viewModel.mutualSharedflow.collectLatest { event ->
                    (requireActivity() as MainActivity).binding.loading.visibility=View.GONE
                    Log.d("stage", "2")
                    when (event) {

                        is HomeViewModel.ParkingVehicleEvents.VehicleParkingSuccessful -> {
                            showToast("Vehicle parked successfully!")
                            //event.vehicleParkingResponse.BookingNumber.toString()

                            printReceipt(event.vehicleParkingResponse.vehicleNo?:"Unknown",event.vehicleParkingResponse.vehicleTypeId.toString(), event.vehicleParkingResponse.inTime.toString(),event.vehicleParkingResponse.location.toString(),event.vehicleParkingResponse.BookingNumber.toString(),event.vehicleParkingResponse.VehicleType.toString())

                            //(requireActivity() as MainActivity).btManager.sendData("1".toByteArray())
                             (requireActivity() as MainActivity).btManager.controlRelayWithAutoOff(1)

                        }
                        is HomeViewModel.ParkingVehicleEvents.VehicleParkingFailed -> {
                            showToast("Failed to park vehicle: ${event.message}")
                        }

                        is HomeViewModel.ParkingVehicleEvents.ANPRVehicleSuccessful -> {
                           // Log.d("ANPR",event.anprVehicleResponse.vehicleNo.toString())
                            binding.edtVehicleNo.setText(event.anprVehicleResponse.vehicleNo)
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
//    private val receiver = object : BroadcastReceiver() {
//        @SuppressLint("MissingPermission")
//        override fun onReceive(context: Context, intent: Intent) {
//            when (intent.action) {
//                BluetoothDevice.ACTION_FOUND -> {
//                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
//                    device?.let {
//                        devices.add(it)
//                        deviceAdapter.add("${it.name} (${it.address})")
//                    }
//                }
//            }
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    override fun onDestroy() {
//        super.onDestroy()
////        bluetoothAdapter.cancelDiscovery()
//        requireContext().unregisterReceiver(receiver)
//    }
//
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        // Unregister the receiver if it was registered
//            requireContext().unregisterReceiver(receiver)
//
//    }
//,BookingNumber:String
    private fun printReceipt(vehicleNumber: String, vehicleTypeId: String, entryDateTime: String,location: String,BookingNumber:String,vehicleType:String) {
        // Create JSON object
        val jsonObject = JsonObject().apply {
            addProperty("vehicleNumber", vehicleNumber)
            addProperty("VehicleTypeId", vehicleTypeId)
            addProperty("vehicleType", vehicleType)
            addProperty("bookingNumber", BookingNumber)
            addProperty(
                "entryDateTime",
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )
        }

        Log.d("jsonObject",jsonObject.toString())

        PrinterSdk.getInstance().getPrinter(this.context, object : PrinterSdk.PrinterListen {
            override fun onDefPrinter(p0: PrinterSdk.Printer?) {

                val canvasWidth = 384
                var currentY = 10
                val lineHeight = 38

                p0?.canvasApi()?.initCanvas(
                    BaseStyle.getStyle()
                        .setWidth(canvasWidth)
                        .setHeight(750));

                fun centerX(text: String, textSize: Int): Int {
                    val charWidth = (textSize * 0.6).toInt() // Better than 0.5 for most fonts
                    val textWidth = charWidth * text.length
                    return ((canvasWidth - textWidth) / 2).coerceAtLeast(0)
                }

                fun renderCenteredText(text: String, size: Int = 25, bold: Boolean = false) {
                    val posX = centerX(text, size)
                    p0?.canvasApi()?.renderText(
                        text,
                        TextStyle.getStyle()
                            .setTextSize(size)
                            .enableBold(bold)
                            .setPosX(posX)
                            .setPosY(currentY)
                    )
                    currentY += lineHeight
                }


                fun renderLine(text: String) {
                    p0?.canvasApi()?.renderText(
                        text,
                        TextStyle.getStyle()
                            .setTextSize(22)  // Increase font size
                            .enableBold(true)    // Make text bold for better visibility
                            .setPosX(0)
                            .setPosY(currentY)
                    )
                    currentY += lineHeight
                }



                val slip = sharedPreferenceManager.getSlipHeaderFooter();
                val header1 = JSONObject(slip ?: "{}").optString("Header1")
                val header2 = JSONObject(slip ?: "{}").optString("Header2")
                val footer1 = JSONObject(slip ?: "{}").optString("Footer1")
                val footer2 = JSONObject(slip ?: "{}").optString("Footer2")


                if(header1.isNotBlank()) {
                    val lines = header1.split("|")
                    for (line in lines) {
                        renderCenteredText(line.trim(), size = 28, bold = true)
                    }
                    currentY += 1 // Spacing
                }

                if(header2 != "" ) {
                    renderCenteredText(header2.uppercase(), size = 24, bold = false)
                    renderLine("---------------------------------")
                }
                currentY += 10 // Spacing

                // Body
                renderLine("Vehicle No  : $vehicleNumber")
                renderLine("Vehicle Type: $vehicleType")
                renderLine("CheckIn Time: $entryDateTime")
                renderLine("Booking No  : $BookingNumber")

                currentY += 10 // Spacing

                p0?.canvasApi()?.renderQrCode(jsonObject.toString(),
                    QrStyle.getStyle()
                        .setPosX(55)
                        .setPosY(currentY).
                        setWidth(250)
                        .setHeight(250));

                currentY += 270 // Spacing



                if(footer1 != "" ) {
                    renderCenteredText("$footer1", size = 21, bold = true)
                }

                if(footer2 != "" ) {
                    renderCenteredText("$footer2", size = 21, bold = false)
                }

                p0?.canvasApi()?.printCanvas(1,null)
            }

            override fun onPrinters(p0: MutableList<PrinterSdk.Printer>?) {
                Toast.makeText(requireContext(),"Print successful",Toast.LENGTH_LONG).show();
            }

        })

        clearFields()


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

    private fun clearFields() {
        binding.edtVehicleNo.text?.clear()
        setupVehicleTypeDropdown()
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
