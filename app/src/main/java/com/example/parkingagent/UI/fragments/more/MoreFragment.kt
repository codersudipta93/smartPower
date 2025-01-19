package com.example.parkingagent.UI.fragments.more

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
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.databinding.FragmentMoreBinding
import com.example.parkingagent.utils.Utils
import com.example.parkingagent.utils.scanner.IScanInterface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject

@AndroidEntryPoint
class MoreFragment : BaseFragment<FragmentMoreBinding>() {

    companion object {
        const val TAG = "Scan-Test"
        const val ACTION_DATA_CODE_RECEIVED = "com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED"
        const val DATA = "data"
    }

    private val viewModel: MoreViewModel by viewModels()
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
        return R.layout.fragment_more
    }

    override fun initView() {
        super.initView()
        bindScannerService()
        registerReceiver()
        setupExitButton()
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
            } else {
                showToast("Invalid QR Code data.")
            }
        } catch (e: Exception) {
            showToast("Failed to parse QR Code data: ${e.message}")
        }
    }

    private fun setupExitButton() {
        binding.btnExit.setOnClickListener {
            val vehicleNumber = binding.edtVehicleNo.text.toString().trim()
            val entryDateTime = binding.edtEntryDateTime.text.toString().trim()

            if (vehicleNumber.isEmpty() || entryDateTime.isEmpty()) {
                showToast("Please ensure all fields are filled before exiting.")
                return@setOnClickListener
            }

            val deviceId = Utils.getDeviceId(requireContext()) // Replace with actual device ID fetching logic
            viewModel.parkedVehicle(vehicleNumber, deviceId)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mutualSharedflow.collectLatest {
                    when (it) {
                        is MoreViewModel.ParkingVehicleEvents.VehicleParkingSuccessful -> {
                            showToast(it.vehicleParkingResponse.msg.toString())
                            clearFields()
                        }

                        is MoreViewModel.ParkingVehicleEvents.VehicleParkingFailed -> {
                            showToast("Exit failed: ${it.message}")
                        }
                    }
                }
            }
        }
    }

    private fun clearFields() {
        binding.edtVehicleNo.text?.clear()
        binding.edtEntryDateTime.text?.clear()
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
