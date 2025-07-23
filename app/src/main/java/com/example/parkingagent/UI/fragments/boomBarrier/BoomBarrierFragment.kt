
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
import android.util.Log
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
        Log.d("bluetooth status", (requireActivity() as MainActivity).btManager.isConnected().toString())
        binding.statusTextView.text = if ((requireActivity() as MainActivity).btManager.isConnected()) "Connected - " + (requireActivity() as MainActivity).btManager.getConnectedDeviceName().toString() else "Not Connected"
        // Log.d("connected device name", (requireActivity() as MainActivity).btManager.getConnectedDeviceName().toString())
       //(requireActivity() as MainActivity).btManager.connectToPairedJDYDevice()
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(macAddress: String) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val device = bluetoothAdapter.getRemoteDevice(macAddress)
        (requireActivity() as MainActivity).btManager.connectToDevice(device)
        updateStatus()
        if ((requireActivity() as MainActivity).btManager.isConnected()){
            (requireActivity() as MainActivity)._isBluetoothConnected.postValue(true)
        }
        else{
            (requireActivity() as MainActivity)._isBluetoothConnected.postValue(false)
        }
    }


}
