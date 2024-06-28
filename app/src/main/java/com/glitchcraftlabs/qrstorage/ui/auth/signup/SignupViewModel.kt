package com.glitchcraftlabs.qrstorage.ui.auth.signup

import androidx.lifecycle.ViewModel
import com.glitchcraftlabs.qrstorage.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
): ViewModel() {

    fun signUp(email: String, password: String) = authRepository.signUp(email, password)
    fun signInWithGoogle(idToken: String) = authRepository.authWithGoogle(idToken)

}