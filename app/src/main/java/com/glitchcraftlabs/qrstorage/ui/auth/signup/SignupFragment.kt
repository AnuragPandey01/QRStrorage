package com.glitchcraftlabs.qrstorage.ui.auth.signup

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.glitchcraftlabs.qrstorage.R
import com.glitchcraftlabs.qrstorage.databinding.FragmentSignupBinding
import com.glitchcraftlabs.qrstorage.util.QueryResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignupFragment : Fragment(R.layout.fragment_signup) {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val viewmodel by viewModels<SignupViewModel>()
    private val progressDialog by lazy { ProgressDialog(requireContext()).apply { setTitle("Loading..") } }
    private lateinit var googleSignInClient : GoogleSignInClient
    private val RC_SIGN_IN = 123

    override fun onViewCreated(view: View, savedInstanceState: Bundle   ?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignupBinding.bind(view)

        val googleSignInOptions  = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)

        registerListeners()
    }

    private fun registerListeners() {
        binding.signupButton.setOnClickListener {
            val email = binding.emailLayout.editText?.text.toString()
            val password = binding.passwordLayout.editText?.text.toString()
            val confirmPassword = binding.confirmPasswordLayout.editText?.text.toString()

            if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                Snackbar.make(requireView(), getString(R.string.please_fill_all_fields), Snackbar.LENGTH_SHORT).show()
            }else if(password != confirmPassword){
                Snackbar.make(requireView(), getString(R.string.password_mismatch), Snackbar.LENGTH_SHORT).show()
            }else if(!binding.checkboxTerms.isChecked){
                Snackbar.make(requireView(), getString(R.string.please_accept_terms), Snackbar.LENGTH_SHORT).show()
            }else{
                viewmodel.viewModelScope.launch {
                    observeAuthState(viewmodel.signUp(email, password))
                }
            }
        }

        binding.googleButton.setOnClickListener {
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailLayout.editText?.text.toString()
            val password = binding.passwordLayout.editText?.text.toString()
            if (email.isBlank() || password.isBlank()) {
                Snackbar.make(requireView(), getString(R.string.please_fill_all_fields), Snackbar.LENGTH_SHORT).show()
            }else{
                viewmodel.viewModelScope.launch {
                    observeAuthState(viewmodel.login(email, password))
                }
            }
        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.signupRadioButton -> {
                    binding.confirmPasswordLayout.visibility = View.VISIBLE
                    binding.signupButton.visibility = View.VISIBLE
                    binding.loginButton.visibility = View.GONE
                    binding.checkboxTerms.visibility = View.VISIBLE
                }
                R.id.loginRadioButton -> {
                    binding.confirmPasswordLayout.visibility = View.GONE
                    binding.signupButton.visibility = View.GONE
                    binding.loginButton.visibility = View.VISIBLE
                    binding.checkboxTerms.visibility = View.GONE
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account = task.result
                val idToken = account.idToken
                observeAuthState(viewmodel.signInWithGoogle(idToken!!))
            }catch (e: Exception){
                Snackbar.make(requireView(), e.message!!, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeAuthState(state: LiveData<QueryResult<FirebaseUser>>) {
        state.observe(viewLifecycleOwner) { result ->
            when (result) {
                is QueryResult.Loading -> {
                    progressDialog.show()
                }
                is QueryResult.Success -> {
                    progressDialog.dismiss()
                    Snackbar.make(requireView(), "Success", Snackbar.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_signupFragment_to_homeFragment)
                }
                is QueryResult.Error -> {
                    progressDialog.dismiss()
                    Snackbar.make(requireView(), result.message!!, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}