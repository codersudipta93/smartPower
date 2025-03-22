package com.example.parkingagent.UI.fragments.cardInOut

import android.nfc.NdefMessage
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.parkingagent.MainActivity
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.UI.fragments.nfcFragment.NfcViewModel
import com.example.parkingagent.databinding.FragmentCardInBinding
import com.example.parkingagent.utils.BluetoothConnectionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class CardInFragment : BaseFragment<FragmentCardInBinding>() {

    private val viewModel: CardInOutViewModel by viewModels()
    private var nfcAdapter: NfcAdapter? = null
//    private var nfcTag: Tag? = null
//    private var jsonData:String?=null


    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_card_in
    }

    override fun initView() {
        super.initView()
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
//        btManager.initialize()
        if (nfcAdapter == null) {
            Toast.makeText(context, "NFC is not supported on this device.", Toast.LENGTH_LONG).show()
            return
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

    override fun onDestroy() {
        super.onDestroy()

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

    // This function writes the provided JSON string to the NFC tag.
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
            // Create an NDEF record with MIME type "application/json"
            val mimeRecord = NdefRecord.createMime(
                "application/json",
                jsonData.toByteArray(Charsets.UTF_8)
            )
            val ndefMessage = NdefMessage(arrayOf(mimeRecord))
            ndef.writeNdefMessage(ndefMessage)
            ndef.close()
            activity?.runOnUiThread {
                Toast.makeText(context, "Successfully wrote data to tag.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
//            Log.d("errorhappen", e.message.toString())
            activity?.runOnUiThread {
                Toast.makeText(context, "Failed to write NFC tag: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Modified function: read the JSON from the tag, update it with an "InTime" field, and then write it back.
//    private fun readJsonFromTag(tag: Tag) {
//        try {
//            val ndef = Ndef.get(tag)
//            if (ndef == null) {
//                activity?.runOnUiThread {
//                    Toast.makeText(context, "Tag doesn't support NDEF.", Toast.LENGTH_LONG).show()
//                }
//                return
//            }
//            ndef.connect()
//            val ndefMessage = ndef.ndefMessage
//            ndef.close()
//
//            var jsonFound = false
//            // Loop through all records in the NDEF message
//            for (record in ndefMessage.records) {
//                if (record.tnf == NdefRecord.TNF_MIME_MEDIA &&
//                    record.type.contentEquals("application/json".toByteArray(Charsets.US_ASCII))) {
//                    val jsonData = String(record.payload, Charsets.UTF_8)
//                    activity?.runOnUiThread {
//                        Toast.makeText(context, "Data read from tag.", Toast.LENGTH_LONG).show()
//                        (requireActivity() as MainActivity).binding.loading.visibility = View.VISIBLE
//                    }
//                    // Parse the JSON and add the "InTime" field.
//                    val jsonObject = JSONObject(jsonData)
//                    val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
//                    jsonObject.put("InTime", currentTime)
//                    jsonObject.put("OutTime", "")
//                    val updatedJsonData = jsonObject.toString()
////                    Log.d("CardInFragment", "Updated JSON: $updatedJsonData")
//
//                    // Write the updated JSON back to the NFC tag.
////                    writeJsonToTag(tag, updatedJsonData)
//                    jsonFound = true
//                    break
//                }
//            }
//            if (!jsonFound) {
//                activity?.runOnUiThread {
//                    Toast.makeText(context, "No JSON data found on tag. Please issue card", Toast.LENGTH_LONG).show()
//                }
//            }
//            else {
//                val cardId = tag.id.joinToString(separator = "") { String.format("%02X", it) }
//                // Pass the card ID to your ViewModel.
//                viewModel.parkVehicle(cardId)
//
//            }
//
//
//        } catch (e: Exception) {
////            Log.d("errorhappen", e.message.toString())
//            activity?.runOnUiThread {
//                Toast.makeText(context, "Failed to read NFC tag: ${e.message}", Toast.LENGTH_LONG).show()
//            }
//        }
//    }

    private fun readJsonFromTag(tag: Tag) {
        try {
            val ndef = Ndef.get(tag)
            if (ndef == null) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Tag doesn't support NDEF.", Toast.LENGTH_LONG).show()
                }
                return
            }
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            ndef.close()

            var jsonFound = false
            // Loop through all records in the NDEF message
            for (record in ndefMessage.records) {
                if (record.tnf == NdefRecord.TNF_MIME_MEDIA &&
                    record.type.contentEquals("application/json".toByteArray(Charsets.US_ASCII))) {
                    val jsonData = String(record.payload, Charsets.UTF_8)
                    jsonFound = true

                    // Parse the JSON data and get the expiryDate field
                    val jsonObject = JSONObject(jsonData)
                    val expiryDateStr = jsonObject.optString("expiryDate", "")
                    if (expiryDateStr.isEmpty()) {
                        activity?.runOnUiThread {
                            Toast.makeText(context, "Expiry date not found on card.", Toast.LENGTH_LONG).show()
                        }
                        return
                    }

                    // Parse the expiry date (assuming format "yyyy-MM-dd")
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val expiryDate = dateFormat.parse(expiryDateStr)
                    val currentDate = Date()

                    activity?.runOnUiThread {
                        if (expiryDate != null && currentDate.before(expiryDate)) {
                            // Card is valid – proceed to park vehicle.
                            val cardId = tag.id.joinToString(separator = "") { String.format("%02X", it) }
                            viewModel.parkVehicle(cardId)
                        } else {
                            Toast.makeText(context, "Card has expired.", Toast.LENGTH_LONG).show()
                        }
                    }
                    break
                }
            }
            if (!jsonFound) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "No JSON data found on tag. Please issue card", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            activity?.runOnUiThread {
                Toast.makeText(context, "Failed to read NFC tag: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun observe() {
        super.observe()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mutualSharedflow.collectLatest {
                    (requireActivity() as MainActivity).binding.loading.visibility = View.GONE

                    when(it) {
                        is CardInOutViewModel.VehicleCardEvents.VehicleCardError -> {
                            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                        }
                        is CardInOutViewModel.VehicleCardEvents.VehicleCardParked -> {
                            Toast.makeText(context, it.data.msg, Toast.LENGTH_LONG).show()
                            (requireActivity() as MainActivity).btManager.sendData("1".toByteArray())

                        }
                    }
                }
            }
        }
    }


}