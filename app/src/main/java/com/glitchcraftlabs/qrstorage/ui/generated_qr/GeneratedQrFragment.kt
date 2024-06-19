package com.glitchcraftlabs.qrstorage.ui.generated_qr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.glitchcraftlabs.qrstorage.R
import com.glitchcraftlabs.qrstorage.databinding.FragmentGeneratedQrBinding
import com.glitchcraftlabs.qrstorage.util.QRGenerator
import com.glitchcraftlabs.qrstorage.util.saveImage
import com.google.android.datatransport.BuildConfig
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class GeneratedQrFragment : Fragment(R.layout.fragment_generated_qr) {

    private val viewModel: GeneratedQrViewModel by viewModels()
    private var _binding: FragmentGeneratedQrBinding? = null
    private val binding: FragmentGeneratedQrBinding get() = _binding!!
    private val args by navArgs<GeneratedQrFragmentArgs>()

    companion object{
        const val QR_WIDTH = 250
        const val QR_HEIGHT = 250
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGeneratedQrBinding.bind(view)
        binding.qrImageView.setImageBitmap(QRGenerator.generateQRCodeBitmap(args.qrData, QR_WIDTH, QR_HEIGHT))
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
                if(launchShareIntent){
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, it)
                    }
                    startActivity(Intent.createChooser(intent, "Share QR code"))
                }
                Toast.makeText(requireContext(), "Image saved", Toast.LENGTH_SHORT).show()
            },
            onFailure = {
                Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun hasWriteExternalStoragePerm() =
        requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private val permRequestLauncher =  registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        if(isGranted){
            checkPermAndSaveImage()
        }else{
            if(
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
            Toast.makeText(requireContext(), "permission is required to save", Toast.LENGTH_SHORT).show()
        }
    }

    private val settingLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            checkPermAndSaveImage()
        }else{
            Toast.makeText(requireContext(), "permission is required to save", Toast.LENGTH_SHORT).show()
        }
    }

}