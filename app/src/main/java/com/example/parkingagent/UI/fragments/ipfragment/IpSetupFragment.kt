package com.example.parkingagent.UI.fragments.ipfragment

import android.util.Log
import android.widget.Toast
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.databinding.FragmentIpSetupBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.widget.ArrayAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.graphics.Color

@AndroidEntryPoint
class IpSetupFragment : BaseFragment<FragmentIpSetupBinding>() {

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    override fun getLayoutResourceId(): Int = R.layout.fragment_ip_setup

    override fun initView() {
        Log.d("IP Address for page","")
        super.initView()
        Log.d("IP Address for page", sharedPreferenceManager.getIpAddress().toString())
        binding.edtIpSetup.setText(sharedPreferenceManager.getIpAddress() ?: "")
        binding.edtPort.setText(sharedPreferenceManager.getPort()?.toString() ?: "")

        // ✅ Setup Spinner (ANPR for Out)
        val options = resources.getStringArray(R.array.anpr_out_options)

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            options
        ) {
            override fun isEnabled(position: Int): Boolean {
                // প্রথম অপশন ("Select") disable
                return position != 0
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                if (position == 0) {
                    view.setTextColor(Color.GRAY)  // placeholder gray
                } else {
                    view.setTextColor(Color.BLACK) // normal option black
                }
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAnprOut.adapter = adapter

// By default "Select" দেখাবে
        binding.spinnerAnprOut.setSelection(0)


        val savedAnprOut = sharedPreferenceManager.getANPRForOut()
        if (!savedAnprOut.isNullOrEmpty()) {
            val position = options.indexOf(savedAnprOut)
            if (position >= 0) {
                binding.spinnerAnprOut.setSelection(position)
            }
        }

        binding.btnSave.setOnClickListener {
            val ip = binding.edtIpSetup.text.toString().trim()
            val port = binding.edtPort.text.toString().trim()

            val anprOut = binding.spinnerAnprOut.selectedItem.toString()


            if (ip.isEmpty()) {
                binding.edtIpSetup.error = "Please enter IP address"
                return@setOnClickListener
            }
            if (port.isEmpty()) {
                binding.edtPort.error = "Please enter Port number"
                return@setOnClickListener
            }

            if (anprOut == "Select") {
                Toast.makeText(requireContext(), "Please select ANPR option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("options",anprOut)
            // Save the IP address and Port using SharedPreferenceManager
            sharedPreferenceManager.saveIpAddress(ip)
            sharedPreferenceManager.savePort(port)
            sharedPreferenceManager.saveANPRForOut(anprOut)
            binding.edtIpSetup.setText(sharedPreferenceManager.getIpAddress().toString())
            binding.edtPort.setText(sharedPreferenceManager.getPort().toString())
            val position = options.indexOf(anprOut)
            if (position >= 0) {
                binding.spinnerAnprOut.setSelection(position)
            }
            Toast.makeText(requireContext(), "IP and Port saved successfully", Toast.LENGTH_SHORT).show()
        }
    }
}
