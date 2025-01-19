package com.example.parkingagent.UI.fragments.ParkedVehicle

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parkingagent.R
import com.example.parkingagent.UI.adapters.ParkedVehicleAdapter
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.databinding.FragmentParkedVehicleBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ParkedVehicleFragment : BaseFragment<FragmentParkedVehicleBinding>() {

    private val viewModel: ParkedVehicleViewModel by viewModels()
    private val parkedVehicleAdapter = ParkedVehicleAdapter()

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_parked_vehicle
    }

    override fun initView() {
        super.initView()

        // Initialize RecyclerView
        binding.idParkedRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = parkedVehicleAdapter
        }

        // Observe PagingData from ViewModel
        observeVehicleList()
    }

    private fun observeVehicleList() {
        lifecycleScope.launch {
            viewModel.vehicleListFlow.collectLatest { pagingData ->

                parkedVehicleAdapter.submitData(pagingData)
                Log.d("PagingData", pagingData.toString())
            }
        }
    }
}
