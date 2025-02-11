package com.example.parkingagent.UI.fragments.nfcFragment

import android.nfc.NfcAdapter
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.databinding.FragmentNfcReadBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NfcReadFragment : BaseFragment<FragmentNfcReadBinding>(){

    // Native NFC adapter
    private var nfcAdapter: NfcAdapter? = null

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_nfc_read
    }

    override fun initView() {
        super.initView()
        if (nfcAdapter == null) {
            Log.e("NFC", "This device doesn't support NFC.")
            binding.idCard.text = "NFC is not supported on this device."
        }

        binding.idBtnNfcWrite.setOnClickListener {
            findNavController().navigate(R.id.action_id_fragment_nfc_to_id_fragment_nfc_write)
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