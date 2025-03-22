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
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.parkingagent.MainActivity
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
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

@AndroidEntryPoint
class QrOutFragment : BaseFragment<FragmentQrOutBinding>() {

    companion object {
        const val TAG = "Scan-Test"
        const val ACTION_DATA_CODE_RECEIVED = "com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED"
        const val DATA = "data"
    }
    private var latestVehicleParkingResponse: VehicleParkingResponse? = null


    private val viewModel: QrInOutViewModel by viewModels()
    private var scanInterface: IScanInterface? = null

    private val br = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_DATA_CODE_RECEIVED -> {
                    val code = intent.getStringExtra(DATA)
                    if (code != null) {
                        Log.d(TAG, "Scanned QR Code Data: $code")
                        setDataToLayout(code)
                    } else {
                        showToast("QR Code scan failed.")
                    }
                }
                else -> {
                    // Handle other broadcast actions if needed
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

    override fun initView() {
        super.initView()
        bindScannerService()
        registerReceiver()
        setupButton()
        observeViewModel()
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
            val entryDateTime = jsonData.optString("entryDateTime", "")

            if (vehicleNumber.isNotEmpty() && entryDateTime.isNotEmpty()) {
                binding.edtVehicleNo.setText(vehicleNumber)
                binding.edtEntryDateTime.setText(entryDateTime)
                (requireActivity() as MainActivity).binding.loading.visibility = View.VISIBLE
                viewModel.parkedVehicle(vehicleNumber,Utils.getDeviceId(requireActivity()))
            } else {
                showToast("Invalid QR Code data.")
            }
        } catch (e: Exception) {
            showToast("Failed to parse QR Code data: ${e.message}")
        }
    }

    private fun setupButton() {
//        binding.btnExit.setOnClickListener {
//            val vehicleNumber = binding.edtVehicleNo.text.toString().trim()
//            val entryDateTime = binding.edtEntryDateTime.text.toString().trim()
//
//            if (vehicleNumber.isEmpty() || entryDateTime.isEmpty()) {
//                showToast("Please ensure all fields are filled before exiting.")
//                return@setOnClickListener
//            }
//
//            val deviceId = Utils.getDeviceId(requireContext()) // Replace with actual device ID fetching logic
//            viewModel.parkedVehicle(vehicleNumber, deviceId)
//        }

        binding.btnCollect.setOnClickListener {
            openBoom()
        }

        binding.btnNotCollect.setOnClickListener {
            openBoom()
        }

    }

    private fun openBoom() {
        if (binding.edtVehicleNo.text.toString().trim().isEmpty() || binding.edtEntryDateTime.text.toString().trim().isEmpty()){
            showToast("Please ensure all fields are filled before exiting.")
            return
        }

        (requireActivity() as MainActivity).btManager.sendData("1".toByteArray())

        viewModel.collectionInsert(binding.edtVehicleNo.text.toString(),binding.edtChargableAmount.text.toString().toDouble())
    }

    /**
     * Modified printReceipt to accept a VehicleParkingResponse and print all its details.
     */
    private fun printReceipt(response: VehicleParkingResponse) {
        PrinterSdk.getInstance().getPrinter(this.context, object : PrinterSdk.PrinterListen {
            override fun onDefPrinter(printer: PrinterSdk.Printer?) {
                // Increase canvas height as needed.
                printer?.canvasApi()?.initCanvas(
                    BaseStyle.getStyle()
                        .setWidth(410)
                        .setHeight(380)
                )

                var currentY = 10
                val lineHeight = 40  // increased line height for larger text

                // Helper function to render each line with updated style.
                fun renderLine(text: String) {
                    printer?.canvasApi()?.renderText(
                        text,
                        TextStyle.getStyle()
                            .setTextSize(25)  // Increase font size
                            .enableBold(true)    // Make text bold for better visibility
                            .setPosX(0)
                            .setPosY(currentY)
                    )
                    currentY += lineHeight
                }

//                renderLine("Msg: ${response.msg}")
                renderLine("Vehicle No: ${response.vehicleNo}")
//                renderLine("Vehicle Type Id: ${response.vehicleTypeId}")
//                renderLine("IO Type: ${response.iOType}")
//                renderLine("Device Id: ${response.deviceId}")
                renderLine("Chargable Amount: ${response.chargableAmount}")
//                renderLine("Parking Id: ${response.vehicleParkingId}")
                renderLine("In Time: ${response.inTime}")
                renderLine("Out Time: ${response.outTime ?: "N/A"}")
                renderLine("Duration: ${response.duration ?: "N/A"}")
                renderLine("Location: ${response.location ?: "N/A"}")

                printer?.canvasApi()?.printCanvas(1, null)
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
                            showToast(it.vehicleParkingResponse.msg.toString())
                            binding.edtOutTime.setText(it.vehicleParkingResponse.outTime ?: "N/A")
                            binding.edtDuration.setText(it.vehicleParkingResponse.duration ?: "N/A")
                            binding.edtChargableAmount.setText(it.vehicleParkingResponse.chargableAmount?.toString() ?: "N/A")
//                            clearFields()
//                            (requireActivity() as MainActivity).btManager.sendData("1".toByteArray())
                            // Store the response for later use.
                            latestVehicleParkingResponse = it.vehicleParkingResponse
                        }

                        is QrInOutViewModel.ParkingVehicleEvents.VehicleParkingFailed -> {
                            showToast("Exit failed: ${it.message}")
                            Log.d(TAG, "Exit failed: ${it.message}")
                        }

                        is QrInOutViewModel.ParkingVehicleEvents.CollectionInsertSuccessful -> {

                            clearFields()
                            showToast("Collection inserted successfully")

                            // Use the stored VehicleParkingResponse data to print the receipt.
                            latestVehicleParkingResponse?.let { response ->
                                printReceipt(response)
                            } ?: run {
                                showToast("No parking response data available to print receipt")
                            }
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