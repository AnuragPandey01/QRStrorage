package com.glitchcraftlabs.qrstorage.ui.history

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
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
    private val historyAdapter by lazy {
        ScanHistoryAdapter(listOf())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAllScansBinding.bind(view)

        initialiseRecyclerView()

        binding.sortRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.new_first -> viewModel.sortByNew()
                R.id.old_first -> viewModel.sortByOld()
            }
        }
        setHasOptionsMenu(true)
        viewModel.history.observe(viewLifecycleOwner){
            historyAdapter.updateData(it)
        }
    }

    private fun initialiseRecyclerView() {
        binding.allScansRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
        historyAdapter.setOnItemClick {
            findNavController().navigate(
                AllScansFragmentDirections.actionAllScansFragmentToGeneratedQrFragment(
                    tag = it.tag,
                    qrData = it.data
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}