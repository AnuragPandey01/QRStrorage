package com.glitchcraftlabs.qrstorage.ui.generated_qr

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.glitchcraftlabs.qrstorage.R
import com.glitchcraftlabs.qrstorage.databinding.FragmentGeneratedQrBinding
import com.glitchcraftlabs.qrstorage.util.Constants.QR_SIZE
import com.glitchcraftlabs.qrstorage.util.QRGenerator
import com.glitchcraftlabs.qrstorage.util.QueryResult
import com.glitchcraftlabs.qrstorage.util.saveImage
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GeneratedQrFragment : Fragment(R.layout.fragment_generated_qr) {

    private val viewModel: GeneratedQrViewModel by viewModels()
    private var _binding: FragmentGeneratedQrBinding? = null
    private val binding: FragmentGeneratedQrBinding get() = _binding!!
    private val args by navArgs<GeneratedQrFragmentArgs>()
    private val progressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage("Please wait...")
            setTitle("Updating")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGeneratedQrBinding.bind(view)
        binding.qrImageView.setImageBitmap(
            QRGenerator.generateQRCodeBitmap(
                args.qrData,
                QR_SIZE,
                QR_SIZE
            )
        )
        binding.qrTag.text = args.tag

        registerListeners()
    }

    private fun registerListeners() {
        binding.shareButton.setOnClickListener {
            checkPermAndSaveImage(launchShareIntent = true)
        }

        binding.saveToGalleryButton.setOnClickListener {
            checkPermAndSaveImage()
        }

        binding.btnEditTag.setOnClickListener {
            val dialogView =
                LayoutInflater.from(requireContext()).inflate(R.layout.edit_tag_dialog_layout, null)
            val tagInput = dialogView.findViewById<TextInputEditText>(R.id.tagInput)
            tagInput.setText(binding.qrTag.text)

            val dialog =  MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setTitle(getString(R.string.edit_tag))
                .create()

            dialogView.findViewById<MaterialButton>(R.id.cancel_edit_button).setOnClickListener {
                dialog.dismiss()
            }
            dialogView.findViewById<MaterialButton>(R.id.save_edit_button).setOnClickListener {
                progressDialog.show()
                val newTag = tagInput.text.toString()
                if (newTag.isNotBlank()) {
                    lifecycleScope.launch {
                        viewModel.updateHistory(binding.qrTag.text.toString(), newTag).observe(viewLifecycleOwner){
                            if(it is QueryResult.Error){
                                progressDialog.dismiss()
                                tagInput.error = it.message
                            }else if(it is QueryResult.Success){
                                progressDialog.dismiss()
                                binding.qrTag.text = newTag
                                dialog.dismiss()
                            }
                        }
                    }
                }else{
                    tagInput.error = getString(R.string.tag_cannot_be_empty)
                }
            }
            dialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkPermAndSaveImage(launchShareIntent: Boolean = false) {
        if (Build.VERSION.SDK_INT < 29 && !hasWriteExternalStoragePerm()) {
            permRequestLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        requireContext().contentResolver.saveImage(
            binding.qrImageView.drawable.toBitmap(),
            args.tag,
            onSuccess = {
                if (launchShareIntent) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, it)
                    }
                    startActivity(Intent.createChooser(intent, getString(R.string.share_qr_code)))
                }
                Toast.makeText(requireContext(),
                    getString(R.string.qr_image_saved_to_gallery), Toast.LENGTH_SHORT)
                    .show()
            },
            onFailure = {
                Toast.makeText(requireContext(),
                    getString(R.string.failed_to_save_image), Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun hasWriteExternalStoragePerm() =
        requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private val permRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                checkPermAndSaveImage()
            } else {
                if (
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", requireActivity().packageName, null)
                    }
                    settingLauncher.launch(intent)

                }
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_is_required_to_save),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val settingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (
                ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                checkPermAndSaveImage()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_is_required_to_save),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

}