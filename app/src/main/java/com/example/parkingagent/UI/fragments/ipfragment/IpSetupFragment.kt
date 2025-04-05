package com.example.parkingagent.UI.fragments.ipfragment

import android.widget.Toast
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.databinding.FragmentIpSetupBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class IpSetupFragment : BaseFragment<FragmentIpSetupBinding>() {

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    override fun getLayoutResourceId(): Int = R.layout.fragment_ip_setup

    override fun initView() {
        super.initView()
        binding.btnSave.setOnClickListener {
            val ip = binding.edtIpSetup.text.toString().trim()
            val port = binding.edtPort.text.toString().trim()

            if (ip.isEmpty()) {
                binding.edtIpSetup.error = "Please enter IP address"
                return@setOnClickListener
            }
            if (port.isEmpty()) {
                binding.edtPort.error = "Please enter Port number"
                return@setOnClickListener
            }

            // Save the IP address and Port using SharedPreferenceManager
            sharedPreferenceManager.saveIpAddress(ip)
            sharedPreferenceManager.savePort(port)
            Toast.makeText(requireContext(), "IP and Port saved successfully", Toast.LENGTH_SHORT).show()
        }
    }
}
