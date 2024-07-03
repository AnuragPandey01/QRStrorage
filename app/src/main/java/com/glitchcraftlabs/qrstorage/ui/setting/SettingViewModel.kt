package com.glitchcraftlabs.qrstorage.ui.setting

import androidx.lifecycle.ViewModel
import com.glitchcraftlabs.qrstorage.data.repository.AuthRepository
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    var userEmail : String? = authRepository.getCurrentUser()?.email

    fun getProvider() : String {
        val providerId = authRepository.getCurrentUser()?.providerData?.get(1)?.providerId
        return if(providerId == GoogleAuthProvider.PROVIDER_ID){
            "Google"
        }else{
            "Email"
        }
    }

    fun sendVerificationEmail() = authRepository.sendVerificationEmail()

    fun isEmailVerified() = authRepository.getVerificationStatus()
}