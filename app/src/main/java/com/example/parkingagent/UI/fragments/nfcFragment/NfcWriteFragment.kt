package com.example.parkingagent.UI.fragments.nfcFragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.parkingagent.MainActivity
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.databinding.FragmentNfcWriteBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class NfcWriteFragment : BaseFragment<FragmentNfcWriteBinding>() {

    private val viewModel: NfcViewModel by viewModels()
    private var nfcAdapter: NfcAdapter? = null
    private var isWaitingForNfc = false
    private var dialog: AlertDialog? = null

    // Form data variables
    private var name: String? = null
    private var contact: String? = null
    private var vehicleNo: String? = null
//    private var amount: Double? = null
    private var expiryDate: String? = null
    private var vehicleTypeId: String? = null

    override fun getLayoutResourceId(): Int = R.layout.fragment_nfc_write

    override fun onResume() {
        super.onResume()
        enableReaderMode()
    }

    override fun onPause() {
        super.onPause()
        disableReaderMode()
    }

    private fun disableReaderMode() {
        nfcAdapter?.disableReaderMode(requireActivity())
    }

    override fun initView() {
        super.initView()
        setupVehicleTypeDropdown()
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext()).also {
            if (it == null) Toast.makeText(context, "NFC not supported", Toast.LENGTH_LONG).show()
        }
        binding.btnGuestRegister.setOnClickListener { registerGuest() }

        binding.edtDate.setOnClickListener {
            showDatePicker()
        }


    }

//    private fun setupVehicleTypeDropdown() {
//        val vehicleTypes = listOf("Two-Wheeler", "Four-Wheeler")
//        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, vehicleTypes)
//        binding.vehicleTypeDropdown.setAdapter(adapter)
//        binding.vehicleTypeDropdown.setOnItemClickListener { _, _, position, _ ->
//            vehicleTypeId = if (position == 0) "2" else "1"
//        }
//    }

    private fun setupVehicleTypeDropdown() {
        val vehicleTypes = listOf("Two-Wheeler", "Four-Wheeler")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, vehicleTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.vehicleTypeDropdown.adapter = adapter

        binding.vehicleTypeDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Map the position to the appropriate vehicle type ID
                vehicleTypeId = if (position == 0) "2" else "1"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Optionally, set a default value or leave it empty
            }
        }
    }


    private fun registerGuest() {
        if (!validateForm()) return

        // Store form data
        name = binding.edtName.text.toString()
        contact = binding.edtContact.text.toString()
        vehicleNo = binding.edtVehicleNo.text.toString()
//        amount = binding.edtAmount.text.toString().toDoubleOrNull() ?: 0.0
        expiryDate=binding.edtDate.text.toString()
        vehicleTypeId = vehicleTypeId ?: return

        Log.d("expiryDate",expiryDate.toString());
        //2025-03-28

        showNfcDialog()
    }

    private fun validateForm(): Boolean {
        var isValid = true
        if (binding.edtName.text.isNullOrBlank()) {
            binding.edtName.error = "Enter name"
            isValid = false
        }
        if (binding.edtContact.text.isNullOrBlank() || binding.edtContact.text!!.length < 10) {
            binding.edtContact.error = "Invalid contact"
            isValid = false
        }
        if (binding.edtVehicleNo.text.isNullOrBlank()) {
            binding.edtVehicleNo.error = "Enter vehicle number"
            isValid = false
        }
//        if (binding.edtAmount.text.isNullOrBlank()) {
//            binding.edtAmount.error = "Enter amount"
//            isValid = false
//        }

        if (binding.edtDate.text.isNullOrBlank()){
            binding.edtDate.error = "Select Date"
            isValid = false
        }

//        if (binding.vehicleTypeDropdown.text.isNullOrBlank()) {
//            binding.vehicleTypeDropdown.error = "Select vehicle type"
//            isValid = false
//        }

        return isValid

    }

    private fun showNfcDialog() {
        dialog = AlertDialog.Builder(requireContext())
            .setTitle("Tap Your Card")
            .setMessage("Touch your NFC card to the device")
            .setCancelable(false)
            .setNegativeButton("Cancel") { dialog, _ ->
                isWaitingForNfc = false
                dialog.dismiss()
            }
            .create()
        dialog?.setOnShowListener { isWaitingForNfc = true }
        dialog?.show()
    }

    private fun enableReaderMode() {
        nfcAdapter?.enableReaderMode(
            requireActivity(),
            { tag -> handleNfcTag(tag) },
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            null
        )
    }

    private fun handleNfcTag(tag: Tag) {
        if (!isWaitingForNfc) return
        requireActivity().runOnUiThread {
            writeDataToTag(tag)
        }
    }

    private fun writeDataToTag(tag: Tag) {
        val jsonData = JSONObject().apply {
            put("name", name)
            put("contact", contact)
            put("vehicle_no", vehicleNo)
            put("expiryDate", expiryDate)
        }.toString()

        writeJsonToTag(tag, jsonData) { success ->
            if (success) {
                val tagId = tag.id.toHexString()
                (requireActivity() as MainActivity).binding.loading.visibility = View.VISIBLE
                viewModel.registerGuest(
                    name!!,
                    contact!!,
                    vehicleNo!!,
                    expiryDate!!,
                    tagId,
                    vehicleTypeId!!.toInt()
                )
                isWaitingForNfc = false
                dialog?.dismiss()
            } else {
                Toast.makeText(context, "Write failed. Try again.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun writeJsonToTag(tag: Tag, data: String, callback: (Boolean) -> Unit) {
        try {
            val ndef = Ndef.get(tag) ?: run {
                Toast.makeText(context, "Tag not supported", Toast.LENGTH_LONG).show()
                callback(false)
                return
            }
            ndef.connect()
            if (!ndef.isWritable) {
                Toast.makeText(context, "Tag not writable", Toast.LENGTH_LONG).show()
                ndef.close()
                callback(false)
                return
            }
            val mimeRecord = NdefRecord.createMime("application/json", data.toByteArray())
            val message = NdefMessage(arrayOf(mimeRecord))
            ndef.writeNdefMessage(message)
            ndef.close()
            Toast.makeText(context, "Write successful", Toast.LENGTH_LONG).show()
            callback(true)
        } catch (e: Exception) {
            Log.e("NFC_ERROR", "Write failed", e)
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            callback(false)
        }
    }

    override fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mutualSharedflow.collectLatest { event ->
                    (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
                    when (event) {
                        is NfcViewModel.GuestEvents.GuestRegistered -> {
                            clearForm()
                            Toast.makeText(context, "Guest registered", Toast.LENGTH_LONG).show()
                        }
                        is NfcViewModel.GuestEvents.Error -> {
                            Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun clearForm() {
        binding.edtName.text?.clear()
        binding.edtContact.text?.clear()
        binding.edtVehicleNo.text?.clear()
        binding.edtDate.text?.clear()
//        binding.vehicleTypeDropdown.text?.clear()
    }

    // Extension to convert ByteArray to Hex String
    private fun ByteArray.toHexString(): String = joinToString("") { "%02X".format(it) }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.edtDate.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }
}