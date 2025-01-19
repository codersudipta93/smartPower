package com.example.parkingagent.UI.fragments.home

import android.graphics.Bitmap
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.databinding.FragmentHomeBinding
import com.example.parkingagent.utils.Utils
import com.google.gson.JsonObject
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.sunmi.printerx.PrinterSdk
import com.sunmi.printerx.enums.Align
import com.sunmi.printerx.style.BaseStyle
import com.sunmi.printerx.style.QrStyle

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()
    private var selectedVehicleTypeId: String? = null

    public var selectedVehicleTypeName:String?=null
    public var vehicleNumber:String?=null



    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_home
    }

    override fun initView() {
        super.initView()

        // Initialize the dropdown menu
        setupVehicleTypeDropdown()

        // Handle button click
        binding.btnPark.setOnClickListener {

            if (binding.edtVehicleNo.text.toString().trim().isEmpty()) {
                showToast("Please enter the vehicle number")
                return@setOnClickListener
            }
            if (selectedVehicleTypeId.isNullOrEmpty()) {
                showToast("Please select the vehicle type")
                return@setOnClickListener
            }
            val deviceId = Utils.getDeviceId(requireContext()) // Replace with your method to fetch deviceId
            this.vehicleNumber=binding.edtVehicleNo.text.toString()
            viewModel.parkedVehicle(this.vehicleNumber.toString(), selectedVehicleTypeId!!, deviceId)
        }

        // Observe ViewModel events
        observeViewModel()
    }

    private fun setupVehicleTypeDropdown() {
        val vehicleTypes = listOf("Two-Wheeler", "Four-Wheeler")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, vehicleTypes)
        binding.vehicleTypeDropdown.setAdapter(adapter)

        binding.vehicleTypeDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedVehicleTypeId = if (position == 0) "2" else "1"
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mutualSharedflow.collectLatest {
                    when (it) {
                        is HomeViewModel.ParkingVehicleEvents.VehicleParkingSuccessful -> {
                            showToast("Vehicle parked successfully!")
                            printReceipt(this@HomeFragment.vehicleNumber.toString(),if(this@HomeFragment.selectedVehicleTypeId.toString()=="2") "Two Wheeler" else "Four Wheeler")
                        }

                        is HomeViewModel.ParkingVehicleEvents.VehicleParkingFailed -> {
                            showToast("Failed to park vehicle: ${it.toString()}")
                        }
                    }
                }
            }
        }
    }

    private fun printReceipt(vehicleNumber: String, vehicleType: String) {
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
