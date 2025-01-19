package com.example.parkingagent

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.parkingagent.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onCreatefragmentNavigation()

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
                R.id.id_loginFragment,R.id.splashFragment
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