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
        val currUser = authRepository.getCurrentUser()!!
        val providerId = currUser.providerData[0]?.providerId
        return if(providerId == GoogleAuthProvider.PROVIDER_ID){
            "Google"
        }else{
            "Email"
        }
    }

    fun isAnonymous() = authRepository.getCurrentUser()!!.isAnonymous

    fun sendVerificationEmail() = authRepository.sendVerificationEmail()

    fun isEmailVerified() = authRepository.getVerificationStatus()
}