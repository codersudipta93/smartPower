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
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.databinding.FragmentNfcWriteBinding
import com.sunmi.printerx.PrinterSdk
import com.sunmi.printerx.style.BaseStyle
import com.sunmi.printerx.style.TextStyle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class NfcWriteFragment : BaseFragment<FragmentNfcWriteBinding>() {
    @Inject
    lateinit var sessionManager: SharedPreferenceManager
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
    private var amount: Double? = null
    private var companyName: String? = null
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
        val vehicleTypes = listOf("Two Wheeler", "Four Wheeler")
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
        // amount = binding.edtAmount.text.toString().toDoubleOrNull() ?: 0.0
        amount = binding.edtAmount.text.toString().toDoubleOrNull() ?: 0.0
        companyName = binding.edtCompanyName.text.toString()
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
            binding.edtContact.error = "Enter contact number"
            isValid = false
        }
        if (binding.edtVehicleNo.text.isNullOrBlank()) {
            binding.edtVehicleNo.error = "Enter vehicle number"
            isValid = false
        }
        if (binding.edtAmount.text.isNullOrBlank()) {
            binding.edtAmount.error = "Enter amount"
            isValid = false
        }

        if (binding.edtDate.text.isNullOrBlank()){
            binding.edtDate.error = "Select Expiry Date"
            isValid = false
        }

        if (binding.edtCompanyName.text.isNullOrBlank()){
            binding.edtCompanyName.error = "Enter company name"
            isValid = false
        }

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
            put("amount", amount)
            put("company_name", companyName)
            put("card_no", tag.id.toHexString())
            put("vehicle_type", vehicleTypeId)
            put("regDate", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
            put("regDateTime", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

            if(vehicleTypeId == "1"){
                put("vehicle_name",  "Four Wheeler")
            }else{
                put("vehicle_name", "Two Wheeler")
            }

        }.toString()

        writeJsonToTag(tag, jsonData) { success ->
            if (success) {
                // Log.d("json data ===>", jsonData);
                val tagId = tag.id.toHexString()
                (requireActivity() as MainActivity).binding.loading.visibility = View.VISIBLE
                viewModel.registerGuest(
                    name!!,
                    contact!!,
                    vehicleNo!!,
                    expiryDate!!,
                    tagId,
                    vehicleTypeId!!.toInt(),
                    amount.toString(),
                    companyName!!
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
                            val guestName = event.data?.cardNo
                            Log.d("issue card res", "$guestName")
                            printCardRegistrationSlip(event.data?.cardNo.toString())

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


    private fun printCardRegistrationSlip(cardNo:String) {
        PrinterSdk.getInstance().getPrinter(requireContext(), object : PrinterSdk.PrinterListen {
            override fun onDefPrinter(printer: PrinterSdk.Printer?) {
                if (printer == null) {
                    Toast.makeText(requireContext(), "Printer not available", Toast.LENGTH_SHORT).show()
                    return
                }

                // Initialize canvas (384 is common width for 58mm Sunmi printers)
                val canvasWidth = 384
                var currentY = 10
                val lineHeight = 38

                printer?.canvasApi()?.initCanvas(
                    BaseStyle.getStyle()
                        .setWidth(410)
                        .setHeight(720)
                )


                fun centerX(text: String, textSize: Int): Int {
                    val charWidth = (textSize * 0.6).toInt() // Better than 0.5 for most fonts
                    val textWidth = charWidth * text.length
                    return ((canvasWidth - textWidth) / 2).coerceAtLeast(0)
                }

                fun renderCenteredText(text: String, size: Int = 25, bold: Boolean = false) {
                    val posX = centerX(text, size)
                    printer?.canvasApi()?.renderText(
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
                    printer?.canvasApi()?.renderText(
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


                val slip = sessionManager.getSlipHeaderFooter();
                val header1 = JSONObject(slip ?: "{}").optString("Header1")
                val header2 = JSONObject(slip ?: "{}").optString("Header2")
                val footer1 = JSONObject(slip ?: "{}").optString("Footer1")
                val footer2 = JSONObject(slip ?: "{}").optString("Footer2")

                // Header
                renderCenteredText("Card Registration Slip", size = 28, bold = true)
                currentY += 1 // Spacing
                if(header1 != "" ) {
                    renderCenteredText(header1, size = 28, bold = true)
                    currentY += 1 // Spacing
                }

                if(header2 != "" ) {
                    renderCenteredText(header2.uppercase(), size = 24, bold = false)
                    renderLine("---------------------------------")
                }
                currentY += 10 // Spacing

                // Body
                renderLine("Card No.       : $cardNo")
                renderLine("Vehicle No.    : $vehicleNo")
                if(vehicleTypeId == "2") {
                    renderLine("Vehicle Type   : Two Wheeler")
                }else{
                    renderLine("Vehicle Type   : Four Wheeler")
                }
                val currentDate =SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                renderLine("Reg. Date      : $currentDate")
                renderLine("Valid upto     : $expiryDate")

                currentY += 1 // Spacing

                renderLine("---------------------------------")
                renderAmount("Recharge Amt.: Rs.$amount")
                renderLine("----------------------------------")
                val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                renderLine("Date of Reg. : $currentDateTime")
                currentY += 18 // Spacing
                if(footer1 != "" ) {
                    renderCenteredText(footer1, size = 21, bold = true)
                }

                if(footer2 != "" ) {
                    renderCenteredText(footer2, size = 21, bold = false)
                }

                printer.canvasApi()?.printCanvas(1, null)
                clearForm()
            }

            override fun onPrinters(printers: MutableList<PrinterSdk.Printer>?) {
                Toast.makeText(requireContext(), "Printed Successfully", Toast.LENGTH_SHORT).show()

            }
        })
    }

    private fun clearForm() {
        binding.edtName.text?.clear()
        binding.edtContact.text?.clear()
        binding.edtVehicleNo.text?.clear()
        binding.edtDate.text?.clear()
        binding.edtAmount.text?.clear()
        binding.edtCompanyName.text?.clear()
        //setupVehicleTypeDropdown()
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
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }
}
