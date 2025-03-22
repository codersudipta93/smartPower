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


    // Modified function: read the JSON from the tag, update it with an "InTime" field, and then write it back.
    private fun readJsonFromTag(tag: Tag) {
        try {
            val ndef = Ndef.get(tag) ?: return Toast.makeText(context,"Tag doesn't support NDEF.",Toast.LENGTH_LONG).show()
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            ndef.close()

            viewModel.parkVehicle(tag.id.joinToString(separator = "") { String.format("%02X", it) },1)
            for (record in ndefMessage.records) {
                if (record.tnf == NdefRecord.TNF_MIME_MEDIA &&
                    record.type.contentEquals("application/json".toByteArray(Charsets.US_ASCII))) {
                    val jsonData = String(record.payload, Charsets.UTF_8)
                    val jsonObject = JSONObject(jsonData)

                    val vehicleNo=jsonObject.optString("vehicle_no", "")
                    val contact=jsonObject.optString("contact", "")
//                    Log.d("jksahfkja",inTimeStr);

                    if (!vehicleNo.isNullOrBlank()){
                        activity?.runOnUiThread {
                            binding.edtVehicleNo.setText(vehicleNo)
                        }
                    }

                    if (!contact.isNullOrBlank()){
                        activity?.runOnUiThread {
                            binding.edtContact.setText(contact)
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
                            binding.edtInTime.setText(it.data.inTime)
                            binding.edtOutTime.setText(it.data.outTime)
                            Toast.makeText(context, it.data.msg, Toast.LENGTH_LONG).show()
                            (requireActivity() as MainActivity).btManager.sendData("1".toByteArray())
                        }
                    }
                }}}
    }
}