package com.glitchcraftlabs.qrstorage.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glitchcraftlabs.qrstorage.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllScansViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val history = repository.history

    init {
        viewModelScope.launch{ repository.getAllHistory() }
    }

    fun sortByNew() {
        viewModelScope.launch{ repository.getAllHistory() }
    }

    fun sortByOld() {
        viewModelScope.launch{ repository.getAllHistory(false) }
    }

    fun sortByTag() {
        viewModelScope.launch{ repository.getAllHistoryOrderByTag() }
    }

    fun searchByTag(tag: String) {
        viewModelScope.launch{ repository.searchByTag(tag) }
    }


}