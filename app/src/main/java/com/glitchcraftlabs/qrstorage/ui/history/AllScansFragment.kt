package com.glitchcraftlabs.qrstorage.ui.history

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.AdapterView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.glitchcraftlabs.qrstorage.R
import com.glitchcraftlabs.qrstorage.databinding.FragmentAllScansBinding
import com.glitchcraftlabs.qrstorage.ui.adapter.ScanHistoryAdapter
import com.glitchcraftlabs.qrstorage.util.QueryResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllScansFragment : Fragment(R.layout.fragment_all_scans), AdapterView.OnItemSelectedListener {

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

        viewModel.history.observe(viewLifecycleOwner){
            when(it){
                is QueryResult.Success -> {
                    if(it.data.isNullOrEmpty()){
                        binding.apply {
                            allScansRecyclerView.visibility = View.GONE
                            progressIndicator.visibility = View.GONE
                            noScansTextView.visibility = View.VISIBLE
                        }
                    }else{
                        binding.apply {
                            allScansRecyclerView.visibility = View.VISIBLE
                            progressIndicator.visibility = View.GONE
                            noScansTextView.visibility = View.GONE
                        }
                        historyAdapter.updateData(it.data)
                    }
                }
                is QueryResult.Error -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is QueryResult.Loading -> {
                    binding.apply {
                        allScansRecyclerView.visibility = View.GONE
                        progressIndicator.visibility = View.VISIBLE
                        noScansTextView.visibility = View.GONE
                    }
                }
            }
        }

        val menuHost = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        menuHost.addMenuProvider(object : MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.history_menu , menu)
                val searchView = menu.findItem(R.id.menu_search)!!.actionView as SearchView
                searchView.isSubmitButtonEnabled = true
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        viewModel.searchByTag(query.orEmpty())
                        return true
                    }

                    override fun onQueryTextChange(query: String?): Boolean {
                        viewModel.searchByTag(query.orEmpty())
                        return true
                    }

                })

                val sortButton = menu.findItem(R.id.menu_sort)
                sortButton.setOnMenuItemClickListener {
                    val dialogView =
                        LayoutInflater.from(requireContext()).inflate(R.layout.sort_options_dialog_layout, null)
                    val dialog =  MaterialAlertDialogBuilder(requireContext())
                        .setView(dialogView)
                        .setTitle("Sort By")
                        .create()

                    val sortOptions = dialogView.findViewById<RadioGroup>(R.id.radio_group_sort_options)
                    sortOptions.check(viewModel.checkedRadioButtonId)
                    sortOptions.setOnCheckedChangeListener { _, checkedId ->
                        when(checkedId){
                            R.id.radio_recently_added -> viewModel.sortByNew()
                            R.id.radio_oldest_first -> viewModel.sortByOld()
                            R.id.radio_tag -> viewModel.sortByTag()
                        }
                        dialog.dismiss()
                    }
                    dialog.show()
                    true
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return true;
            }

        },viewLifecycleOwner)
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

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(position){
            0 -> viewModel.sortByNew()
            1 -> viewModel.sortByOld()
            2 -> viewModel.sortByTag()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}