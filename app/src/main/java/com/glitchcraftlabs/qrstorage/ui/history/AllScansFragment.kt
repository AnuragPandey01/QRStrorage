package com.glitchcraftlabs.qrstorage.ui.history

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.glitchcraftlabs.qrstorage.R
import com.glitchcraftlabs.qrstorage.databinding.FragmentAllScansBinding
import com.glitchcraftlabs.qrstorage.ui.adapter.ScanHistoryAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllScansFragment : Fragment(R.layout.fragment_all_scans) {

    private var _binding: FragmentAllScansBinding? = null
    private val binding: FragmentAllScansBinding get() = _binding!!
    private val viewModel: AllScansViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAllScansBinding.bind(view)

        binding.allScansRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ScanHistoryAdapter(listOf()) //TODO: Pass the list of scans
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}