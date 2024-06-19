package com.glitchcraftlabs.qrstorage.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glitchcraftlabs.qrstorage.data.local.History
import com.glitchcraftlabs.qrstorage.data.repository.Repository
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {

    private val _history: MutableLiveData<List<History>> = MutableLiveData(emptyList())
    val history: LiveData<List<History>>
        get() = _history

    init{
        loadHistory()
    }

    private fun loadHistory(){
        viewModelScope.launch {
            _history.value = repository.getAllHistory()
        }
    }

    fun insertGeneratedQR(tag: String, value: String){
        viewModelScope.launch {
            repository.insertHistory(
                tag = tag,
                value = value,
                isGenerated = true
            )
            loadHistory()
        }
    }

    fun insertScan(qrResult: Barcode){
        viewModelScope.launch {
            repository.insertHistory(
                value = qrResult.rawValue.orEmpty(),
                tag = qrResult.displayValue.orEmpty().take(10),
                isGenerated = false
            )
            loadHistory()
        }
    }

}