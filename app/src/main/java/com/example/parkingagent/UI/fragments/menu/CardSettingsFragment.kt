package com.example.parkingagent.UI.fragments.menu

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parkingagent.R
import com.example.parkingagent.UI.adapters.MenuAdapter
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.UI.fragments.MenuFragment
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.data.remote.models.Menu.MenuDataItem
import com.example.parkingagent.databinding.FragmentCardSettingsBinding
import com.example.parkingagent.utils.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class CardSettingsFragment : BaseFragment<FragmentCardSettingsBinding>() {

    override fun getLayoutResourceId(): Int = R.layout.fragment_card_settings

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var adapter: MenuAdapter

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    override fun initView() {
        super.initView()
        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = MenuAdapter(emptyList()) { menuItem ->
            navigateToDestination(menuItem.appMenuId)
        }
        binding.recyclerViewMenu.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@CardSettingsFragment.adapter
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
            val parentItems = allItems.filter { it.parentId == 1 }
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
//            25 -> navigate(R.id.id_parkingRateFragment)
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

    override fun observe() {
        super.observe()
        // Observe any flows or LiveData if needed
    }
}
