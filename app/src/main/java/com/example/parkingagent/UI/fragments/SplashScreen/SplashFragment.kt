package com.example.parkingagent.UI.fragments.SplashScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkingagent.R
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.net.NetworkRequest
import android.net.Network
import com.example.parkingagent.utils.NetworkUtils
@AndroidEntryPoint
class SplashFragment : Fragment() {

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    private val viewModel: SplashViewModel by viewModels()
    private lateinit var binding: FragmentSplashBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.icon_animation)
        binding.iconImageView.animation = animation

        lifecycleScope.launch {
            delay(1500)
            // Check if token exists in SharedPreferenceManager
            val isConnected = NetworkUtils.isNetworkAvailable(requireContext())



            val token = sharedPreferenceManager.getAccessToken()
            if (!token.isNullOrBlank() && sharedPreferenceManager.getEntityId()!=0) {
                // Token exists: navigate directly to the menu screen

                if(isConnected){
                    findNavController().navigate(R.id.action_splashFragment_to_menuFragment)
                }else{
                    findNavController().navigate(R.id.action_global_noInternetFragment)
                }

            } else {
                // No token: navigate to the login screen
                findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
            }
        }
    }
}
