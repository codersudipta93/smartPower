package com.example.parkingagent.UI.fragments.menu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.parkingagent.R

// TODO: Rename parameter arguments, choose names that match

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parkingagent.UI.adapters.MenuAdapter
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.data.remote.models.Menu.MenuDataItem
import com.example.parkingagent.databinding.FragmentParkingMenuBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ParkingMenuFragment : BaseFragment<FragmentParkingMenuBinding>() {

    override fun getLayoutResourceId(): Int = R.layout.fragment_parking_menu

    override fun initView() {
        super.initView()

        // Menu list for parking fragment (QR In and QR Out)
        // Menu list for parking fragment (QR In and QR Out) with icon URLs.
        val menuList = listOf(
            MenuDataItem(
                menuName = "QR In",
                icon = "http://45.249.111.51/SmartPowerAPI/Images/QRin.png"
            ),
            MenuDataItem(
                menuName = "QR Out",
                icon = "http://45.249.111.51/SmartPowerAPI/Images/QRout.png"
            )
        )

        val adapter = MenuAdapter(menuList) { menuItem ->
            when (menuItem.menuName) {
                "QR In" -> findNavController().navigate(R.id.id_homeFragment) // adjust navigation action as needed
                "QR Out" -> findNavController().navigate(R.id.id_qrOutFragment)
            }
        }

        binding.recyclerViewMenu.adapter = adapter
        binding.recyclerViewMenu.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun observe() {
        super.observe()
        // Observe any flows or LiveData if needed
    }
}
