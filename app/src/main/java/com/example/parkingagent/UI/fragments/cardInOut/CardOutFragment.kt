package com.example.parkingagent.UI.fragments.cardInOut

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.parkingagent.MainActivity
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.databinding.FragmentCardOutBinding
import com.example.parkingagent.utils.BluetoothConnectionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class CardOutFragment:BaseFragment<FragmentCardOutBinding>() {

    private val viewModel: CardInOutViewModel by viewModels()
    private var nfcAdapter: NfcAdapter? = null

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_card_out
    }

    override fun initView() {
        super.initView()

        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
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
//        btManager.closeConnection()
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
            (requireActivity() as MainActivity).btManager.sendData("1".toByteArray())
        } catch (e: Exception) {
//            Log.d("errorhappen", e.message.toString())
            activity?.runOnUiThread {
                Toast.makeText(context, "Failed to write NFC tag: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Modified function: read the JSON from the tag, update it with an "InTime" field, and then write it back.
    private fun readJsonFromTag(tag: Tag) {
        try {
            val ndef = Ndef.get(tag) ?: return Toast.makeText(context,"Tag doesn't support NDEF.",Toast.LENGTH_LONG).show()
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            ndef.close()

            for (record in ndefMessage.records) {
                if (record.tnf == NdefRecord.TNF_MIME_MEDIA &&
                    record.type.contentEquals("application/json".toByteArray(Charsets.US_ASCII))) {
                    val jsonData = String(record.payload, Charsets.UTF_8)
                    val jsonObject = JSONObject(jsonData)

                    val inTimeStr = jsonObject.optString("InTime", "")
                    val vehicleNo=jsonObject.optString("vehicle_no", "")
                    val contact=jsonObject.optString("contact", "")
//                    Log.d("jksahfkja",inTimeStr);
                    if (inTimeStr.isNotEmpty() && inTimeStr != "") {
                        val inTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(inTimeStr)
                        val outTime = Date()
                        val outTimeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(outTime)

                        val durationMillis = outTime.time - (inTime?.time ?: outTime.time)
                        val durationHours = TimeUnit.MILLISECONDS.toHours(durationMillis) + 1
                        val chargeAmount = durationHours * 10 // ₹10 per hour

                        val currentAmount = jsonObject.optInt("amount", 0)
                        val updatedAmount = (currentAmount - chargeAmount).coerceAtLeast(0)

                        jsonObject.put("OutTime", "")
                        jsonObject.put("amount", updatedAmount)
                        jsonObject.put("InTime","")


                        activity?.runOnUiThread {
//                            binding.txtCardId.text = "Card ID: " + tag.id.joinToString(" ") { String.format("%02X", it) }
                            binding.edtInTime.setText(inTimeStr)
                            binding.edtOutTime.setText(outTimeStr)
                            binding.edtVehicleNo.setText(vehicleNo)
                            binding.edtContact.setText(contact)
                            binding.edtDuration.setText(durationHours.toString())
//                            binding.txtDuration.text = "Duration: $durationHours hours"
                            binding.edtChargeAmount.setText(chargeAmount.toString())
                            binding.edtAmount.setText(updatedAmount.toString())
                        }

                        writeJsonToTag(tag, jsonObject.toString())

                        viewModel.parkVehicle(tag.id.joinToString(separator = "") { String.format("%02X", it) },1)
                    }
                    else{
                        activity?.runOnUiThread {
                            Toast.makeText(
                                context,
                                "Vehicle Already Checked Out",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            activity?.runOnUiThread {
                Toast.makeText(context, "Failed to read NFC tag: ${e.message}", Toast.LENGTH_LONG)
                    .show();
            }
        }
    }


    override fun observe() {
        super.observe()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mutualSharedflow.collectLatest {
                    (requireActivity() as MainActivity).binding.loading.visibility = View.GONE
                    when(it){
                        is CardInOutViewModel.VehicleCardEvents.VehicleCardError -> {
                            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                        }
                        is CardInOutViewModel.VehicleCardEvents.VehicleCardParked -> {
                            binding.edtDuration.setText(it.data.duration)
                            Toast.makeText(context, it.data.msg, Toast.LENGTH_LONG).show()
                        }
                    }
                }}}
    }
}