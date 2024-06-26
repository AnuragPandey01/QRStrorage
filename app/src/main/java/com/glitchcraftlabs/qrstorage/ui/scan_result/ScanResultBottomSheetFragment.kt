package com.glitchcraftlabs.qrstorage.ui.scan_result

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.glitchcraftlabs.qrstorage.R
import com.glitchcraftlabs.qrstorage.databinding.BottomsheetScanReultBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.mlkit.vision.barcode.common.Barcode


class ScanResultBottomSheetFragment(
    private val qrResult: Barcode,
    private val onDismiss: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetScanReultBinding? = null
    private val binding: BottomsheetScanReultBinding
        get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetScanReultBinding.inflate(layoutInflater)
        setOnClickListener()
        return binding.root
    }

    private fun setOnClickListener() {

        val scanResult = qrResult.displayValue.orEmpty()

        binding.resultText.text = scanResult
        binding.resultText.setOnClickListener {
            // Copy the text to clipboard
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Scan Result", scanResult))
            Toast.makeText(requireContext(),
                getString(R.string.text_copied_to_clipboard), Toast.LENGTH_SHORT).show()
        }

        when(qrResult.valueType){
            Barcode.TYPE_URL -> {
                binding.btnOpenUrl.visibility = View.VISIBLE
                binding.btnOpenUrl.setOnClickListener {
                    startIntent(Intent(Intent.ACTION_VIEW, Uri.parse(qrResult.url!!.url)))
                }
            }
            Barcode.TYPE_EMAIL -> {
                binding.btnOpenUrl.visibility = View.VISIBLE
                binding.btnOpenUrl.setOnClickListener {
                    startIntent(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${qrResult.email!!.address}")))
                }
            }
            Barcode.TYPE_PHONE -> {
                binding.btnOpenUrl.visibility = View.VISIBLE
                binding.btnOpenUrl.setOnClickListener {
                    startIntent(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${qrResult.phone!!.number}")))
                }
            }
            Barcode.TYPE_CONTACT_INFO ->{
                val details = qrResult.contactInfo!!
                binding.btnOpenUrl.visibility = View.VISIBLE
                binding.btnOpenUrl.setOnClickListener {
                    val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
                        putExtra(ContactsContract.Intents.Insert.PHONE, details.phones.firstOrNull()?.number)
                        putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, details.phones.firstOrNull()?.type)
                        putExtra(ContactsContract.Intents.Insert.EMAIL, details.emails.firstOrNull()?.address)
                        putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, details.emails.firstOrNull()?.type)
                        putExtra(ContactsContract.Intents.Insert.NAME, details.name?.formattedName)
                    }
                    startIntent(intent)
                }
            }
            else -> {
                binding.btnOpenUrl.visibility = View.GONE
            }
        }

        binding.btnShare.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, scanResult)
                type = "text/plain"
            }
            startIntent(Intent.createChooser(sendIntent, getString(R.string.share_via)))
        }
    }

    private fun startIntent(intent: Intent){
        try {
            startActivity(intent)
        } catch (ignored: ActivityNotFoundException) {
            // no Activity found to run the given Intent
            Toast.makeText(requireContext(),
                getString(R.string.no_compatible_app_found), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

