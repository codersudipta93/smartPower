package com.example.parkingagent.UI.fragments.nfcFragment

import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.databinding.FragmentNfcReadBinding
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONException
import org.json.JSONObject

@AndroidEntryPoint
class NfcReadFragment : BaseFragment<FragmentNfcReadBinding>(){

    // Native NFC adapter
    private var nfcAdapter: NfcAdapter? = null

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_nfc_read
    }

    override fun initView() {
        super.initView()
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        if (nfcAdapter == null) {
            Log.e("NFC", "This device doesn't support NFC.")
            Toast.makeText(requireContext(), "This device doesn't support NFC.", Toast.LENGTH_SHORT).show()
        }

        binding.idBtnNfcWrite.setOnClickListener {
            findNavController().navigate(R.id.action_id_fragment_nfc_to_id_fragment_nfc_write)
        }

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

    private fun enableReaderMode() {
        nfcAdapter?.enableReaderMode(
            requireActivity(),
            { tag -> readJsonFromTag(tag) },
            // Here using FLAG_READER_NFC_A (and no platform sounds) for compatibility.
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            null
        )
    }

    private fun readJsonFromTag(tag: Tag) {
        try {
            val ndef = Ndef.get(tag)
            if (ndef == null) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Tag doesn't support NDEF.", Toast.LENGTH_LONG).show()
                }
            }
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            ndef.close()

            // Loop through all records in the NDEF message
            for (record in ndefMessage.records) {
                if (record.tnf == NdefRecord.TNF_MIME_MEDIA &&
                    record.type.contentEquals("application/json".toByteArray(Charsets.US_ASCII))) {
                    val jsonData = String(record.payload, Charsets.UTF_8)
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Data read from tag.", Toast.LENGTH_LONG).show()
                    }

                    updateUIWithJson(jsonData)
                }
            }
            activity?.runOnUiThread {
                Toast.makeText(context, "No JSON data found on tag.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.d("errorhappen", e.message.toString())
            activity?.runOnUiThread {
                Toast.makeText(context, "Failed to read NFC tag: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun updateUIWithJson(jsonData: String) {
        try {
            val jsonObject = JSONObject(jsonData)
            val name = jsonObject.optString("name")
            val contact = jsonObject.optString("contact")
            val vehicleNo = jsonObject.optString("vehicle_no")
            val amount = jsonObject.optDouble("amount")

            // Update the UI on the main thread.
            activity?.runOnUiThread {
                binding.edtName.setText(name)
                binding.edtContact.setText(contact)
                binding.edtVehicleNo.setText(vehicleNo)
                binding.edtAmount.setText(amount.toString())
            }
        } catch (e: JSONException) {
            Log.e("NfcReadFragment", "Failed to parse JSON: ${e.message}")
            activity?.runOnUiThread {
                Toast.makeText(context, "Failed to parse JSON data", Toast.LENGTH_LONG).show()
            }
        }
    }


    /**
     * Enable NFC reader mode.
     */
//    private fun enableReaderMode() {
//        nfcAdapter?.enableReaderMode(
//            requireActivity(),
//            ,
//            // Here using FLAG_READER_NFC_A (and no platform sounds) for compatibility.
//            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
//            null
//        )
//    }

}