package com.glitchcraftlabs.qrstorage.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glitchcraftlabs.qrstorage.data.local.History
import com.glitchcraftlabs.qrstorage.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllScansViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _history: MutableLiveData<List<History>> = MutableLiveData()
    val history: LiveData<List<History>> = _history

    init {
        viewModelScope.launch{ _history.value = repository.getAllHistory() }
    }

    fun sortByNew() {
        viewModelScope.launch{ _history.value = repository.getAllHistory() }
    }

    fun sortByOld() {
        viewModelScope.launch{ _history.value = repository.getAllHistory(false) }
    }


}