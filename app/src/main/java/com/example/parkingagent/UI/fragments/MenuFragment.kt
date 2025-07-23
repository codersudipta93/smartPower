package com.example.parkingagent.UI.fragments

import android.graphics.PorterDuff
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parkingagent.MainActivity
import com.example.parkingagent.R
import com.example.parkingagent.UI.adapters.MenuAdapter
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.UI.decorators.GridSpacingItemDecoration
import com.example.parkingagent.UI.fragments.home.HomeViewModel
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.data.remote.models.Menu.MenuDataItem
import com.example.parkingagent.databinding.FragmentMenuBinding
import com.example.parkingagent.utils.SharedViewModel
import com.example.parkingagent.utils.Utils
import com.sunmi.printerx.PrinterSdk
import com.sunmi.printerx.style.BaseStyle
import com.sunmi.printerx.style.TextStyle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.sunmi.printerx.style.QrStyle
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
import android.net.Uri
import javax.inject.Inject
import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

import java.util.*

@AndroidEntryPoint
class MenuFragment : BaseFragment<FragmentMenuBinding>() {
    @Inject
    lateinit var sessionManager: SharedPreferenceManager
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: MenuAdapter
    private var collectionList: JSONArray? = null

    override fun getLayoutResourceId(): Int {
       return R.layout.fragment_menu
    }

    override fun initView() {
        super.initView()
        //performLogout()
        Log.d("Install Version", sessionManager.getInstalledVersion())

        lifecycleScope.launch {
//            delay(1000)
            // install app version
//            if(sessionManager.getInstalledVersion() != sessionManager.getCurrentAppVersion()){
//                showAlertMsg("A new version of Smart Power (${sessionManager.getCurrentAppVersion()}) is available! Please update to latest version")
//            }
        }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing to disable back press
            }
        })


        setupRecyclerView()
        setupObservers()

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation {
                //performLogout()
                logoutAPI()
            }
        }

        binding.idTxtFullname.text="Welcome, "+sessionManager.getFullName()
        binding.idTxtVersion.text="App Version - "+sessionManager.getInstalledVersion()


        binding.idTxtLocation.text=sessionManager.getLocation()

//        (requireActivity() as MainActivity).isBluetoothConnected
//            .observe(viewLifecycleOwner) { connected ->
//                val tintColor = ContextCompat.getColor(
//                    requireContext(),
//                    if (connected) R.color.green else R.color.red
//                )
//                binding.imgBtStatus.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
//            }

//        binding.imgBtStatus.setOnClickListener {
//            navigate(R.id.id_boomBarrierFragment)
//        }

        (requireActivity() as MainActivity).binding.loading.visibility= View.VISIBLE
        sharedViewModel.loadMenu()
        //printRechargeSlip()
    }


    fun showAlertMsg(msg: String) {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Hold on!")
            .setMessage(msg)
            .setCancelable(false)
            .setNegativeButton("Update") { d, _ ->
                d.dismiss()

                val url = "http://45.249.111.51/smartpowerapk/"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                requireContext().startActivity(intent)
            }
            .create()

        dialog.show()
    }


    private fun setupRecyclerView() {
        val spanCount = 2
        adapter = MenuAdapter(emptyList()) { menuItem ->
            navigateToDestination(menuItem.appMenuId)
        }
        binding.recyclerViewMenu.apply {
            layoutManager = GridLayoutManager(requireContext(), spanCount)
            adapter = this@MenuFragment.adapter

            // convert your desired dp to px
            val spacingPx = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._16sdp)
            addItemDecoration(
                GridSpacingItemDecoration(
                    spanCount = spanCount,
                    spacing = spacingPx,
                    includeEdge = true
                )
            )
        }
    }


//    private fun setupObservers() {
//        sharedViewModel.menuItems.observe(viewLifecycleOwner) { items ->
////            Log.d("MenuFragment", "Received ${items.size} menu items")
//            if (items.isNotEmpty()) {
//                adapter.updateItems(items)
//            } else {
//                showEmptyState()
//            }
//        }
//    }

    private fun setupObservers() {
        sharedViewModel.menuItems.observe(viewLifecycleOwner) { allItems ->
            (requireActivity() as MainActivity).binding.loading.visibility= View.GONE
            val parentItems = allItems.filter { it.parentId == 0 }
            if (parentItems.isNotEmpty()) {
                adapter.updateItems(parentItems)
            }
            else {
                showEmptyState()
            }
        }
    }

    private fun showEmptyState() {
        Toast.makeText(requireContext(), "No menu items available", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToDestination(appMenuId: Int?) {
        when (appMenuId) {
            1 -> {navigate(R.id.id_prkingMenuFragment)}
            2 -> navigate(R.id.id_cardSettingsFragment)
            3 -> navigate(R.id.id_reportsFragment      )
            5 -> navigate(R.id.id_fragment_card_in)
            6 -> navigate(R.id.id_fragment_card_out)
            7 -> navigate(R.id.id_homeFragment)
            8 -> navigate(R.id.id_qrOutFragment)
            9 -> navigate(R.id.id_fragment_nfc_write)
            10 -> navigate(R.id.id_fragment_nfc)
          //11 -> navigate(R.id.id_surrenderCardFragment)
            25 -> navigate(R.id.id_parkingRateFragment)
            // Add other mappings as needed
            15 -> navigate(R.id.id_ipSetupFragment)
            else -> showInvalidMenuError()
        }
    }

    private fun navigate(destinationId: Int) {
        findNavController().navigate(destinationId)
    }

    private fun showInvalidMenuError() {
        Toast.makeText(requireContext(), "Invalid menu option", Toast.LENGTH_SHORT).show()
    }

//    private fun performLogout() {
//        Log.d("Logout", "Logout")
//        sessionManager.clearToken()
//        sessionManager.setLoginStatus(false)
//
//        // Create navigation options to clear back stack
//        val navOptions = NavOptions.Builder()
//            .setPopUpTo(R.id.nav_graph, true) // Use your nav graph resource id here
//            .build()
//
//        // Navigate to login with clear back stack
//        findNavController().navigate(R.id.id_loginFragment, null, navOptions)
//
//    }

    private fun performLogout() {
        try {
            Log.d("Logout", "Starting logout process")
            // First clear credentials
            sessionManager.clearToken()
            sessionManager.setLoginStatus(false)

            // Then navigate safely
            if (isAdded && !isDetached() && view != null) {
                // Create navigation options to clear back stack
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()

                // Navigate to login
                findNavController().navigate(R.id.id_loginFragment, null, navOptions)
                Log.d("Logout", "Navigation complete")
            } else {
                Log.e("Logout", "Fragment not attached or view is null")
            }
        } catch (e: Exception) {
            Log.e("Logout", "Error during logout: ${e.message}")
        }
    }

    private fun showLogoutConfirmation(onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to log out?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Logout") { dialog, _ ->
                dialog.dismiss()
                onConfirm()
            }
            .create()
            .show()
    }



    fun logoutAPI (){
        (requireActivity() as MainActivity).binding.loading.visibility = View.VISIBLE
        val client = OkHttpClient()

        Log.d("Device ID", Utils.getDeviceId(requireActivity()))
        Log.d("Device ID", sessionManager.getAccessToken().toString())


        val json = JSONObject().apply {
            put("DeviceId", Utils.getDeviceId(requireActivity()))
            put("EntityId", sessionManager.getEntityId().toString())
            put("UserId", sessionManager.getUserId().toString())
            put("Token", sessionManager.getAccessToken())
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${sessionManager.getBaseUrl()}Device/AgentLogOutFinalReport")
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

                Log.d("Raw API Response", responseBody ?: "Null response");


                if (response.isSuccessful && responseBody != null) {
                    try {

                      requireActivity().runOnUiThread {
                           (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
                           Toast.makeText(requireContext(), "Logout Successful", Toast.LENGTH_SHORT).show()
                            (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
                        }
                        val jsonObject = JSONObject(responseBody)
                        val dataObject = jsonObject.optJSONObject("Data")
                        collectionList = dataObject.optJSONArray("VehicleTypeWiseCollections")

                        printDailyReport(responseBody)

                    } catch (e: JSONException) {
                        Log.e("JSON_PARSE_ERROR", "Error parsing response JSON", e)
                        ( requireActivity() as MainActivity).binding.loading.visibility = View.GONE
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



    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }



    private var isPrinting = false
    private fun printDailyReport(responseBody: String) {
        isPrinting = true
        PrinterSdk.getInstance().getPrinter(requireContext(), object : PrinterSdk.PrinterListen {
            override fun onDefPrinter(printer: PrinterSdk.Printer?) {
                if (printer == null) {
                   // Toast.makeText(requireContext(), "Printer not available", Toast.LENGTH_SHORT).show()
                    return
                }

                val canvasWidth = 384
                var currentY = 10
                val lineHeight = 38

                printer.canvasApi()?.initCanvas(
                    BaseStyle.getStyle()
                        .setWidth(384)
                        .setHeight(1440)
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


                val jsonObject = JSONObject(responseBody)

                val dataObject = jsonObject.optJSONObject("Data")
                Log.d("dataObject", dataObject.toString())
                val loginTime = dataObject.optString("LoginTime", "")
                val logoutTime = dataObject.optString("LogoutTime", "")
                val fullName = dataObject.optString("FullName", "")
                val totalCount = dataObject.optString("TotalCount", "0")
                val totalAmount = dataObject.optString("TotalCash", "0.00") // Note: This is a special character key




                val slip = sessionManager.getSlipHeaderFooter()
                val header1 = JSONObject(slip ?: "{}").optString("Header1")
                val header2 = JSONObject(slip ?: "{}").optString("Header2")
                val footer1 = JSONObject(slip ?: "{}").optString("Footer1")
                val footer2 = JSONObject(slip ?: "{}").optString("Footer2")

                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                    Date()
                )



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

                renderHeaderText("DAILY EXIT SUMMARY", size = 28, bold = true)
                renderCenteredText("AGENT: ${fullName}", size = 22)

                renderLine("---------------------------------")

                renderLine("Login Time: ${loginTime}")
                renderLine("Logout Time: ${logoutTime}")

                renderLine("---------------------------------")

//                renderAmount("TWO WHEELER")
//                currentY += 10
//                renderLine("          COUNT       AMOUNT")
//                renderLine("CASH      ${twoWhCashCount}          Rs. ${twoWhCashAmount}")
//                renderLine("CARD      ${twoWhCardCount}          Rs. ${twoWhCardAmount}")
//
//                renderLine("---------------------------------")
//
//                renderAmount("FOUR WHEELER")
//                currentY += 10
//                renderLine("           COUNT      AMOUNT")
//                renderLine("CASH       ${fourWhCashCount}         Rs. ${fourWhCashAmount}")
//                renderLine("CARD       ${fourWhCardCount}         Rs. ${fourWhCardAmount}")


                val vehicleArray = collectionList
                Log.d("vehicle array" , vehicleArray.toString())

                if (vehicleArray != null) {
                    for (i in 0 until vehicleArray.length()) {

                        val vehicleItem = vehicleArray.getJSONObject(i)
                        Log.d("vehicleItem", vehicleItem.toString())
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






                currentY += 20
                renderLine("=================================")
                renderAmount("TOTAL COUNT   :  ${totalCount}")
                renderAmount("TOTAL AMOUNT  :  Rs. ${totalAmount}")
                renderLine("==================================")


                currentY += 35

                // Footer
                if (footer1.isNotEmpty()) {
                    renderCenteredText(footer1, size = 22, bold = true)
                    currentY += 5
                }

                if (footer2.isNotEmpty()) {
                    renderCenteredText(footer2, size = 20)
                }

                renderLine("---------------------------------")
                renderCenteredText("Printed On: $currentDateTime", size = 22)

                try {
                    // Print
                    printer.canvasApi()?.printCanvas(1, null)

                    // Use the main thread to handle post-print actions
                    requireActivity().runOnUiThread {
                        try {
                            // Mark printing as complete
                            isPrinting = false

                            // Show toast
                           // Toast.makeText(requireContext(), "Report printed successfully", Toast.LENGTH_SHORT).show()

                            // Add a small delay before logout
                            Handler(Looper.getMainLooper()).postDelayed({
                                // Check if fragment is still attached
                                if (isAdded && !isDetached()) {
                                    performLogout()
                                }
                            }, 500)
                        } catch (e: Exception) {
                            Log.e("PrintError", "Error in UI thread after print: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PrintError", "Error during printing: ${e.message}")
                    isPrinting = false
                }


            }

            override fun onPrinters(printers: MutableList<PrinterSdk.Printer>?) {

                //Toast.makeText(requireContext(), "Report printed successfully", Toast.LENGTH_SHORT).show()
            }


        })


    }

}

