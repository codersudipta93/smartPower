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
import com.example.parkingagent.UI.fragments.home.HomeViewModel
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.data.remote.models.Menu.MenuDataItem
import com.example.parkingagent.databinding.FragmentMenuBinding
import com.example.parkingagent.utils.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MenuFragment : BaseFragment<FragmentMenuBinding>() {

    private val sharedViewModel: SharedViewModel by activityViewModels()

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    private lateinit var adapter: MenuAdapter


    override fun getLayoutResourceId(): Int {
       return R.layout.fragment_menu
    }

    override fun initView() {
        super.initView()

        setupRecyclerView()
        setupObservers()

        binding.btnLogout.setOnClickListener {

            showLogoutConfirmation {
                performLogout()
            }

        }

        binding.idTxtFullname.text=sharedPreferenceManager.getFullName()

        binding.idTxtLocation.text=sharedPreferenceManager.getLocation()

        (requireActivity() as MainActivity).isBluetoothConnected
            .observe(viewLifecycleOwner) { connected ->
                val tintColor = ContextCompat.getColor(
                    requireContext(),
                    if (connected) R.color.green else R.color.red
                )
                binding.imgBtStatus.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
            }

        binding.imgBtStatus.setOnClickListener {
            navigate(R.id.id_boomBarrierFragment)
        }

        (requireActivity() as MainActivity).binding.loading.visibility= View.VISIBLE
        sharedViewModel.loadMenu()

    }

    private fun setupRecyclerView() {
        adapter = MenuAdapter(emptyList()) { menuItem ->
            navigateToDestination(menuItem.appMenuId)
        }
        binding.recyclerViewMenu.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@MenuFragment.adapter
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
            1 -> navigate(R.id.id_prkingMenuFragment)
            2 -> navigate(R.id.id_cardSettingsFragment)
//            3 -> navigate(R.id.id_reportsFragment)
            5 -> navigate(R.id.id_fragment_card_in)
            6 -> navigate(R.id.id_fragment_card_out)
            7 -> navigate(R.id.id_homeFragment)
            8 -> navigate(R.id.id_qrOutFragment)
            9 -> navigate(R.id.id_fragment_nfc_write)
            10 -> navigate(R.id.id_fragment_nfc)
//            11 -> navigate(R.id.id_surrenderCardFragment)
            25 -> navigate(R.id.id_parkingRateFragment)
            // Add other mappings as needed
            else -> showInvalidMenuError()
        }
    }

    private fun navigate(destinationId: Int) {
        findNavController().navigate(destinationId)
    }

    private fun showInvalidMenuError() {
        Toast.makeText(requireContext(), "Invalid menu option", Toast.LENGTH_SHORT).show()
    }

    private fun performLogout() {
        sharedPreferenceManager.clearToken()
        sharedPreferenceManager.setLoginStatus(false)

        // Create navigation options to clear back stack
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true) // Use your nav graph resource id here
            .build()

        // Navigate to login with clear back stack
        findNavController().navigate(R.id.id_loginFragment, null, navOptions)

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
}

