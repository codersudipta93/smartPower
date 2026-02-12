package com.example.parkingagent.UI.fragments.nfcFragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.databinding.FragmentNfcReadBinding
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import okhttp3.OkHttpClient
import android.content.Context
import android.nfc.NdefMessage
import com.example.parkingagent.MainActivity
import com.sunmi.printerx.PrinterSdk
import com.sunmi.printerx.style.BaseStyle
import com.sunmi.printerx.style.TextStyle
import com.example.parkingagent.data.local.SharedPreferenceManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Call
import okhttp3.Callback
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import android.widget.Spinner
import android.graphics.Color
import android.nfc.*
import android.nfc.tech.NdefFormatable
import java.util.Date
import android.view.WindowManager

@AndroidEntryPoint
class NfcReadFragment : BaseFragment<FragmentNfcReadBinding>(){
    @Inject
    lateinit var sessionManager: SharedPreferenceManager
    // Native NFC adapter
    private var nfcAdapter: NfcAdapter? = null
    private var isWaitingForNfc = false
    private var vehicleTypeId: String? = null

    private var vehicleTypeName: String? = null
    //private var selectedVehicleTypeId: String? = null
    private val vehicleTypeList = mutableListOf<String>()
    private val vehicleTypeIdList = mutableListOf<String>()


    private var NfcAlertDialog: AlertDialog? = null
    private var isCardRead = false // 🔸 added flag
    private lateinit var cardJsonData: JSONObject

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_nfc_read
    }

    override fun initView() {
        Log.d("page", "Check card page......");
        super.initView()
        //setupVehicleTypeDropdown()
          getDropdown()
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
            if (nfcAdapter == null) {
                Log.e("NFC", "This device doesn't support NFC.")
                Toast.makeText(
                    requireContext(),
                    "This device doesn't support NFC.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        binding.idBtnNfcWrite.setOnClickListener {
            //findNavController().navigate(R.id.action_id_fragment_nfc_to_id_fragment_nfc_write)
            Log.d("click on Write NFC", "Success")
           // showNfcInputDialog()

            showRechargeDialog()
        }

        binding.edtExpiryDate.setOnClickListener {
            showDatePicker();
        }
    }




    private fun showRechargeDialog() {
       // printRechargeSlip()
        if(isCardRead == false){
            showTapCardDialog("card_read", "", "")
        }else {

            val dialogView =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_nfc_input, null)
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            val edtAmount = dialogView.findViewById<EditText>(R.id.edt_amount)
            val txtDate = dialogView.findViewById<TextView>(R.id.txt_date)
            val btnSubmit = dialogView.findViewById<Button>(R.id.btn_submit)
            val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)

            // Handle date picker logic
            txtDate.setOnClickListener {
                val calendar = Calendar.getInstance()
                val datePicker = DatePickerDialog(
                    requireContext(),
                    { _, year, month, dayOfMonth ->
                        val selectedDate = Calendar.getInstance()
                        selectedDate.set(year, month, dayOfMonth)
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        txtDate.text = sdf.format(selectedDate.time)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

                // ✅ Allow only today and future dates
                datePicker.datePicker.minDate = calendar.timeInMillis

                datePicker.show()
            }

            // Submit button logic
            btnSubmit.setOnClickListener {
                val amount = edtAmount.text.toString().trim()
                val date = txtDate.text.toString().trim()
                if (amount.isEmpty() || amount <= "0") {
                    edtAmount.error = "Please enter recharge amount"
                }else if (amount <= "0") {
                    edtAmount.error = "Amount should be greater than zero"
                } else if (date.isEmpty()) {
                    txtDate.error = "Please select a expiry date"
                } else {
                    dialog.dismiss()
                    // You can use amount and date now
                    Log.d("NFC_INPUT", "Amount: $amount, Date: $date")
                    //rechargeCardApi(amount,date)
                    showTapCardDialog("update_card", amount, date)
                }
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

            dialog.show()
            dialog.window?.setDimAmount(0f)
        }
    }


    private fun handleNfcCard(tag: Tag, amount:String, date:String) {
        if (!isWaitingForNfc) return
        requireActivity().runOnUiThread {
            writeDataToTag(tag,amount,date)
        }
    }

    private fun ByteArray.toHexString(): String = joinToString("") { "%02X".format(it) }

    private fun writeDataToTag(tag: Tag, rechargeAmount:String,expiryDate:String) {
        val jsonData = JSONObject().apply {
            put("name", cardJsonData.optString("name"))
            put("contact", cardJsonData.optString("contact"))
            put("vehicle_no", cardJsonData.optString("vehicle_no"))
            put("expiryDate", expiryDate)
            put("amount", rechargeAmount)
            put("company_name", cardJsonData.optString("company_name"))
            put("card_no", tag.id.toHexString())
            put("vehicle_type", cardJsonData.optString("vehicle_type"))

            put("regDate", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
            put("regDateTime", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            put("vehicle_name", vehicleTypeName)
//            if(cardJsonData.optString("vehicle_type") == "1"){
//                put("vehicle_name",  "Four Wheeler")
//            }else{
//                put("vehicle_name", "Two Wheeler")
//            }


        }.toString()

        Log.d("Tag",tag.id.toHexString())
        Log.d("data",jsonData)
        writeJsonToTag(tag, jsonData) { success ->
            if (success) {
                 Log.d("json data ===>", jsonData);
                val tagId = tag.id.toHexString()
                (requireActivity() as MainActivity).binding.loading.visibility = View.VISIBLE
                rechargeCardApi(rechargeAmount, expiryDate)
                NfcAlertDialog?.dismiss()
            } else {
                Toast.makeText(context, "Write failed. Try again.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun writeJsonToTag(tag: Tag, data: String, callback: (Boolean) -> Unit) {

        try {
            val ndef = Ndef.get(tag) ?: run {
                Log.d("tag", tag.id.toHexString())
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


    private fun rechargeCardApi(
        rechargeAmount: String,
        expiryDate: String,
    ) {
        val client = OkHttpClient()

        Log.d("expiryDate",expiryDate)
        Log.d("CardNo",cardJsonData.optString("card_no"))
        Log.d("RechargeAmount",rechargeAmount)
        Log.d("EntityId",sessionManager.getEntityId().toString())
        Log.d("UserId",sessionManager.getUserId().toString())

        Log.d("Token",sessionManager.getAccessToken().toString())
        Log.d("URL","${sessionManager.getBaseUrl()}Device/GuestCardRecharge")

        val json = JSONObject().apply {
            put("CardExpiryDate", expiryDate)
            put("CardNo", cardJsonData.optString("card_no"))
            put("RechargeAmount", rechargeAmount)
            put("EntityId", sessionManager.getEntityId().toString())
            put("UserId", sessionManager.getUserId().toString())
        }


        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${sessionManager.getBaseUrl()}Device/GuestCardRecharge")
            .addHeader("Authorization", "Bearer ${sessionManager.getAccessToken()}")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Failed to call API", e)
                (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "API call failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("Recharge API_RESPONSE", "Response: $responseBody")

                requireActivity().runOnUiThread {
                    (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Recharge successful!", Toast.LENGTH_SHORT).show()
                        activity?.runOnUiThread {
                            binding.edtAmount.setText(rechargeAmount);
                            binding.edtExpiryDate.setText(expiryDate);
                        }
                        printRechargeSlip(rechargeAmount,expiryDate)

                    } else {
                        (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
                        Toast.makeText(requireContext(), "Recharge failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })
    }


    private fun printRechargeSlip(amount: String,expiryDate: String) {
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
                        .setHeight(730)
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

                fun renderMultilineCenteredText(text: String, size: Int = 25, bold: Boolean = false) {
                    val lines = text.split(", ")
                    for (line in lines) {
                        val words = line.trim().split(" ")
                        val sb = StringBuilder()
                        var lineWidth = 0

                        for (word in words) {
                            val wordWidth = (size * 0.55 * word.length).toInt()
                            if (lineWidth + wordWidth > 375) {
                                renderCenteredText(sb.toString().trim(), size, bold)
                                sb.clear()
                                lineWidth = 0
                            }
                            sb.append("$word ")
                            lineWidth += wordWidth + (size / 2) // spacing
                        }

                        if (sb.isNotEmpty()) {
                            renderCenteredText(sb.toString().trim(), size, bold)
                        }
                    }
                }



                val slip = sessionManager.getSlipHeaderFooter();
                val header1 = JSONObject(slip ?: "{}").optString("Header1")
                val header2 = JSONObject(slip ?: "{}").optString("Header2")
                val footer1 = JSONObject(slip ?: "{}").optString("Footer1")
                val footer2 = JSONObject(slip ?: "{}").optString("Footer2")



                // Header
                renderCenteredText("Card Recharge Slip", size = 28, bold = true)
                currentY += 1 // Spacing


                if(header1.isNotBlank()) {
                    val lines = header1.split("|")
                    for (line in lines) {
                        renderCenteredText(line.trim(), size = 23, bold = true)
                    }
                    currentY += 1 // Spacing
                }


                if(header2.isNotBlank()) {
                    val lines = header2.split("|")
                    for (line in lines) {
                        renderCenteredText(line.trim(), size = 22)
                    }
                    currentY += 1 // Spacing
                }
                currentY += 10 // Spacing


                val CardNo = cardJsonData.optString("card_no")
                val VehicleNo = cardJsonData.optString("vehicle_no")
                val VehicleType = cardJsonData.optString("vehicle_name")
                val currentDate =SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                val regDateTime = cardJsonData.optString("regDateTime")

                // Body
                renderLine("Card No.       : $CardNo")
                renderLine("Vehicle No.    : $VehicleNo")
                renderLine("Vehicle Type   : $VehicleType")
                renderLine("Reg. Date      : $currentDate")
                renderLine("Valid upto     : $expiryDate")

                currentY += 1 // Spacing

                renderLine("---------------------------------")
                val formattedAmount = String.format("%.2f", amount.toDouble())

                renderAmount("Recharge Amt.: Rs.$formattedAmount")
                renderLine("----------------------------------")
                renderLine("Date of Reg. : $currentDateTime")
                currentY += 10 // Spacing
                if(footer1 != "" ) {
                    renderCenteredText(footer1, size = 21, bold = true)
                }

                if(footer2 != "" ) {
                    renderCenteredText(footer2, size = 21, bold = false)
                }

                printer.canvasApi()?.printCanvas(1, null)
            }

            override fun onPrinters(printers: MutableList<PrinterSdk.Printer>?) {
                Toast.makeText(requireContext(), "Printed Successfully", Toast.LENGTH_SHORT).show()
            }
        })
    }




    private fun showTapCardDialog(type:String, amount:String, date:String) {
        if (NfcAlertDialog?.isShowing == true) return

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle("Tap Your Card")
            .setMessage("Touch your NFC card to the device")
            .setCancelable(false)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss();
                isWaitingForNfc = false
            }

        NfcAlertDialog = dialogBuilder.create()
        NfcAlertDialog?.setOnShowListener { isWaitingForNfc = true }
        NfcAlertDialog?.show()

        if(type == "Check_card"){
            enableReaderMode(type,"","")
        }else{
            enableReaderMode(type, amount, date)

        }

    }


    override fun onResume() {
        super.onResume()
        //enableReaderMode()
        showTapCardDialog("check_card", "", "")
    }

    override fun onPause() {
        super.onPause()
        disableReaderMode()
    }

    private fun disableReaderMode() {
        nfcAdapter?.disableReaderMode(requireActivity())
    }

    private fun enableReaderMode(type: String, amount:String, date:String) {
        nfcAdapter?.enableReaderMode(
            requireActivity(),
            { tag ->
                if (type == "check_card") {
                    readJsonFromTag(tag)  // Call readJsonFromTag if type is "check_card"
                } else {
                    handleNfcCard(tag, amount, date)  // Otherwise, call readJson From Tag
                }
            },
            // Here using FLAG_READER_NFC_A (and no platform sounds) for compatibility.
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            null
        )
        //Log.d("Step 1","Call enabled reader code function")
    }




    // OFF code on 12-04-34

//    private fun readJsonFromTag(tag: Tag) {
//        try {
//            val ndef = Ndef.get(tag)
//            if (ndef == null) {
//                activity?.runOnUiThread {
//                    Toast.makeText(context, "Tag doesn't support NDEF.", Toast.LENGTH_LONG).show()
//                }
//            }
//            ndef.connect()
//            val ndefMessage = ndef.ndefMessage
//            ndef.close()
////            Log.d("card check data ===>", String(ndef, Charsets.UTF_8));
//            // Loop through all records in the NDEF message
//            var jsonFound = false
//            for (record in ndefMessage.records) {
//                if (record.tnf == NdefRecord.TNF_MIME_MEDIA &&
//                    record.type.contentEquals("application/json".toByteArray(Charsets.US_ASCII))) {
//                    val jsonData = String(record.payload, Charsets.UTF_8)
//                    Log.d("card data", jsonData)
//                    activity?.runOnUiThread {
//                        updateUIWithJson(jsonData)
//                        Toast.makeText(context, "Data read from tag", Toast.LENGTH_SHORT).show()
//                        isCardRead = true
//                        jsonFound = true;
//                        NfcAlertDialog?.dismiss()
//                      //  Toast.makeText(context, "Data read from tag.", Toast.LENGTH_LONG).show()
//                    }
//
//                }
//            }
//            if (!jsonFound) {
//                activity?.runOnUiThread {
//                    Toast.makeText(context, "No JSON data found on tag.", Toast.LENGTH_LONG).show()
//                }
//            }
//
//        } catch (e: Exception) {
//            Log.d("error happen", e.message.toString())
//            activity?.runOnUiThread {
//                Toast.makeText(context, "Failed to read NFC tag: ${e.message}", Toast.LENGTH_LONG).show()
//            }
//        }
//    }
    //===========================================

    private fun readJsonFromTag(tag: Tag) {
        try {
            val ndef = Ndef.get(tag)
            if (ndef == null) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Tag doesn't support NDEF.", Toast.LENGTH_LONG).show()
                }
                return
            }

            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            ndef.close()

            var jsonFound = false

            for (record in ndefMessage.records) {
                if (
                    record.tnf == NdefRecord.TNF_MIME_MEDIA &&
                    record.type.contentEquals("application/json".toByteArray(Charsets.US_ASCII))
                ) {
                    val jsonData = String(record.payload, Charsets.UTF_8)
                    Log.d("card data", jsonData)

                    // ✅ Set flag before jumping to main thread
                    jsonFound = true

                    activity?.runOnUiThread {
                        updateUIWithJson(jsonData)
                        Toast.makeText(context, "Data read from tag", Toast.LENGTH_SHORT).show()
                        isCardRead = true
                        NfcAlertDialog?.dismiss()
                    }

                    break // stop after first valid JSON
                }
            }

            if (!jsonFound) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "No JSON data found on tag.", Toast.LENGTH_LONG).show()
                }
            }

        } catch (e: Exception) {
            Log.d("error happen", e.message.toString())
            activity?.runOnUiThread {
                Toast.makeText(context, "Failed to read NFC tag: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun updateUIWithJson(jsonData: String) {
        try {
            cardJsonData = JSONObject(jsonData)
            val name = cardJsonData.optString("name")
            val contact = cardJsonData.optString("contact")
            val vehicleNo = cardJsonData.optString("vehicle_no")
            val amount = cardJsonData.optDouble("amount")
            val companyName = cardJsonData.optString("company_name")
            val cardNumber = cardJsonData.optString("card_no")
            val cardExpiry = cardJsonData.optString("expiryDate")
            val vehicleType = cardJsonData.optString("vehicle_type")

            Log.d("vehicleType", vehicleType);

            // Update the UI on the main thread.
            activity?.runOnUiThread {
                binding.edtName.setText(name);
                binding.edtContact.setText(contact);
                binding.edtVehicleNo.setText(vehicleNo);
                binding.edtAmount.setText(amount.toString());
                binding.edtCompanyName.setText(companyName);
                binding.edtTagId.setText(cardNumber);
                binding.edtExpiryDate.setText(cardExpiry);
                val index = vehicleTypeIdList.indexOf(vehicleType)
                if (index != -1) {
                    binding.vehicleTypeDropdown.setSelection(index)
                }
            }
        } catch (e: JSONException) {
            Log.e("NfcReadFragment", "Failed to parse JSON: ${e.message}")
            activity?.runOnUiThread {
                Toast.makeText(context, "Failed to parse JSON data", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.edtExpiryDate.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

//    private fun setupVehicleTypeDropdown() {
//        val vehicleTypes = listOf("Two Wheeler", "Four Wheeler")
//        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, vehicleTypes)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        //binding.vehicleTypeDropdown.adapter = adapter
//        binding.vehicleTypeDropdown.adapter = adapter
//        binding.vehicleTypeDropdown.isEnabled = false
//        binding.vehicleTypeDropdown.isClickable = false
//
//        binding.vehicleTypeDropdown.post {
//            val selectedView = binding.vehicleTypeDropdown.selectedView
//            if (selectedView != null && selectedView is TextView) {
//                selectedView.setTextColor(Color.BLACK) // Now it will work
//            }
//        }
//
//
//        binding.vehicleTypeDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                // Map the position to the appropriate vehicle type ID
//                vehicleTypeId = if (position == 0) "2" else "1"
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
            put("UserId", sessionManager.getUserId().toString())
            put("Token", sessionManager.getAccessToken().toString())
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${sessionManager.getBaseUrl()}Device/GetVehicleType")
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
                vehicleTypeId = vehicleTypeIdList.getOrNull(position)
                vehicleTypeName = vehicleTypeList.getOrNull(position)
                Log.d("VehicleTypeSelection", "Selected ID: $vehicleTypeId, Name: $vehicleTypeName")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                vehicleTypeId = null
                vehicleTypeName = null
                Log.d("VehicleTypeSelection", "Nothing selected")
            }
        }
    }







    /**
     * Enable NFC reader mode.
     */
//    private fun enableReaderMode() {
//        nfcAdapter?.enableReaderMode(
//            requireActivity(),
//            ,
//            // Here using FLAG_READER_NFC_A (and no platform sounds) for compatibility.
//            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
//            null
//        )
//    }

}