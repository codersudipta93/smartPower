package com.example.parkingagent.UI.fragments.parkingRate

import androidx.fragment.app.viewModels
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.parkingagent.R
import com.example.parkingagent.UI.adapters.ParkingRateAdapter
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.databinding.FragmentParkingRateBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ParkingRateFragment : BaseFragment<FragmentParkingRateBinding>() {

    private val viewModel: ParkingRateViewModel by viewModels()

    private lateinit var adapter: ParkingRateAdapter

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_parking_rate
    }

    override fun initView() {
        super.initView()

        // 1) Initialize the adapter with an empty list
        adapter = ParkingRateAdapter(emptyList())
        binding.rvRates.adapter = adapter

        // 2) Trigger the API call
        viewModel.getParkingRate()

    }

    override fun observe() {
        super.observe()

        // 3) Collect the SharedFlow for results
        lifecycleScope.launch {
            viewModel.mutualSharedflow.collect { event ->
                when (event) {
                    is ParkingRateViewModel.GetParkingRateEvents.GetParkingRateSuccessful -> {
                        // Pass the raw data items directly to the adapter
                        val data = event.parkingRateDataItem
                            ?.filterNotNull()
                            ?: emptyList()
                        adapter.updateData(data)
                    }
                    is ParkingRateViewModel.GetParkingRateEvents.GetParkingRateFailed -> {
                        // Show an error message
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }


}