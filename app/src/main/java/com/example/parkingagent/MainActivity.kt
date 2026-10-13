
package com.example.parkingagent

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.databinding.ActivityMainBinding
import com.example.parkingagent.utils.BluetoothConnectionManager
import com.example.parkingagent.utils.SharedViewModel
import com.example.parkingagent.utils.Utils
import com.lottiefiles.dotlottie.core.util.lifecycleOwner
import com.example.parkingagent.utils.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.net.NetworkRequest
import android.net.Network
import kotlinx.coroutines.*
import android.os.Looper

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import org.json.JSONObject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    public lateinit var binding: ActivityMainBinding
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private lateinit var handlerSec: Handler
    private lateinit var runnableSec: Runnable

    public val sharedViewModel:SharedViewModel by viewModels()

    private val client = OkHttpClient()

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var isBackEnabled = true

    public var isSplashCompleted = false
    public var isQrOutProcessing = false


    private var lastProcessedCarOutKey: String? = null   // notun variable
    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    @Inject
    lateinit var btManager: BluetoothConnectionManager

    public val _isBluetoothConnected = MutableLiveData(false)
    val isBluetoothConnected: LiveData<Boolean> = _isBluetoothConnected

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        onCreatefragmentNavigation()

        setupToolbar()

        // Observe the API response
        observeHeartBeatApi()


        handlerSec = Handler(Looper.getMainLooper())

        runnableSec = object : Runnable {
            override fun run() {


                if (!isSplashCompleted) {
                    handlerSec.postDelayed(this, 3000)
                    return
                }

                if (isQrOutProcessing) {
                    handlerSec.postDelayed(this, 3000)
                    return
                }

                val KioskCarInUrl = sharedPreferenceManager.getKioskParkingInBaseUrl()
                val KioskCarOutUrl = sharedPreferenceManager.getKioskParkingOutBaseUrl()

                if (KioskCarInUrl.isNotEmpty()) {
                    Log.d("KIOSK CAR IN BASE_URL", KioskCarInUrl)

                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

// Current time
                    val endCalendar = Calendar.getInstance()
                    val endTime = formatter.format(endCalendar.time)

// Current time - 30 seconds
                    val startCalendar = Calendar.getInstance()
                    startCalendar.add(Calendar.SECOND, -30)
                    val startTime = formatter.format(startCalendar.time)

                    Log.d("STIME", "StartTime = $startTime")
                    Log.d("ETIME", "EndTime = $endTime")


//                    "startTime": "2026-07-08 00:00:00",
//                    "endTime": "2026-07-08 23:59:59"



                    val json = """
                       {
                        "carNo": "",
                        "pageNum": "1",
                        "pageSize": "100",
                        "startTime":"$startTime",
                        "endTime":"$endTime"
                    }
                    """.trimIndent()

                    Log.d("body",json)

                    val body = json.toRequestBody("application/json".toMediaType())

                    val request = Request.Builder()
                        .url(KioskCarInUrl)
                        .post(body)
                        .build()

                    client.newCall(request).enqueue(object : Callback {

                        override fun onFailure(call: Call, e: IOException) {
                            Log.e("CAR_IN", e.message ?: "Error")
                        }

                        override fun onResponse(call: Call, response: Response) {

                            val result = response.body?.string()

                            Log.d("CAR_IN_RESPONSE", result ?: "No Data")

                            if (response.isSuccessful && result != null) {

                                try {

                                    val root = JSONObject(result)

                                    val status = root.getInt("status")

                                    if (status == 1) {

                                        val data = root.getJSONArray("data")

                                        if (data.length() > 0) {

                                            for (i in 0 until data.length()) {

                                                val item = data.getJSONObject(i)

                                                val carNo = item.optString("carNo")

                                                Log.d("CAR_NO", carNo)
                                                if (item.has("carNo")) {
                                                    insertVehicle(item.optString("carNo"))
                                                } else {
                                                    insertVehicle(item.optString("card"))
                                                }
                                            }
 
                                        }

                                    }

                                } catch (e: Exception) {
                                    Log.e("JSON", e.toString())
                                }
                            }
                        }
                    })
                }

                if (!isQrOutProcessing && KioskCarOutUrl.isNotEmpty()) {

                    Log.d("KIOSK CAR OUT BASE_URL", KioskCarOutUrl)

                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                    // Current Time
                    val endCalendar = Calendar.getInstance()
                    val endTime = formatter.format(endCalendar.time)

                    // Current Time - 10 sec
                    val startCalendar = Calendar.getInstance()
                    startCalendar.add(Calendar.SECOND, -10)
                    val startTime = formatter.format(startCalendar.time)

                    val json = """
                    {
                        "carNo": "",
                        "pageNum": "1",
                        "pageSize": "100",
                        "startTime": "$startTime",
                        "endTime": "$endTime"
                    }
                    """.trimIndent()

                    Log.d("OUT_REQ_BODY", json)

                    val body = json.toRequestBody("application/json".toMediaType())

                    val request = Request.Builder()
                        .url(KioskCarOutUrl)
                        .post(body)
                        .build()

                    client.newCall(request).enqueue(object : Callback {

                        override fun onFailure(call: Call, e: IOException) {
                            Log.e("CAR_OUT", e.message ?: "Error")
                        }

                        override fun onResponse(call: Call, response: Response) {

                            val result = response.body?.string()

                            Log.d("CAR_OUT_RESPONSE", result ?: "No Data")

                            if (!response.isSuccessful || result == null) return

                            try {

                                val root = JSONObject(result)

                                if (root.optInt("status") == 1) {

                                    val data = root.getJSONArray("data")

                                    if (data.length() > 0) {

                                        // First Item = Latest Car
                                        val latestCar = data.getJSONObject(0)
                                        Log.d("LATEST_CAR", latestCar.toString())
                                        Log.d("LATEST_CAR_NO", latestCar.optString("carNo"))
                                        Log.d("LATEST_CAR_TIME", latestCar.optString("time"))

                                        val carNo = latestCar.optString("carNo")
                                        val dateTime = latestCar.optString("time")

                                        runOnUiThread {

                                            val carKey = "$carNo|$dateTime"

                                            if (carKey != lastProcessedCarOutKey) {
                                                runOnUiThread {
                                                    lastProcessedCarOutKey = carKey
                                                    isQrOutProcessing = true
                                                    if (latestCar.has("carNo")) {
                                                        val carNumber = latestCar.optString("carNo")

                                                        val json = JSONObject().apply {
                                                            put("DeviceId", Utils.getDeviceId(this@MainActivity))
                                                            put("VehicleNo", carNumber)
                                                        }

                                                        val body = json.toString().toRequestBody("application/json".toMediaType())

                                                        val request = Request.Builder()
                                                            .url(sharedPreferenceManager.getBaseUrl() + "Device/MonthlySubscriptionCheck")
                                                            .addHeader("Authorization", sharedPreferenceManager.getAccessToken() ?: "")
                                                            .addHeader("Content-Type", "application/json")
                                                            .post(body)
                                                            .build()

                                                        client.newCall(request).enqueue(object : Callback {
                                                            override fun onFailure(call: Call, e: IOException) {
                                                                Log.e("MONTHLY_CHECK_FAIL", e.message ?: "Error")
                                                                runOnUiThread {
                                                                    isQrOutProcessing = false
                                                                    Toast.makeText(this@MainActivity, "Network error: Vehicle check failed", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }

                                                            override fun onResponse(call: Call, response: Response) {
                                                                val result = response.body?.string()
                                                                Log.d("MONTHLY_CHECK_RESPONSE", result ?: "No Data")

                                                                if (response.isSuccessful && result != null) {
                                                                    try {
                                                                        val root = JSONObject(result)
                                                                        val isMonthly = root.optBoolean("IsMonthly", false)
                                                                        val msg = root.optString("msg", "")

                                                                        runOnUiThread {
                                                                            if (isMonthly) {
                                                                                val toastMsg = if (msg.isNotEmpty()) msg else "Monthly Vehicle: $carNumber"
                                                                                Toast.makeText(this@MainActivity, toastMsg, Toast.LENGTH_LONG).show()

                                                                                btManager.controlRelayWithAutoOff(1)

                                                                                Handler(Looper.getMainLooper()).postDelayed({
                                                                                    isQrOutProcessing = false
                                                                                }, 3000)
                                                                            } else {
                                                                                openQrOutPage(carNumber, dateTime)
                                                                            }
                                                                        }
                                                                    } catch (e: Exception) {
                                                                        Log.e("JSON_ERROR", e.toString())
                                                                        runOnUiThread {
                                                                            isQrOutProcessing = false
                                                                            Toast.makeText(this@MainActivity, "Data error: Check failed", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    }
                                                                } else {
                                                                    runOnUiThread {
                                                                        isQrOutProcessing = false
                                                                        Toast.makeText(this@MainActivity, "Server error: Check failed", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                }
                                                            }
                                                        })
                                                    } else {
                                                        openQrOutPage(latestCar.optString("card"), dateTime)
                                                    }
                                                }
                                            } else {
                                                Log.d("KIOSK_DEDUPE", "Same car-out record ($carKey) already processed, skipping re-open")
                                            }
                                        }
                                    }
                                }

                            } catch (e: Exception) {
                                Log.e("JSON_ERROR", e.toString())
                            }
                        }
                    })
                }

                handlerSec.postDelayed(this, 3000)
            }
        }

        // Setup periodic API call
        handler = Handler()
        runnable = object : Runnable {
            override fun run() {
                sharedViewModel.callHeartBeatApi(Utils.getDeviceId(context = this@MainActivity))
                handler.postDelayed(this, 5 * 60 * 1000) // 5 minutes
            }
        }

        btManager.initialize()

        Handler(Looper.getMainLooper()).postDelayed({
           btManager.connectToPairedJDYDevice()

            val tintColor = ContextCompat.getColor(
                this@MainActivity,
                if (btManager.isConnected()) R.color.green else R.color.red
            )
            binding.appBarMain.imgBtStatus.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
        }, 800)

        // Load menu when activity starts
//        sharedViewModel.loadMenu()


//        // Observe menu loading state
//        sharedViewModel.menuLoadingState.observe(this) { state ->
//            when (state) {
//                is SharedViewModel.MenuLoadingState.Loading -> {
//                    binding.loading.visibility = View.VISIBLE
//                }
//                is SharedViewModel.MenuLoadingState.Success -> {
//                    binding.loading.visibility = View.GONE
//                }
//                is SharedViewModel.MenuLoadingState.Error -> {
//                    binding.loading.visibility = View.GONE
//                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
//                }
//            }
//        }

        isBluetoothConnected.observe(this) {
                connected ->
            val tintColor = ContextCompat.getColor(
                this@MainActivity,
                if (connected) R.color.green else R.color.red
            )
            binding.appBarMain.imgBtStatus.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
        }



        NetworkUtils.registerNetworkCallback(this) { isConnected ->
            runOnUiThread {
                try {
                    if (::navController.isInitialized) {
                        val currentDest = navController.currentDestination?.id

                        // Only navigate if we're not already on the correct fragment
                        when {
                            // When network is lost and not already on IP setup
                            !isConnected && currentDest != R.id.id_noInternetFragment -> {
                                Log.d("Network", "No internet - navigating to IP Setup")
                                setBackEnabled(false)
                                navController.navigate(
                                    R.id.action_global_noInternetFragment,
                                    null,
                                    NavOptions.Builder()
                                        .setPopUpTo(R.id.nav_graph, false)
                                        .build()
                                )

                            }

                            // When network is restored and we're on IP setup
                            isConnected && currentDest == R.id.id_noInternetFragment -> {
                                Log.d("Network", "Internet restored - navigating to Menu")
                                Toast.makeText(this, "Internet connection restored.", Toast.LENGTH_SHORT).show()
                                setBackEnabled(true)
                                navController.navigate(
                                    R.id.action_global_menuFragment,
                                    null,
                                    NavOptions.Builder()
                                        .setPopUpTo(R.id.nav_graph, false)
                                        .build()
                                )
                            }

                            // First launch with no network - stay where we are
                            !isConnected && currentDest == R.id.id_menuFragment -> {
                                Log.d("Network", "First launch with no internet - staying on Menu")
                                // Optionally show a warning
                                showNoInternetAlert("No internet connection detected")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Navigation", "Error during network change handling", e)
                }
            }
        }




        isBackEnabled = false

        // For example, re-enable back after 5 seconds (demo)
        Handler(Looper.getMainLooper()).postDelayed({
            isBackEnabled = true
        }, 5000)

    }


    fun openQrOutPage(carNo: String, dateTime: String) {

        runOnUiThread {
            Log.d(
                "MONTHLY_FLOW",
                "4. openQrOutPage called again: carNo=$carNo"
            )
            if (navController.currentDestination?.id != R.id.id_qrOutFragment) {

                val bundle = Bundle().apply {
                    putString("carNo", carNo)
                    putString("dateTime", dateTime)
                }

                navController.navigate(
                    R.id.id_qrOutFragment,
                    bundle,
                    NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .build()
                )
            }
        }
    }


    private fun insertVehicle(vehicleNo: String) {

        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("DeviceId", Utils.getDeviceId(this@MainActivity))
            put("IOType", "0")
            put("VehicleNo", vehicleNo)
            put("VehicleTypeId", "9")
            put("ParkingMode", "MAN")
            put("UserId", sharedPreferenceManager.getUserId())
            put("BookingNumber", "")
        }

        val body = json.toString()
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(sharedPreferenceManager.getBaseUrl()+"Device/VehicleParking")
            .addHeader("Authorization", sharedPreferenceManager.getAccessToken() ?: "")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("INSERT", e.message ?: "")
            }

            override fun onResponse(call: Call, response: Response) {

                Log.d(
                    "INSERT_RESPONSE",
                    response.body?.string() ?: ""
                )

            }

        })
    }


    override fun onBackPressed() {
        if (isBackEnabled) {
            super.onBackPressed()
        } else {
            Toast.makeText(this, "Back button is disabled right now", Toast.LENGTH_SHORT).show()
        }
    }

    // Optional: method to set back press flag from fragment
    fun setBackEnabled(enabled: Boolean) {
        isBackEnabled = enabled
    }


    private fun showNoInternetAlert(msg:String) {
        AlertDialog.Builder(this)
            .setTitle("Alert")
            .setMessage(msg)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }


    private fun setupToolbar() {
        // Access views through the appBarMain reference
        val toolbar = binding.appBarMain.toolbar
//        val btnBack = toolbar.findViewById<ImageButton>(R.id.btnBack)
//        val btnLogout = toolbar.findViewById<ImageButton>(R.id.btnLogout)
//        val toolbarTitle = toolbar.findViewById<TextView>(R.id.toolbarTitle)

        binding.appBarMain.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.appBarMain.imgBtStatus.setOnClickListener {
            // Your logout logic
            navController.navigate(R.id.id_boomBarrierFragment)
        }

        // Update toolbar title based on navigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.appBarMain.toolbarTitle.text = when (destination.id) {
                R.id.id_homeFragment -> "QR In"
                R.id.id_qrOutFragment -> "QR Out"
                R.id.id_menuFragment -> "Menu"
                R.id.id_cardSettingsFragment -> "Card Setting"
                R.id.id_boomBarrierFragment -> "Boom Barrier"
                R.id.id_ipSetupFragment -> "IP & Port"
                R.id.id_loginFragment -> "Login"
                R.id.splashFragment -> "Splash"
                R.id.id_prkingMenuFragment -> "Parking"
                R.id.id_fragment_card_in -> "Card In"
                R.id.id_fragment_card_out -> "Card Out"
                R.id.id_fragment_nfc -> "Check Card"
                R.id.id_fragment_nfc_write -> "Issue Card"
                R.id.id_parkingRateFragment->"Parking Rate"
                R.id.id_reportsFragment->"Reports"

                // Add other fragments
                else -> ""
            }

            // Show/hide back button
            binding.appBarMain.btnBack.visibility = if (destination.id == navController.graph.startDestinationId) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(runnable) // Start periodic API call
        handlerSec.post(runnableSec)

    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable) // Stop periodic API call
        handlerSec.removeCallbacks(runnableSec)
    }

    override fun onDestroy() {
        super.onDestroy()
        btManager.closeConnection()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }


    object NetworkUtils {

        fun registerNetworkCallback(context: Context, onChanged: (Boolean) -> Unit) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    onChanged(true)
                }

                override fun onLost(network: android.net.Network) {
                    onChanged(false)
                }
            })
        }

        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }

    private fun observeHeartBeatApi() {


        sharedViewModel.heartBeatResponse.observe(this) { result ->
            result.onSuccess { response ->
                // Handle successful response
                Log.d("HeartBeat", "Response: ${response.status}")
            }.onFailure { throwable ->
                // Handle error
                Toast.makeText(this,throwable.message,Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onCreatefragmentNavigation(){
        // ======== Connecting Nav graph to Fragment
//        navController = findNavController(R.id.navHostFragment)
        val navHostFrag = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFrag.navController

        // ==== Connection Bottom Navigation to nav Graph ========
//        binding.bottomNavigation.setupWithNavController(navController)

        // ==== Navigate to cart fragment ========
//        binding.cart.setOnClickListener {
//            navController.navigate(R.id.cartFragment)
//        }
//
//
//        // ==== Navigate to cart fragment ========
//        binding.notification.setOnClickListener {
//            navController.navigate(R.id.notificationFragment)
//        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.id_loginFragment,R.id.splashFragment,R.id.id_menuFragment,R.id.id_noInternetFragment
                    -> {
                    binding.appBarMain.toolbar.visibility= View.GONE
                }

                else -> {
                    binding.appBarMain.toolbar.visibility = View.VISIBLE

                }
            }
        }
    }

    private fun navigateToLogin() {
        // Clear session data
        sharedPreferenceManager.clearToken()
        sharedPreferenceManager.setLoginStatus(false)

        // Create navigation options to clear back stack
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true) // Use your nav graph resource id here
            .build()

        // Navigate to login with clear back stack
        navController.navigate(R.id.id_loginFragment, null, navOptions)
    }

    private fun showLogoutConfirmation(onConfirm: () -> Unit) {
        AlertDialog.Builder(this@MainActivity)
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


}
