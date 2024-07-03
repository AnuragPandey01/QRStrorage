package com.glitchcraftlabs.qrstorage.ui.home

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glitchcraftlabs.qrstorage.data.repository.AuthRepository
import com.glitchcraftlabs.qrstorage.data.repository.Repository
import com.glitchcraftlabs.qrstorage.util.QueryResult
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: Repository,
    private val authRepository: AuthRepository
): ViewModel() {

    val history = repository.history
    private var _selectedFileUri : Uri? = null
    val selectedFileUri: Uri?
        get() = _selectedFileUri

    init{
        loadHistory()
    }

    fun loadHistory(){
        viewModelScope.launch {
            repository.getRecentWithLimit(5)
        }
    }

    suspend fun insertGeneratedQR(tag: String, value: String): LiveData<QueryResult<Nothing?>> {
        val res = repository.insertHistory(
            tag = tag,
            value = value,
            isGenerated = true
        )
        loadHistory()
        return res
    }

    fun insertScan(qrResult: Barcode){
        viewModelScope.launch {
            repository.insertHistory(
                value = qrResult.rawValue.orEmpty(),
                tag = UUID.randomUUID().toString(),
                isGenerated = false
            )
            loadHistory()
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun setSelectedFileUri(uri: Uri){
        _selectedFileUri = uri
    }

    fun clearSelectedFileUri(){
        _selectedFileUri = null
    }

    suspend fun uploadFile(tag:String, fileUri: Uri) = repository.uploadFile(tag, fileUri,authRepository.getCurrentUser()!!)

    fun isEmailVerified() = authRepository.getVerificationStatus()

    fun sendVerificationEmail() = authRepository.sendVerificationEmail()

    fun getCurrentUser() = authRepository.getCurrentUser()

}