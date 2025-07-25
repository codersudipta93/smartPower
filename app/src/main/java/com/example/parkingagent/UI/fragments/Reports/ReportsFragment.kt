package com.example.parkingagent.UI.fragments.Reports
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.parkingagent.MainActivity
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.UI.fragments.Reports.ReportsViewModel
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.databinding.FragmentReportsBinding
import com.example.parkingagent.utils.Utils
import com.sunmi.printerx.PrinterSdk
import com.sunmi.printerx.style.BaseStyle
import com.sunmi.printerx.style.TextStyle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import org.json.JSONException
import android.content.Context
import android.widget.TextView
import org.json.JSONArray

@AndroidEntryPoint
class ReportsFragment : BaseFragment<FragmentReportsBinding>() {
    @Inject
    lateinit var sessionManager: SharedPreferenceManager
    private val viewModel: ReportsViewModel by viewModels()

    private lateinit var fromDateTime: Date
    private lateinit var toDateTime: Date

    private var fromDate: String? = null

    private var collectionList: JSONArray? = null
    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_reports
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // You can use viewModel here
    }
    override fun initView() {

        setCurrentDate()
//        binding.dateRow.setOnClickListener {
//            showDatePicker()
//        }

        binding.btnPrint.setOnClickListener{
            printDailyReport()
        }

        // Initialize default date/time values
        setDefaultDateTimeValues()

        // Set click listeners
        binding.fromDateTimeRow.setOnClickListener {
            showDateTimePicker(requireContext(), isFromDate = true)
        }

        binding.toDateTimeRow.setOnClickListener {
            showDateTimePicker(requireContext(), isFromDate = false)
        }
    }

    private fun setCurrentDate() {
        val currentDate = getFormattedDate(Calendar.getInstance())
       // binding.tvSelectedDate.text = currentDate;

    }
    private fun getFormattedDate(calendar: Calendar): String {
        return String.format(
            Locale.getDefault(),
            "%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1, // Add 1 since months are 0-based
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }



    private fun setDefaultDateTimeValues() {
        val calendar = Calendar.getInstance()

        // Set From Date to start of today (00:00:00)
        val fromCalendar = calendar.clone() as Calendar
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0)
        fromCalendar.set(Calendar.MINUTE, 0)
        fromCalendar.set(Calendar.SECOND, 0)
        fromDateTime = fromCalendar.time

        // Set To Date to end of today (23:59:59)
        val toCalendar = calendar.clone() as Calendar
        toCalendar.set(Calendar.HOUR_OF_DAY, 23)
        toCalendar.set(Calendar.MINUTE, 59)
        toCalendar.set(Calendar.SECOND, 59)
        toDateTime = toCalendar.time

        // Format and display
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        binding.tvFromDateTime.text = dateFormat.format(fromDateTime)
        binding.tvToDateTime.text = dateFormat.format(toDateTime)

        (requireActivity() as MainActivity).binding.loading.visibility = View.VISIBLE
        loadReport()
    }

    private fun showDateTimePicker(context: Context, isFromDate: Boolean) {
        val currentDateTime = if (isFromDate) fromDateTime else toDateTime
        val calendar = Calendar.getInstance().apply { time = currentDateTime }

        DatePickerDialog(
            context,
            { _, year, month, day ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val selectedDateTime = Calendar.getInstance().apply {
                            set(year, month, day, hour, minute)
                            if (!isFromDate) {
                                set(Calendar.SECOND, 59)
                            }
                        }

                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        if (isFromDate) {
                            fromDateTime = selectedDateTime.time
                            binding.tvFromDateTime.text = dateFormat.format(fromDateTime)
                            loadReport()
                        } else {
                            toDateTime = selectedDateTime.time
                            binding.tvToDateTime.text = dateFormat.format(toDateTime)
                            loadReport()
                        }
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

               // binding.tvSelectedDate.text = dateFormat.format(selectedDate.time);
                (requireActivity() as MainActivity).binding.loading.visibility = View.VISIBLE

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
   }



    fun loadReport (){
        val client = OkHttpClient()

       // Log.d("expiryDate",binding.tvSelectedDate.text.toString())
        Log.d("Device ID", Utils.getDeviceId(requireActivity()))
        Log.d("from date", fromDate.toString())


        val json = JSONObject().apply {
          //  put("Date", binding.tvSelectedDate.text.toString())
            put("DeviceId", Utils.getDeviceId(requireActivity()))
            put("EntityId", sessionManager.getEntityId().toString())
            put("UserId", sessionManager.getUserId().toString())
            put("FromDate", binding.tvFromDateTime.text.toString())
            put("ToDate", binding.tvToDateTime.text.toString())
        }


        Log.d("Request of report data", json.toString())
        Log.d("request data", sessionManager.getAccessToken().toString())
        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${sessionManager.getBaseUrl()}Device/DailyCollectionReportFinal")
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

                Log.d("Raw API Response", responseBody ?: "Null response")

                if (response.isSuccessful && responseBody != null) {
                    try {
                        val jsonObject = JSONObject(responseBody)
                        val dataObject = jsonObject.optJSONObject("Data")


                        val totalCount = dataObject.optString("TotalCount", "0")
                        val totalAmount = dataObject.optString("TotalCash", "0.00") // Note: This is a special character key

                         collectionList = dataObject.optJSONArray("VehicleTypeWiseCollections")
                        requireActivity().runOnUiThread {
                            binding.vehicleCardContainer.removeAllViews()

                            collectionList?.let { list ->
                                for (i in 0 until list.length()) {
                                    val item = list.getJSONObject(i)

                                    val vehicleType = item.optString("VehicleTypeName", "Unknown Vehicle")
                                    val cashCount = item.optString("CashCount", "0")
                                    val cashAmount = item.optString("CashAmount", "0.00")
                                    val cardCount = item.optString("CardCount", "0")
                                    val cardAmount = item.optString("CardAmount", "0.00")

                                    addVehicleCard(vehicleType, cashCount, cashAmount, cardCount, cardAmount)
                                }
                            }


                            binding.tvTotalCount.text = dataObject.optString("TotalCount", "0")
                            binding.tvTotalAmount.text = "Rs. ${dataObject.optString("TotalCash", "0.00")}"

                            binding.summaryCard.visibility = View.VISIBLE
                            binding.btnPrint.visibility = View.VISIBLE
                            (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
                        }


                    } catch (e: JSONException) {
                        Log.e("JSON_PARSE_ERROR", "Error parsing response JSON", e)
                        (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
                    }
                } else {
                    requireActivity().runOnUiThread {
                        (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
                        Toast.makeText(requireContext(), "Report fetch failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }


        })
    }


    private fun addVehicleCard(
        vehicleType: String,
        cashCount: String,
        cashAmount: String,
        cardCount: String,
        cardAmount: String
    ) {
        val inflater = LayoutInflater.from(requireContext())
        val cardView = inflater.inflate(R.layout.vehicle_type_card, binding.vehicleCardContainer, false)

        cardView.findViewById<TextView>(R.id.tvVehicleTypeTitle).text = vehicleType
        cardView.findViewById<TextView>(R.id.tvCashCount).text = cashCount
        cardView.findViewById<TextView>(R.id.tvCashAmount).text = "Rs. $cashAmount"
        cardView.findViewById<TextView>(R.id.tvCardCount).text = cardCount
        cardView.findViewById<TextView>(R.id.tvCardAmount).text = "Rs. $cardAmount"

        binding.vehicleCardContainer.addView(cardView)
    }




    private fun printDailyReport() {

        PrinterSdk.getInstance().getPrinter(requireContext(), object : PrinterSdk.PrinterListen {
            override fun onDefPrinter(printer: PrinterSdk.Printer?) {
                if (printer == null) {
                    Toast.makeText(requireContext(), "Printer not available", Toast.LENGTH_SHORT).show()
                    return
                }

                val canvasWidth = 384
                var currentY = 10
                val lineHeight = 38

                printer.canvasApi()?.initCanvas(
                    BaseStyle.getStyle()
                        .setWidth(384)
                        .setHeight(1410)
                )

                fun centerX(text: String, textSize: Int): Int {
                    // 0.55 instead of 0.6 for better centering in some fonts
                    val charWidth = (textSize * 0.55).toInt()
                    val textWidth = charWidth * text.length
                    val x = (canvasWidth - textWidth) / 2
                    return x.coerceAtLeast(0)
                }

                fun renderCenteredText(text: String, size: Int = 25, bold: Boolean = false) {
                    val posX = centerX(text, size)+10
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

                fun renderHeaderText(text: String, size: Int = 25, bold: Boolean = false) {
                    val posX = centerX(text, size)+25
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
                            .setTextSize(22)
                            .enableBold(true)
                            .setPosX(25) // Added left margin
                            .setPosY(currentY)
                    )
                    currentY += lineHeight
                }

                fun renderAmount(text: String) {
                    printer?.canvasApi()?.renderText(
                        text,
                        TextStyle.getStyle()
                            .setTextSize(25)
                            .enableBold(true)
                            .setPosX(25) // Added left margin
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




                val slip = sessionManager.getSlipHeaderFooter()
                val header1 = JSONObject(slip ?: "{}").optString("Header1")
                  if(header1.isNotBlank()) {
                    val lines = header1.split("|")
                    for (line in lines) {
                        renderCenteredText(line.trim(), size = 28, bold = true)
                    }
                    currentY += 1 // Spacing
                }


                val header2 = JSONObject(slip ?: "{}").optString("Header2")
                val footer1 = JSONObject(slip ?: "{}").optString("Footer1")
                val footer2 = JSONObject(slip ?: "{}").optString("Footer2")

                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                    Date()
                )

//                if (header1.isNotEmpty()) {
//                    renderCenteredText(header1, size = 25, bold = true)
//                    currentY += 2
//                }

                if (header2.isNotEmpty()) {
                    renderCenteredText(header2, size = 22)
                    currentY += 1
                }

                renderHeaderText("COLLECTION SUMMARY", size = 28, bold = true)
                renderCenteredText("AGENT: ${sessionManager.getFullName()}", size = 22)

                renderLine("---------------------------------")

                renderLine("From Date : "+binding.tvFromDateTime.text.toString())
                renderLine("To Date   : "+binding.tvToDateTime.text.toString())

                renderLine("---------------------------------")

//                renderAmount("TWO WHEELER")
//                currentY += 10
//                renderLine("          COUNT       AMOUNT")
//                //renderLine("CASH      "+binding.cashTwoWheelerCount.text.toString()+"          "+binding.cashTwoWheelerAmount.text.toString())
//                //renderLine("CARD      "+binding.cardTwoWheelerCount.text.toString()+"          "+binding.cardTwoWheelerAmount.text.toString())
//
//                renderLine("---------------------------------")
//
//                renderAmount("FOUR WHEELER")
//                currentY += 10
//                renderLine("           COUNT      AMOUNT")
//                //renderLine("CASH       "+binding.cashFourWheelerCount.text.toString()+"        "+binding.cashFourWheelerAmount.text.toString())
//               // renderLine("CARD       "+binding.cardFourWheelerCount.text.toString()+"        "+binding.cardFourWheelerAmount.text.toString())
//


                val vehicleArray = collectionList

                if (vehicleArray != null) {
                    for (i in 0 until vehicleArray.length()) {
                        val vehicleItem = vehicleArray.getJSONObject(i)
                        val vehicleName = vehicleItem.optString("VehicleTypeName", "")
                        val cashCount = vehicleItem.optString("CashCount", "0")
                        val cardCount = vehicleItem.optString("CardCount", "0")
                        val notCollectedCount = vehicleItem.optString("NotCollectedCount", "0")
                        val cashAmount = vehicleItem.optString("CashAmount", "0.00")
                        val cardAmount = vehicleItem.optString("CardAmount", "0.00")
                        val notCollectedAmount = vehicleItem.optString("NotCollectedAmount", "0.00")
                        currentY += 10
                        renderAmount(vehicleName.uppercase())
                        currentY += 10
                        renderLine("           COUNT      AMOUNT")

                        renderLine("CASH        $cashCount         $cashAmount")
                        renderLine("CARD        $cardCount         $cardAmount")

                    }
                }
















                //======
                currentY += 20
                renderLine("=================================")
                renderAmount("TOTAL COUNT   :  "+binding.tvTotalCount.text.toString())
                renderAmount("TOTAL AMOUNT  :  "+binding.tvTotalAmount.text.toString())
                renderLine("==================================")


                currentY += 25

                // Footer
                if (footer1.isNotEmpty()) {
                    renderCenteredText(footer1, size = 22, bold = true)
                    currentY += 5
                }

                if (footer2.isNotEmpty()) {
                    renderCenteredText(footer2, size = 20)
                }

                renderLine("---------------------------------")
                renderCenteredText("Printed On : $currentDateTime", size = 22)

                // Print
                printer.canvasApi()?.printCanvas(1, null)

            }

            override fun onPrinters(printers: MutableList<PrinterSdk.Printer>?) {
                Toast.makeText(requireContext(), "Report printed successfully", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun printDailyReportOld() {
        PrinterSdk.getInstance().getPrinter(requireContext(), object : PrinterSdk.PrinterListen {
            override fun onDefPrinter(printer: PrinterSdk.Printer?) {
                if (printer == null) {
                    Toast.makeText(requireContext(), "Printer not available", Toast.LENGTH_SHORT).show()
                    return
                }

                val canvasWidth = 384
                var currentY = 10
                val lineHeight = 38

                printer.canvasApi()?.initCanvas(
                    BaseStyle.getStyle()
                        .setWidth(384)
                        .setHeight(1050)
                )

                fun centerX(text: String, textSize: Int): Int {
                    // 0.55 instead of 0.6 for better centering in some fonts
                    val charWidth = (textSize * 0.55).toInt()
                    val textWidth = charWidth * text.length
                    val x = (canvasWidth - textWidth) / 2
                    return x.coerceAtLeast(0)
                }

                fun renderCenteredText(text: String, size: Int = 25, bold: Boolean = false) {
                    val posX = centerX(text, size)+10
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

                fun renderHeaderText(text: String, size: Int = 25, bold: Boolean = false) {
                    val posX = centerX(text, size)+25
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
                            .setTextSize(22)
                            .enableBold(true)
                            .setPosX(25) // Added left margin
                            .setPosY(currentY)
                    )
                    currentY += lineHeight
                }

                fun renderAmount(text: String) {
                    printer?.canvasApi()?.renderText(
                        text,
                        TextStyle.getStyle()
                            .setTextSize(25)
                            .enableBold(true)
                            .setPosX(25) // Added left margin
                            .setPosY(currentY)
                    )
                    currentY += lineHeight
                }



                val slip = sessionManager.getSlipHeaderFooter()
                val header1 = JSONObject(slip ?: "{}").optString("Header1")
                val header2 = JSONObject(slip ?: "{}").optString("Header2")
                val footer1 = JSONObject(slip ?: "{}").optString("Footer1")
                val footer2 = JSONObject(slip ?: "{}").optString("Footer2")

                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                // Header
                renderHeaderText("DAILY COLLECTION REPORT", size = 28, bold = true)
                currentY += 5

                  if(header1.isNotBlank()) {
                    val lines = header1.split("|")
                    for (line in lines) {
                        renderCenteredText(line.trim(), size = 28, bold = true)
                    }
                    currentY += 1 // Spacing
                }


                if (header2.isNotEmpty()) {
                    renderCenteredText(header2, size = 22)
                    currentY += 5
                }

                renderLine("---------------------------------")

               // renderLine("Report Date: ${binding.tvSelectedDate.text}")
                renderLine("Name: ${sessionManager.getFullName()}")
                renderLine("Printed On: $currentDateTime")
                currentY += 10

                renderLine("---------------------------------")
                renderCenteredText("COLLECTED AMOUNT", size = 25, bold = true)
                renderLine("---------------------------------")

                //renderLine("Two Wheeler  :         "+binding.collectedTwoWheeler.text.toString())
                //renderLine("Four Wheeler :         "+binding.collectedFourWheeler.text.toString())
                renderLine("---------------------------------")
              // renderAmount("TOTAL      :        "+binding.collectedTotalAmount.text.toString())

                currentY += 15


                renderLine("---------------------------------")
                renderCenteredText("NOT COLLECTED AMOUNT", size = 25, bold = true)
                renderLine("---------------------------------")

                //renderLine("Two Wheeler  :         "+binding.notCollectedTwoWheeler.text.toString())
              //  renderLine("Four Wheeler :         "+binding.notCollectedFourWheeler.text.toString())
                renderLine("---------------------------------")
               // renderAmount("TOTAL      :        "+binding.notCollectedTotalAmount.text.toString())

                currentY += 35

                // Footer
                if (footer1.isNotEmpty()) {
                    renderCenteredText(footer1, size = 22, bold = true)
                    currentY += 5
                }

                if (footer2.isNotEmpty()) {
                    renderCenteredText(footer2, size = 20)
                }

                // Print
                printer.canvasApi()?.printCanvas(1, null)
            }

            override fun onPrinters(printers: MutableList<PrinterSdk.Printer>?) {
                Toast.makeText(requireContext(), "Report printed successfully", Toast.LENGTH_SHORT).show()
            }
        })
    }



}
