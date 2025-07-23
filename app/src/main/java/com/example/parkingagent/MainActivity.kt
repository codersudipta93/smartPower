
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


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    public lateinit var binding: ActivityMainBinding
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    public val sharedViewModel:SharedViewModel by viewModels()

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var isBackEnabled = true
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

    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable) // Stop periodic API call
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
