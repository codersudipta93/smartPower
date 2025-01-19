package com.example.parkingagent.UI.fragments.deviceActivation

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.parkingagent.R

class DeviceActivationFragment : Fragment() {

    companion object {
        fun newInstance() = DeviceActivationFragment()
    }

    private val viewModel: DeviceActivationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_device_activation, container, false)
    }
}