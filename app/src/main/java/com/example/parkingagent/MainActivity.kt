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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
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
    lateinit var btManager: BluetoothConnectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)
        onCreatefragmentNavigation()

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
        binding.bottomNavigation.setupWithNavController(navController)

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
                R.id.id_loginFragment,R.id.splashFragment,R.id.id_menuFragment,R.id.id_fragment_nfc,R.id.id_fragment_nfc_write,R.id.id_qrOutFragment,R.id.id_fragment_card_in,R.id.id_fragment_card_out,R.id.id_boomBarrierFragment,R.id.id_homeFragment,R.id.id_prkingMenuFragment,R.id.id_cardSettingsFragment
                    -> {
                    binding.appBarMain.toolbar.visibility= View.GONE
                    binding.bottomNavigation.visibility= View.GONE
                }

                else -> {
                    binding.appBarMain.toolbar.visibility = View.VISIBLE
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
            }
        }
    }


}