package com.glitchcraftlabs.qrstorage.ui.setting

import android.app.ProgressDialog
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.glitchcraftlabs.qrstorage.R
import com.glitchcraftlabs.qrstorage.databinding.FragmentSettingBinding
import com.glitchcraftlabs.qrstorage.util.QueryResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : Fragment(R.layout.fragment_setting) {

    private val viewModel: SettingViewModel by viewModels()
    private  var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private val progressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setTitle("Loading..")
            setMessage("Please wait" )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingBinding.bind(view)

        binding.apply {
            email.setText(viewModel.userEmail)
            if(viewModel.getProvider() == "Google"){
                btnEmailProvider.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.ic_google, null))
            }else{
                viewModel.isEmailVerified().observe(viewLifecycleOwner){
                    when(it){
                        is QueryResult.Loading -> progressDialog.show()
                        is QueryResult.Success -> {
                            progressDialog.dismiss()
                            if (it.data == true) {
                                Toast.makeText(requireContext(), "tur", Toast.LENGTH_SHORT).show()
                                btnEmailProvider.setImageDrawable(ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_check
                                ))
                            } else {
                                Toast.makeText(requireContext(), "fal", Toast.LENGTH_SHORT).show()

                                btnEmailProvider.setImageDrawable(ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_not_verified
                                ))

                                btnEmailProvider.setOnClickListener {
                                    showEmailVerificationDialog()
                                }
                            }
                        }
                        is QueryResult.Error -> {
                            progressDialog.dismiss()
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
}