package com.example.parkingagent.UI.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parkingagent.R
import com.example.parkingagent.UI.adapters.MenuAdapter
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.UI.fragments.home.HomeViewModel
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.data.remote.models.Menu.MenuDataItem
import com.example.parkingagent.databinding.FragmentMenuBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MenuFragment : BaseFragment<FragmentMenuBinding>() {

    private val viewModel: MenuViewModel by viewModels()

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_menu
    }

    override fun initView() {
        super.initView()

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })

        viewModel.getMenu()

//        val menuList = listOf(
//            "Parking",
//            "Card Setting",
//            "Reports",
////            "Card In",
////            "Card Out",
////            "QR In",
////            "QR Out",
////            "Issue Card",
////            "Check Card",
////            "Surrender Card",
//            "Boom Barrier"
//        )

        val menuList=listOf(
            MenuDataItem(menuName = "Parking", icon = "http://45.249.111.51/SmartPowerAPI/Images/parking.png")
            ,MenuDataItem(menuName = "Card Setting", icon = "http://45.249.111.51/SmartPowerAPI/Images/CardSetting.png")
            ,MenuDataItem(menuName = "Reports", icon = "http://45.249.111.51/SmartPowerAPI/Images/reports.png")
            ,MenuDataItem(menuName = "Boom Barrier", icon = "http://45.249.111.51/SmartPowerAPI/Images/BoomBerriar.png")
        )

        val adapter = MenuAdapter(menuList) { menuItem ->
            when (menuItem.menuName) {
                "Parking" -> findNavController().navigate(R.id.id_prkingMenuFragment)
                "Card Setting" -> findNavController().navigate(R.id.id_cardSettingsFragment)
//                "Reports" -> findNavController().navigate(R.id.id_fragment_more)
//                "Card In" -> findNavController().navigate(R.id.id_fragment_card_in)
//                "Card Out" -> findNavController().navigate(R.id.id_fragment_card_out)
//                "QR In" -> findNavController().navigate(R.id.id_homeFragment)
//                "QR Out" -> findNavController().navigate(R.id.id_qrOutFragment)
//                "Issue Card"->findNavController().navigate(R.id.id_fragment_nfc_write)
//                "Check Card"->findNavController().navigate(R.id.id_fragment_nfc)
                "Boom Barrier"->findNavController().navigate(R.id.id_boomBarrierFragment)
                // Add navigation for other menu items as needed
            }
        }

        binding.recyclerViewMenu.adapter = adapter
        binding.recyclerViewMenu.layoutManager = GridLayoutManager(requireContext(),2)


        // Setup Logout button click listener
        binding.btnLogout.setOnClickListener {
            // Clear token and update login status
            sharedPreferenceManager.clearToken()
            sharedPreferenceManager.setLoginStatus(false)

            // Navigate to LoginFragment and clear the backstack
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.id_menuFragment, true)
                .build()
            findNavController().navigate(R.id.action_menuFragment_to_loginFragment, null, navOptions)
        }
    }


    override fun observe() {
        super.observe()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mutualSharedflow.collectLatest { event ->
                    when (event) {
                        is MenuViewModel.GetMenuEvents.GetMenuFailed -> {
                            Log.d("MenuFragment", "GetMenuFailed: ${event.message}")
                        }
                        is MenuViewModel.GetMenuEvents.GetMenuSuccessful -> {
                            Log.d("MenuFragment", "GetMenuSuccessful: ${event.menuResponse}")
                        }
                    }
                }
            }
        }
    }

}

