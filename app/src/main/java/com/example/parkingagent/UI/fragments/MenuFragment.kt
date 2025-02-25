package com.example.parkingagent.UI.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parkingagent.R
import com.example.parkingagent.UI.adapters.MenuAdapter
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.UI.fragments.home.HomeViewModel
import com.example.parkingagent.databinding.FragmentMenuBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MenuFragment : BaseFragment<FragmentMenuBinding>() {

    private val viewModel: MenuViewModel by viewModels()

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_menu
    }

    override fun initView() {
        super.initView()
        viewModel.getMenu()

        val menuList = listOf(
            "Parking",
            "Card Setting",
            "Reports",
            "Card In",
            "Card Out",
            "QR In",
            "QR Out",
            "Issue Card",
            "Check Card",
            "Surrender Card",
            "Boom Barrier"
        )

        val adapter = MenuAdapter(menuList) { menuItem ->
            when (menuItem) {
                "Parking" -> findNavController().navigate(R.id.id_homeFragment)
                "Card Setting" -> findNavController().navigate(R.id.id_fragment_profile)
//                "Reports" -> findNavController().navigate(R.id.id_fragment_more)
                "Card In" -> findNavController().navigate(R.id.id_fragment_card_in)
                "Card Out" -> findNavController().navigate(R.id.id_fragment_card_out)
                "QR In" -> findNavController().navigate(R.id.id_homeFragment)
                "QR Out" -> findNavController().navigate(R.id.id_qrOutFragment)
                "Issue Card"->findNavController().navigate(R.id.id_fragment_nfc_write)
                "Check Card"->findNavController().navigate(R.id.id_fragment_nfc)
                // Add navigation for other menu items as needed
            }
        }

        binding.recyclerViewMenu.adapter = adapter
        binding.recyclerViewMenu.layoutManager = LinearLayoutManager(requireContext())
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