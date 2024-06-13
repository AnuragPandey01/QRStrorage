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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel : HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setUpRecyclerView()
        registerListeners()

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
        //TODO: Implement scanner
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}