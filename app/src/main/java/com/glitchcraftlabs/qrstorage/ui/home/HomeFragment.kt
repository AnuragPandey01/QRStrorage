package com.glitchcraftlabs.qrstorage.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.glitchcraftlabs.qrstorage.R
import com.glitchcraftlabs.qrstorage.databinding.FragmentHomeBinding
import com.glitchcraftlabs.qrstorage.ui.adapter.ScanHistoryAdapter
import com.glitchcraftlabs.qrstorage.ui.scan_result.ScanResultBottomSheetFragment
import com.glitchcraftlabs.qrstorage.util.QueryResult
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID

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
        ScanHistoryAdapter(listOf())
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // Initialize the scanner
        scanner = GmsBarcodeScanning.getClient(requireActivity(),options)

        setUpRecyclerView()
        registerListeners()

        viewModel.history.observe(viewLifecycleOwner){
            when(it){
                is QueryResult.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                    binding.noScansTextView.visibility = View.GONE
                    binding.recentScansRecyclerView.visibility = View.GONE
                }
                is QueryResult.Success -> {
                    binding.progressIndicator.visibility = View.GONE
                    if(it.data!!.isEmpty()) {
                        binding.recentScansRecyclerView.visibility = View.GONE
                        binding.noScansTextView.visibility = View.VISIBLE
                    }else{
                        binding.recentScansRecyclerView.visibility = View.VISIBLE
                        binding.noScansTextView.visibility = View.GONE
                        recentScanAdapter.updateData(it.data)
                    }
                }
                is QueryResult.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        qrResult?.let{
            ScanResultBottomSheetFragment(
                qrResult!!,
                onDismiss = {
                    viewModel.viewModelScope.launch {
                        viewModel.insertScan(qrResult!!)
                    }
                    qrResult = null
                }
            ).show(parentFragmentManager,"ScanResult")
        }
        viewModel.loadHistory()
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
                Snackbar.make(binding.root,
                    getString(R.string.please_fill_all_fields), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                viewModel.insertGeneratedQR(
                    tag = binding.tagInput.text.toString(),
                    value = binding.qrTextInput.text.toString()
                ).observe(viewLifecycleOwner){
                    if(it is QueryResult.Error){
                        binding.tagInput.error = it.message
                    }

                    if(it is QueryResult.Success){
                        findNavController().navigate(
                            HomeFragmentDirections.actionHomeFragmentToGeneratedQrFragment(
                                tag = binding.tagInput.text.toString(),
                                qrData = binding.qrTextInput.text.toString()
                            )
                        )
                        binding.tagInput.text?.clear()
                        binding.qrTextInput.text?.clear()
                    }
                }
            }
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
            Snackbar.make(binding.root, getString(R.string.scan_cancelled), Snackbar.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Snackbar.make(binding.root,
                getString(R.string.error_scanning_qr_code), Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}