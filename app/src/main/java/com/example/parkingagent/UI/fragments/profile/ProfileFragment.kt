package com.example.parkingagent.UI.fragments.profile

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.parkingagent.R
import com.example.parkingagent.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Native NFC adapter
    private var nfcAdapter: NfcAdapter? = null

    // Flag to determine the current mode: false = read mode, true = write mode.
    private var isWriteMode: Boolean = false

    // The data to write when in write mode.
    private var dataToWrite: String = "Hello NFC!"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        // Obtain the default NFC adapter.
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        if (nfcAdapter == null) {
            Log.e("NFC", "This device doesn't support NFC.")
            binding.tvCardInfo.text = "NFC is not supported on this device."
        }

        // Set up the button to toggle between read and write mode.
        binding.btnStartCardRead.setOnClickListener {
            isWriteMode = !isWriteMode
            binding.btnStartCardRead.text = if (isWriteMode) "Write Card" else "Read Card"
            binding.tvCardInfo.text = if (isWriteMode)
                "Write mode enabled. Tap an NFC tag to write data."
            else
                "Read mode enabled. Tap an NFC tag to read data."
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

    /**
     * Enable NFC reader mode.
     */
    private fun enableReaderMode() {
        nfcAdapter?.enableReaderMode(
            requireActivity(),
            { tag -> nfcReaderCallback(tag) },
            // Here using FLAG_READER_NFC_A (and no platform sounds) for compatibility.
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            null
        )
    }

    /**
     * Disable NFC reader mode.
     */
    private fun disableReaderMode() {
        nfcAdapter?.disableReaderMode(requireActivity())
    }

    /**
     * NFC reader callback invoked when an NFC tag is detected.
     */
    private fun nfcReaderCallback(tag: Tag) {
        if (isWriteMode) {
            writeToTag(tag, "dkkjfgskgfdsjkfgdsfjk")
        } else {
            readFromTag(tag)
        }
    }

    /**
     * Reads an NDEF message from the NFC tag.
     */
    private fun readFromTag(tag: Tag) {
        try {
            val ndef = Ndef.get(tag)
            ndef?.connect()
            val ndefMessage: NdefMessage? = ndef?.ndefMessage
            val result = StringBuilder()
            if (ndefMessage != null) {
                for (record in ndefMessage.records) {
                    val text = when {
                        // Handle well-known text records (with status byte and language code).
                        record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                                record.type.contentEquals(NdefRecord.RTD_TEXT) -> {
                            val payload = record.payload
                            val textEncoding = if ((payload[0].toInt() and 0x80) == 0) Charsets.UTF_8 else Charsets.UTF_16
                            val languageCodeLength = payload[0].toInt() and 0x3F
                            String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, textEncoding)
                        }
                        // Handle MIME media records (where the payload is directly the text).
                        record.tnf == NdefRecord.TNF_MIME_MEDIA &&
                                String(record.type, Charsets.US_ASCII) == "text/plain" -> {
                            String(record.payload, Charsets.UTF_8)
                        }
                        else -> "Unsupported record type"
                    }
                    result.append(text).append("\n")
                }
                Log.d("NFC", "Read content: ${result.toString()}")
            } else {
                Log.d("NFC", "No NDEF message found on tag.")
                result.append("No NDEF message found.")
            }
            ndef?.close()
            // Update UI on the main thread.
            requireActivity().runOnUiThread {
                binding.tvCardInfo.text = result.toString()
            }
        } catch (e: Exception) {
            Log.e("NFC", "Error reading NFC tag", e)
        }
    }

    /**
     * Writes a plain-text NDEF message to the NFC tag.
     */
    private fun writeToTag(tag: Tag, data: String) {
        try {
            val ndef = Ndef.get(tag)
            if (ndef == null) {
                Log.e("NFC", "Tag doesn't support NDEF")
                requireActivity().runOnUiThread {
                    binding.tvCardInfo.text = "Tag doesn't support NDEF."
                }
                return
            }
            ndef.connect()
            if (!ndef.isWritable) {
                Log.e("NFC", "Tag is not writable")
                requireActivity().runOnUiThread {
                    binding.tvCardInfo.text = "Tag is not writable."
                }
                ndef.close()
                return
            }
            // Create an NDEF record with MIME type "text/plain".
            val mimeRecord = NdefRecord.createMime("text/plain", data.toByteArray(Charsets.UTF_8))
            val ndefMessage = NdefMessage(arrayOf(mimeRecord))
            ndef.writeNdefMessage(ndefMessage)
            Log.d("NFC", "Successfully wrote to tag.")
            requireActivity().runOnUiThread {
                binding.tvCardInfo.text = "Successfully wrote to tag."
            }
            ndef.close()
        } catch (e: Exception) {
            Log.e("NFC", "Failed to write NFC tag", e)
            requireActivity().runOnUiThread {
                binding.tvCardInfo.text = "Failed to write NFC tag: ${e.message}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
