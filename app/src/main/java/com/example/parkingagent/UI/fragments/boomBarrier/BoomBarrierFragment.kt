package com.example.parkingagent.UI.fragments.boomBarrier

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.parkingagent.MainActivity
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.databinding.FragmentBoomBarrierBinding
import com.example.parkingagent.utils.BluetoothConnectionManager
import javax.inject.Inject

class BoomBarrierFragment : BaseFragment<FragmentBoomBarrierBinding>() {

    private val deviceList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_boom_barrier
    }

    override fun initView() {
        super.initView()

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, deviceList)
        binding.deviceListView.adapter = adapter
        updateStatus()

        (requireActivity() as MainActivity).btManager.setDeviceDiscoveredListener(object : BluetoothConnectionManager.DeviceDiscoveredListener {
            @SuppressLint("MissingPermission")
            override fun onDeviceDiscovered(device: BluetoothDevice) {
                val deviceInfo = "${device.name}\n${device.address}"
                activity?.runOnUiThread {
                    if (!deviceList.contains(deviceInfo)) {
                        deviceList.add(deviceInfo)
                        adapter.notifyDataSetChanged()
                    }
                }
            }

        })

        binding.scanButton.setOnClickListener {
            (requireActivity() as MainActivity).btManager.initialize()
        }

        binding.deviceListView.setOnItemClickListener { _, _, position, _ ->
            val macAddress = deviceList[position].split("\n")[1]
            connectToDevice(macAddress)
        }

    }

    private fun updateStatus() {
        binding.statusTextView.text = if ((requireActivity() as MainActivity).btManager.isConnected()) "Connected" else "Not Connected"
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(macAddress: String) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val device = bluetoothAdapter.getRemoteDevice(macAddress)
        (requireActivity() as MainActivity).btManager.connectToDevice(device)
        updateStatus()
    }


}