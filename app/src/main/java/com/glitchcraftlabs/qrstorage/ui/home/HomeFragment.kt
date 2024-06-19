package com.glitchcraftlabs.qrstorage.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.glitchcraftlabs.qrstorage.R
import com.glitchcraftlabs.qrstorage.databinding.FragmentHomeBinding
import com.glitchcraftlabs.qrstorage.ui.adapter.ScanHistoryAdapter
import com.glitchcraftlabs.qrstorage.ui.scan_result.ScanResultBottomSheetFragment
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
    private var qrResult: Barcode? = null
    private val recentScanAdapter by lazy {
        ScanHistoryAdapter(listOf(), 5)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // Initialize the scanner
        scanner = GmsBarcodeScanning.getClient(requireActivity(),options)

        setUpRecyclerView()
        registerListeners()

        viewModel.history.observe(viewLifecycleOwner){
            if(it.isEmpty()) {
                binding.recentScansRecyclerView.visibility = View.GONE
                binding.noScansTextView.visibility = View.VISIBLE
            }else{
                binding.recentScansRecyclerView.visibility = View.VISIBLE
                binding.noScansTextView.visibility = View.GONE
                recentScanAdapter.updateData(it)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        qrResult?.let{
            ScanResultBottomSheetFragment(
                qrResult!!,
                onDismiss = {
                    viewModel.insertScan(qrResult!!)
                    qrResult = null
                }
            ).show(parentFragmentManager,"ScanResult")
        }
    }

    private fun setUpRecyclerView() {
        binding.recentScansRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentScanAdapter
        }

        recentScanAdapter.setOnItemClick {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToGeneratedQrFragment(
                    tag = it.tag,
                    qrData = it.data
                )
            )
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
            if(binding.radioGroup.checkedRadioButtonId != R.id.textRadioButton){
                return@setOnClickListener
            }
            if(binding.tagInput.text.isNullOrBlank() || binding.qrTextInput.text.isNullOrBlank()){
                Snackbar.make(binding.root, "Please fill all fields", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.insertGeneratedQR(
                tag = binding.tagInput.text.toString(),
                value = binding.qrTextInput.text.toString()
            )
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToGeneratedQrFragment(
                    tag = binding.tagInput.text.toString(),
                    qrData = binding.qrTextInput.text.toString()
                )
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