package com.glitchcraftlabs.qrstorage.ui.auth

import androidx.lifecycle.ViewModel
import com.glitchcraftlabs.qrstorage.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
): ViewModel() {

    fun signUp(email: String, password: String) = authRepository.signUp(email, password)

    fun signInWithGoogle(idToken: String) = authRepository.authWithGoogle(idToken)

    fun login(email: String, password: String) = authRepository.signIn(email, password)

    fun getCurrentUser() = authRepository.getCurrentUser()

    fun signInAnonymously() = authRepository.signInAnonymously()

}