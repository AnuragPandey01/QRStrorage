package com.glitchcraftlabs.qrstorage.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.glitchcraftlabs.qrstorage.MainActivity
import com.glitchcraftlabs.qrstorage.R
import com.glitchcraftlabs.qrstorage.databinding.FragmentHomeBinding
import com.glitchcraftlabs.qrstorage.ui.adapter.ScanHistoryAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel : HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val options : GmsBarcodeScannerOptions by lazy{
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
    }
    private lateinit var scanner : GmsBarcodeScanner
    var qrResult: Barcode? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // Initialize the scanner
        scanner = GmsBarcodeScanning.getClient(requireActivity(),options)

        setUpRecyclerView()
        registerListeners()


    }

    override fun onResume() {
        super.onResume()
        qrResult?.let{
            ScanResultBottomSheetFragment(qrResult!!).show(parentFragmentManager,"ScanResult")
            qrResult = null
        }
    }

    private fun setUpRecyclerView() {
        binding.recentScansRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ScanHistoryAdapter(listOf(), 5) //TODO: Pass the list of
        }
    }

    private fun registerListeners() {

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                R.id.textRadioButton -> {
                    binding.uploadFileButton.visibility = View.GONE
                    binding.fileStorageInfoTextView.visibility = View.GONE
                    binding.qrTextInputLayout.visibility = View.VISIBLE
                }
                R.id.fileRadioButton -> {
                    binding.uploadFileButton.visibility = View.VISIBLE
                    binding.fileStorageInfoTextView.visibility = View.VISIBLE
                    binding.qrTextInputLayout.visibility = View.GONE
                }
            }
        }

        binding.uploadFileButton.setOnClickListener {
            //TODO: Implement file upload
        }

        binding.generateButton.setOnClickListener {
            //TODO: Implement QR code generation
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToGeneratedQrFragment()
            )
        }

        binding.quickScanButton.setOnClickListener {
            openScanner()
        }

        binding.allScansButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAllScansFragment())
        }
    }

    private fun openScanner() {
        scanner.startScan().addOnSuccessListener{
            qrResult = it
        }.addOnCanceledListener {
            Snackbar.make(binding.root, "Scan cancelled", Snackbar.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Snackbar.make(binding.root, "Error scanning QR code", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}