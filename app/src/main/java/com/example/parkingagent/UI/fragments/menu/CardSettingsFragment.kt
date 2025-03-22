package com.example.parkingagent.UI.fragments.menu

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parkingagent.R
import com.example.parkingagent.UI.adapters.MenuAdapter
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.data.remote.models.Menu.MenuDataItem
import com.example.parkingagent.databinding.FragmentCardSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CardSettingsFragment : BaseFragment<FragmentCardSettingsBinding>() {

    override fun getLayoutResourceId(): Int = R.layout.fragment_card_settings

    override fun initView() {
        super.initView()

        // Menu list for card settings fragment (Card In and Card Out)
        // Menu list for card settings fragment with icon URLs.
        val menuList = listOf(
            MenuDataItem(
                menuName = "Card In",
                icon = "http://45.249.111.51/SmartPowerAPI/Images/CardIn.png"
            ),
            MenuDataItem(
                menuName = "Card Out",
                icon = "http://45.249.111.51/SmartPowerAPI/Images/CardOut.png"
            ),
            MenuDataItem(
                menuName = "Issue Card",
                icon = "http://45.249.111.51/SmartPowerAPI/Images/IssueCard.png"
            ),
            MenuDataItem(
                menuName = "Check Card",
                icon = "http://45.249.111.51/SmartPowerAPI/Images/CheckCard.png"
            ),
            MenuDataItem(
                menuName = "Surrender Card",
                icon = "http://45.249.111.51/SmartPowerAPI/Images/SurrenderCard.png"
            )
        )

        val adapter = MenuAdapter(menuList) { menuItem ->
            when (menuItem.menuName) {
                "Card In" -> findNavController().navigate(R.id.id_fragment_card_in)
                "Card Out" -> findNavController().navigate(R.id.id_fragment_card_out)
                "Issue Card"->findNavController().navigate(R.id.id_fragment_nfc_write)
                "Check Card"->findNavController().navigate(R.id.id_fragment_nfc)
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
