package com.example.parkingagent.UI.fragments.qrInOut

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.parkingagent.MainActivity
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.data.remote.models.CollectionInsert.CollectionInsertData
import com.example.parkingagent.data.remote.models.VehicleParking.VehicleParkingResponse
//import com.example.parkingagent.UI.fragments.more.MoreViewModel
import com.example.parkingagent.databinding.FragmentMoreBinding
import com.example.parkingagent.databinding.FragmentQrOutBinding
import com.example.parkingagent.utils.Utils
import com.example.parkingagent.utils.scanner.IScanInterface
import com.google.gson.JsonObject
import com.sunmi.printerx.PrinterSdk
import com.sunmi.printerx.style.BaseStyle
import com.sunmi.printerx.style.QrStyle
import com.sunmi.printerx.style.TextStyle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import android.view.MotionEvent
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import java.io.IOException
import android.view.inputmethod.InputMethodManager
import com.example.parkingagent.UI.fragments.qrInOut.QrInOutViewModel.ParkingVehicleEvents
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import androidx.lifecycle.viewModelScope
@AndroidEntryPoint
class QrOutFragment : BaseFragment<FragmentQrOutBinding>() {

    companion object {
        const val TAG = "Scan-Test"
        const val ACTION_DATA_CODE_RECEIVED = "com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED"
        const val DATA = "data"
    }
    private var latestVehicleParkingResponse: VehicleParkingResponse? = null

    private var isReceiptPrinted = false
    private val _mutualSharedflow= MutableSharedFlow<ParkingVehicleEvents>()
    val mutualSharedflow: SharedFlow<ParkingVehicleEvents> = _mutualSharedflow

    private val viewModel: QrInOutViewModel by viewModels()
    private var scanInterface: IScanInterface? = null
    // Flag to track if processing is in progress
    private var isProcessing = false

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager


    private val br = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Set the processing flag to true
            isProcessing = true
            // Disable both vehicle input and search button
            binding.edtVehicleNo.isEnabled = false
            binding.btnSearch.isEnabled = false

            binding.edtVehicleNo.setText("")
            when (intent?.action) {
                ACTION_DATA_CODE_RECEIVED -> {
                    val code = intent.getStringExtra(DATA)
                    if (code != null) {
                        Log.d(TAG, "Scanned QR Code Data: $code")
                        setDataToLayout(code)
                    } else {
                        showToast("QR Code scan failed.")
                        // Re-enable UI elements if scan fails
                        enableInputElements()
                    }
                }
                else -> {
                    // Handle other broadcast actions if needed
                    enableInputElements()
                }
            }
        }
    }

    private val conn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            scanInterface = IScanInterface.Stub.asInterface(service)
            Log.i(TAG, "Scanner Service Connected!")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.e(TAG, "Scanner Service Disconnected!")
            scanInterface = null
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_qr_out
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun initView() {
        super.initView()
        bindScannerService()
        registerReceiver()
        setupButton()
        observeViewModel()
        Log.d("QR out device id", Utils.getDeviceId(requireActivity()))

        binding.btnSearch.setOnClickListener {
            Log.d("Hey", "Search button clicked")
            // Hide keyboard if needed
            val editText = view?.findViewById<TextInputEditText>(R.id.edt_vehicleNo)
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText?.windowToken, 0)

            searchVehicle()
        }
    }

    // Helper method to enable input elements
    private fun enableInputElements() {
        binding.edtVehicleNo.isEnabled = true
        binding.btnSearch.isEnabled = true
        isProcessing = false
    }

    fun searchVehicle() {
        // Prevent search if already processing
        if (isProcessing) {
            showToast("Processing in progress, please wait...")
            return
        }

        isProcessing = true
        binding.edtVehicleNo.isEnabled = false
        binding.btnSearch.isEnabled = false

        (requireActivity() as MainActivity).binding.loading.visibility = View.VISIBLE
        //(requireActivity() as MainActivity).btManager.sendData("1".toByteArray())
        viewModel.searchAndParkVehicle(
            binding.edtVehicleNo.text.toString(),
            Utils.getDeviceId(requireActivity()),
            sharedPreferenceManager.getUserId().toString()
        )
    }

    private fun bindScannerService() {
        if (scanInterface == null) {
            val intent = Intent()
            intent.setPackage("com.example.parkingagent.utils.scanner")
            intent.action = "com.example.parkingagent.utils.scanner.IScanInterface"
            requireActivity().startService(intent)
            requireActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE)
        }
    }

    private fun registerReceiver() {
        requireActivity().registerReceiver(
            br,
            IntentFilter(ACTION_DATA_CODE_RECEIVED),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    private fun setDataToLayout(qrData: String) {
        try {
            val jsonData = JSONObject(qrData)
            val vehicleNumber = jsonData.optString("vehicleNumber", "")
            Log.d("Vehicle Number", vehicleNumber)
            val entryDateTime = jsonData.optString("entryDateTime", "")
            val bookingNumber = jsonData.optString("bookingNumber", "")
            val VehicleTypeId = jsonData.optString("VehicleTypeId", "")
            if (vehicleNumber.isNotEmpty() && entryDateTime.isNotEmpty()) {
                isReceiptPrinted = false
                (requireActivity() as MainActivity).binding.loading.visibility = View.VISIBLE
                viewModel.parkedVehicle(
                    vehicleNumber,
                    Utils.getDeviceId(requireActivity()),
                    bookingNumber,
                    VehicleTypeId
                )
            } else {
                showToast("Invalid QR Code data.")
                enableInputElements()
            }
        } catch (e: Exception) {
            showToast("Failed to parse QR Code data: ${e.message}")
            enableInputElements()
        }
    }

    private fun setupButton() {
        binding.btnCollect.setOnClickListener {
            openBoom("1")
        }

        binding.btnNotCollect.setOnClickListener {
            openBoom("0")
        }


    }

    private fun openBoom(IsCollected: String) {
        if (binding.edtVehicleNo.text.toString().trim().isEmpty() || binding.edtEntryDateTime.text.toString().trim().isEmpty()) {
            showToast("Please ensure all fields are filled before exiting.")
            return
        }
        Log.d("In Time", binding.edtOutTime.text.toString())

        Log.d("Parking API data res2", latestVehicleParkingResponse?.vehicleTypeId.toString());

       // (requireActivity() as MainActivity).btManager.sendData("1".toByteArray())

        viewModel.collectionInsert(
            binding.edtVehicleNo.text.toString(),
            binding.edtChargableAmount.text.toString().toDouble(),
            IsCollected,
            latestVehicleParkingResponse?.deviceId.toString(),
            latestVehicleParkingResponse?.vehicleTypeId.toString(),
            latestVehicleParkingResponse?.inTime.toString(),
            latestVehicleParkingResponse?.outTime.toString()
        )
    }

    private fun printReceipt(response: VehicleParkingResponse) {
        PrinterSdk.getInstance().getPrinter(this.context, object : PrinterSdk.PrinterListen {
            override fun onDefPrinter(p0: PrinterSdk.Printer?) {
                // Increase canvas height as needed.

                val canvasWidth = 384
                var currentY = 10
                val lineHeight = 38

                p0?.canvasApi()?.initCanvas(
                    BaseStyle.getStyle()
                        .setWidth(410)
                        .setHeight(700)
                )

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

                fun renderAmount(text: String) {
                    p0?.canvasApi()?.renderText(
                        text,
                        TextStyle.getStyle()
                            .setTextSize(25)  // Increase font size
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

                if (header1 != "") {
                    renderCenteredText("$header1", size = 28, bold = true)
                    currentY += 1 // Spacing
                }

                if (header2 != "") {
                    renderCenteredText(header2.uppercase(), size = 24, bold = false)
                    renderLine("---------------------------------")
                }
                currentY += 10 // Spacing

                renderLine("Vehicle No   : ${response.vehicleNo}")
                renderLine("Vehicle Type : ${response.VehicleType}")
                renderLine("Booking No   : ${response.BookingNumber}")
                renderLine("In Time      : ${response.inTime}");
                renderLine("Out Time     : ${response.outTime ?: "N/A"}")
                renderLine("Duration     : ${response.duration ?: "N/A"}")
                renderLine("---------------------------------")
                renderAmount("Pay CASH: ₹${response.chargableAmount}")
                renderLine("---------------------------------")

                currentY += 10

                if (footer1 != "") {
                    renderCenteredText("$footer1", size = 21, bold = true)
                }

                if (footer2 != "") {
                    renderCenteredText("$footer2", size = 21, bold = false)
                }
                p0?.canvasApi()?.printCanvas(1, null)
            }

            override fun onPrinters(printers: MutableList<PrinterSdk.Printer>?) {
                Toast.makeText(requireContext(), "Print successful", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mutualSharedflow.collectLatest {
                    (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
                    when (it) {
                        is QrInOutViewModel.ParkingVehicleEvents.VehicleParkingSuccessful -> {
                            Log.d("vehicleParkingResponse", it.vehicleParkingResponse.toString())
                            showToast(it.vehicleParkingResponse.msg.toString())

                            binding.edtVehicleNo.setText(it.vehicleParkingResponse.vehicleNo ?: "")
                            binding.edtOutTime.setText(it.vehicleParkingResponse.outTime ?: "N/A")
                            binding.edtEntryDateTime.setText(it.vehicleParkingResponse.inTime ?: "N/A")
                            binding.edtDuration.setText(it.vehicleParkingResponse.duration ?: "N/A")
                            binding.edtChargableAmount.setText(it.vehicleParkingResponse.chargableAmount?.toString() ?: "N/A")

                       //     (requireActivity() as MainActivity).btManager.sendData("1".toByteArray())

                            // Store the response for later use.
                            latestVehicleParkingResponse = it.vehicleParkingResponse

                            // Re-enable input elements
                            enableInputElements()
                        }

                        is QrInOutViewModel.ParkingVehicleEvents.VehicleParkingFailed -> {
                            showToast("Exit failed: ${it.message}")
                            Log.d(TAG, "Exit failed: ${it.message}")

                            // Re-enable input elements
                            enableInputElements()
                        }

                        is QrInOutViewModel.ParkingVehicleEvents.CollectionInsertSuccessful -> {
                            // Prevent multiple prints
                            if (!isReceiptPrinted) {
                                (requireActivity() as MainActivity).btManager.sendData("1".toByteArray())
                                clearFields()
                                showToast(it.collectionInsertData.msg.toString())

                                // Use the stored VehicleParkingResponse data to print the receipt.
                                latestVehicleParkingResponse?.let { response ->
                                    printReceipt(response)
                                    isReceiptPrinted = true // Set flag to true after printing
                                } ?: run {
                                    showToast("No parking response data available to print receipt")
                                }
                            }

                            // Re-enable input elements
                            enableInputElements()
                        }
                    }
                }
            }
        }
    }

    private fun clearFields() {
        binding.edtVehicleNo.text?.clear()
        binding.edtEntryDateTime.text?.clear()
        binding.edtOutTime.text?.clear()
        binding.edtDuration.text?.clear()
        binding.edtChargableAmount.text?.clear()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(br)
        requireActivity().unbindService(conn)
    }
}