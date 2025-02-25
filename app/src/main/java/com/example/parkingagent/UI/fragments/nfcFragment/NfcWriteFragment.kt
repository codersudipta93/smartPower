package com.example.parkingagent.UI.fragments.nfcFragment

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.parkingagent.MainActivity
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.databinding.FragmentNfcWriteBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject

@AndroidEntryPoint
class NfcWriteFragment : BaseFragment<FragmentNfcWriteBinding>() {

    private val viewModel: NfcViewModel by viewModels()
    private var nfcAdapter: NfcAdapter? = null
//    private var nfcTag: Tag? = null
    private var jsonData:String?=null
    private var selectedVehicleTypeId: String? = null


    private fun setupVehicleTypeDropdown() {
        val vehicleTypes = listOf("Two-Wheeler", "Four-Wheeler")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, vehicleTypes)
        binding.vehicleTypeDropdown.setAdapter(adapter)

        binding.vehicleTypeDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedVehicleTypeId = if (position == 0) "2" else "1"
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_nfc_write
    }

    override fun onResume() {
        super.onResume()
        enableReaderMode()
    }

    override fun onPause() {
        super.onPause()
        disableReaderMode()
    }

    private fun disableReaderMode() {
        nfcAdapter?.disableReaderMode(requireActivity())
    }

    override fun initView() {
        super.initView()
        setupVehicleTypeDropdown()
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        if (nfcAdapter == null) {
            Toast.makeText(context, "NFC is not supported on this device.", Toast.LENGTH_LONG).show()
            return
        }

        binding.btnGuestRegister.setOnClickListener {
            registerGuest()
        }
    }

    private fun registerGuest() {
        if (binding.edtName.text.isNullOrBlank()) {
            binding.edtName.error = "Please enter name"
            return
        }
        if (binding.edtContact.text.isNullOrBlank() || binding.edtContact.text!!.length < 10) {
            binding.edtContact.error = "Please enter a valid contact number"
            return
        }
        if (binding.edtVehicleNo.text.isNullOrBlank()) {
            binding.edtVehicleNo.error = "Please enter vehicle number"
            return
        }
        if (binding.edtAmount.text.isNullOrBlank()) {
            binding.edtAmount.error = "Please enter amount"
            return
        }

        if (binding.vehicleTypeDropdown.text.isNullOrBlank()) {
            binding.vehicleTypeDropdown.error = "Please select Vehicle type"
            return
        }

        jsonData = JSONObject().apply {
            put("name", binding.edtName.text.toString())
            put("contact", binding.edtContact.text.toString())
            put("vehicle_no", binding.edtVehicleNo.text.toString())
            put("amount", binding.edtAmount.text.toString().toDouble())
        }.toString()

        (requireActivity() as MainActivity).binding.loading.visibility = View.VISIBLE
        viewModel.registerGuest(
            binding.edtName.text.toString(),
            binding.edtContact.text.toString(),
            binding.edtVehicleNo.text.toString(),
            binding.edtAmount.text.toString().toDouble(),
            "04824C42646B80",
            selectedVehicleTypeId.toString().toInt()
        )

        // Attempt to write data to NFC tag
//        nfcTag?.let { tag ->
//            writeJsonToTag(tag, jsonData)
//        } ?: run {
//            Toast.makeText(context, "No NFC tag detected. Please tap an NFC tag.", Toast.LENGTH_LONG).show()
//        }
    }

    private fun enableReaderMode() {
        nfcAdapter?.enableReaderMode(
            requireActivity(),
            { tag -> nfcReaderCallback(tag) },
            // Here using FLAG_READER_NFC_A (and no platform sounds) for compatibility.
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            null
        )
    }

    private fun nfcReaderCallback(tag: Tag) {
        if (jsonData!=null) {
            writeJsonToTag(tag, jsonData!!)
        } else {
            Toast.makeText(requireContext(),"No data to write",Toast.LENGTH_LONG).show()
        }
    }

//    private fun writeJsonToTag(tag: Tag, jsonData: String) {
//        try {
//            val ndef = Ndef.get(tag)
//            if (ndef == null) {
//                Toast.makeText(context, "Tag doesn't support NDEF.", Toast.LENGTH_LONG).show()
//                return
//            }
//            ndef.connect()
//            if (!ndef.isWritable) {
//                Toast.makeText(context, "Tag is not writable.", Toast.LENGTH_LONG).show()
//                ndef.close()
//                return
//            }
//
//            val mimeRecord = NdefRecord.createMime("application/json", jsonData.toByteArray(Charsets.UTF_8))
//            val ndefMessage = NdefMessage(arrayOf(mimeRecord))
//            ndef.writeNdefMessage(ndefMessage)
//            ndef.close()
//
//            Toast.makeText(context, "Successfully wrote data to tag.", Toast.LENGTH_LONG).show()
//        } catch (e: Exception) {
//            Log.d("errorhappen",e.message.toString())
//            Toast.makeText(context, "Failed to write NFC tag: ${e.message}", Toast.LENGTH_LONG).show()
//        }
//    }

    private fun writeJsonToTag(tag: Tag, jsonData: String) {
        try {
            val ndef = Ndef.get(tag)
            if (ndef == null) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Tag doesn't support NDEF.", Toast.LENGTH_LONG).show()
                }
                return
            }
            ndef.connect()
            if (!ndef.isWritable) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Tag is not writable.", Toast.LENGTH_LONG).show()
                }
                ndef.close()
                return
            }

            val mimeRecord = NdefRecord.createMime("application/json", jsonData.toByteArray(Charsets.UTF_8))
            val ndefMessage = NdefMessage(arrayOf(mimeRecord))
            ndef.writeNdefMessage(ndefMessage)
            ndef.close()

            activity?.runOnUiThread {
                Toast.makeText(context, "Successfully wrote data to tag.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.d("errorhappen", e.message.toString())
            activity?.runOnUiThread {
                Toast.makeText(context, "Failed to write NFC tag: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun observe() {
        super.observe()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mutualSharedflow.collectLatest {
                    (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
                    when (it) {
                        is NfcViewModel.GuestEvents.GuestRegistered -> {
                            binding.edtName.text?.clear()
                            binding.edtContact.text?.clear()
                            binding.edtVehicleNo.text?.clear()
                            binding.edtAmount.text?.clear()
                            Toast.makeText(context, "Guest Registered Successfully", Toast.LENGTH_LONG).show()
                        }
                        is NfcViewModel.GuestEvents.Error -> {
                            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}
