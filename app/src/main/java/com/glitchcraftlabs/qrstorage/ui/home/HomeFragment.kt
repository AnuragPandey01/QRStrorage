package com.glitchcraftlabs.qrstorage.ui.home

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
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
import com.glitchcraftlabs.qrstorage.util.Constants.FILE_SIZE_LIMIT
import com.glitchcraftlabs.qrstorage.util.QueryResult
import com.glitchcraftlabs.qrstorage.util.getFileMetaInfo
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
    private val progressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setTitle("Uploading File")
            setMessage("Please wait.." )
        }
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

        val menuHost = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu , menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if(menuItem.itemId == R.id.menu_logout){
                    viewModel.logout()
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAuthFragment())
                }
                if(menuItem.itemId == R.id.menu_setting){
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSettingFragment())
                }
                return true
            }

        },viewLifecycleOwner)
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
        viewModel.loadHistory(false)
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

        val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val (fileSize, fileName) = requireContext().getFileMetaInfo(it)
                if (fileSize > FILE_SIZE_LIMIT) {
                    Snackbar.make(
                        binding.root,
                        "file should be below ${FILE_SIZE_LIMIT / (1024 * 1024L)} mb",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return@let
                }
                binding.uploadedFileButton.visibility = View.VISIBLE
                binding.uploadFileButton.visibility = View.GONE
                binding.fileName.text = fileName
                viewModel.setSelectedFileUri(it)
            }
        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                R.id.textRadioButton -> {
                    binding.uploadFileButton.visibility = View.GONE
                    binding.fileStorageInfoTextView.visibility = View.GONE
                    binding.qrTextInputLayout.visibility = View.VISIBLE
                    viewModel.loadHistory(false)
                }
                R.id.fileRadioButton -> {
                    binding.uploadFileButton.visibility = View.VISIBLE
                    binding.fileStorageInfoTextView.visibility = View.VISIBLE
                    binding.qrTextInputLayout.visibility = View.GONE
                    viewModel.loadHistory(true)
                }
            }
        }

        binding.generateButton.setOnClickListener {
            val tag = binding.tagInput.text.toString()

            if(binding.radioGroup.checkedRadioButtonId == R.id.fileRadioButton){
                if(tag.isBlank() || viewModel.selectedFileUri.isNull()){
                    Snackbar.make(binding.root,
                        getString(R.string.please_fill_all_fields), Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if(viewModel.getCurrentUser() == null){
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAuthFragment())
                }

                viewModel.isEmailVerified().observe(viewLifecycleOwner){
                    when(it){
                        is QueryResult.Loading -> {}
                        is QueryResult.Success -> {
                            if(it.data == false){
                                showEmailVerificationDialog()
                                return@observe
                            }else{
                                viewModel.viewModelScope.launch {
                                    progressDialog.show()
                                    viewModel.uploadFile(tag, viewModel.selectedFileUri!!).observe(viewLifecycleOwner){
                                        when(it){
                                            is QueryResult.Loading -> {}
                                            is QueryResult.Success -> {
                                                insertHistory(tag, it.data.toString(), true)
                                                progressDialog.dismiss()
                                            }
                                            is QueryResult.Error -> {
                                                progressDialog.dismiss()
                                                Snackbar.make(binding.root, it.message ?: "something went wrong", Snackbar.LENGTH_SHORT)
                                                    .show()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is QueryResult.Error -> {
                            Snackbar.make(binding.root, it.message ?: "something went wrong", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }else{
                if(binding.tagInput.text.isNullOrBlank() || binding.qrTextInput.text.isNullOrBlank()){
                    Snackbar.make(binding.root,
                        getString(R.string.please_fill_all_fields), Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                insertHistory(tag, binding.qrTextInput.text.toString(),false)
            }

        }

        binding.quickScanButton.setOnClickListener {
            openScanner()
        }

        binding.allScansButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAllScansFragment())
        }

        binding.uploadFileButton.setOnClickListener {
            getContent.launch("*/*")
        }

        binding.clearFile.setOnClickListener {
            viewModel.clearSelectedFileUri()
            binding.uploadedFileButton.visibility = View.GONE
            binding.uploadFileButton.visibility = View.VISIBLE
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

    private fun insertHistory(tag:String, value: String, isFile: Boolean){
        lifecycleScope.launch {
            viewModel.insertGeneratedQR(
                tag = tag,
                value = value,
                isFile = isFile
            ).observe(viewLifecycleOwner){
                if(it is QueryResult.Error){
                    binding.tagInput.error = it.message
                }

                if(it is QueryResult.Success){
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToGeneratedQrFragment(
                            tag = tag,
                            qrData = value
                        )
                    )
                    binding.tagInput.text?.clear()
                    binding.qrTextInput.text?.clear()
                }
            }
        }
    }

    private fun showEmailVerificationDialog(){
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.email_verification_dialog_layout,null)
        val verifyButton = dialogView.findViewById<MaterialButton>(R.id.btnVerifyEmail)



        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        verifyButton.setOnClickListener {
            dialog.dismiss()
            viewModel.sendVerificationEmail().observe(viewLifecycleOwner){
                if(it is QueryResult.Success){
                    Snackbar.make(binding.root, "Verification email sent", Snackbar.LENGTH_SHORT).show()
                }
                if(it is QueryResult.Error){
                    Snackbar.make(binding.root, it.message ?: "unable to send verification email", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

fun Any?.isNull() = this == null