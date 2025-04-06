package com.example.parkingagent

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.databinding.ActivityMainBinding
import com.example.parkingagent.utils.BluetoothConnectionManager
import com.example.parkingagent.utils.SharedViewModel
import com.example.parkingagent.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    public lateinit var binding: ActivityMainBinding
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    public val sharedViewModel:SharedViewModel by viewModels()

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    @Inject
    lateinit var btManager: BluetoothConnectionManager

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

        // Load menu when activity starts
        sharedViewModel.loadMenu()


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

            binding.appBarMain.btnLogout.setOnClickListener {
            // Your logout logic
            navigateToLogin()
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
                R.id.id_loginFragment,R.id.splashFragment,R.id.id_menuFragment
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


}