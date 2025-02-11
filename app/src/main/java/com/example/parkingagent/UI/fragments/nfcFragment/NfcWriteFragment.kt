package com.example.parkingagent.UI.fragments.nfcFragment

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.view.View
import android.widget.Toast
import com.example.parkingagent.UI.fragments.nfcFragment.NfcViewModel

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.example.parkingagent.MainActivity
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.UI.fragments.LoginFragment.LoginViewModel.LoginEvents
import com.example.parkingagent.databinding.FragmentNfcWriteBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NfcWriteFragment:BaseFragment<FragmentNfcWriteBinding>() {

    private val viewModel: NfcViewModel by viewModels()


    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_nfc_write
    }

    override fun initView() {
        super.initView()

        binding.btnGuestRegister.setOnClickListener{
            registerGuest()
        }

    }

    private fun registerGuest(){
    if (binding.edtName.text?.isEmpty() == true){
        binding.edtName.error="Please enter name"
        return
    }
    if (binding.edtContact.text?.isEmpty() == true){
        binding.edtContact.error="Please enter contact number"
        return
    }
        if (binding.edtContact.text?.trim()?.length!! <10){
            binding.edtContact.error="Please enter valid contact number"
            return
        }
        if (binding.edtVehicleNo.text?.isEmpty() == true){
            binding.edtVehicleNo.error="Please enter vehicle number"
            return
        }

        if (binding.edtAmount.text.isNullOrBlank() || binding.edtAmount.text.isNullOrEmpty()){
            binding.edtAmount.error="Please enter amount"
            return
        }
        (requireActivity() as MainActivity).binding.loading.visibility= View.VISIBLE
        viewModel.registerGuest(binding.edtName.text?.toString()?:"",binding.edtContact.text.toString(),binding.edtVehicleNo.text.toString(),binding.edtAmount.text.toString().toDouble(),"test")

    }

    private fun writeJsonToTag(tag: Tag, jsonData: String) {
        try {
            val ndef = Ndef.get(tag)
            if (ndef == null) {
                // Tag doesn't support NDEF.
                requireActivity().runOnUiThread {
                    Toast.makeText(context,"Tag doesn't support NDEF.",Toast.LENGTH_LONG).show()
                }
                return
            }
            ndef.connect()
            if (!ndef.isWritable) {
                requireActivity().runOnUiThread {
                    Toast.makeText(context,"Tag is not writable.",Toast.LENGTH_LONG).show()
                }
                ndef.close()
                return
            }
            // Create an NDEF record with MIME type "application/json".
            val mimeRecord = NdefRecord.createMime("application/json", jsonData.toByteArray(Charsets.UTF_8))
            val ndefMessage = NdefMessage(arrayOf(mimeRecord))
            ndef.writeNdefMessage(ndefMessage)
            requireActivity().runOnUiThread {
                Toast.makeText(context, "Successfully wrote JSON to tag.", Toast.LENGTH_LONG).show()
            }
            ndef.close()
        } catch (e: Exception) {
            requireActivity().runOnUiThread {
                Toast.makeText(context,e.message,Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun observe() {
        super.observe()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mutualSharedflow.collectLatest {
                    (requireActivity() as MainActivity).binding.loading.visibility= View.GONE
                    when (it){
                        is NfcViewModel.GuestEvents.GuestRegistered ->{
                            binding.edtName.text?.clear()
                            binding.edtContact.text?.clear()
                            binding.edtVehicleNo.text?.clear()
                            binding.edtAmount.text?.clear()
                        }
                        is NfcViewModel.GuestEvents.Error ->{
                            Toast.makeText(context,it.message,Toast.LENGTH_LONG).show()
                        }

                    }

                }
            }
        }

    }

}