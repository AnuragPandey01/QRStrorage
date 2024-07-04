package com.glitchcraftlabs.qrstorage.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide.init
import com.glitchcraftlabs.qrstorage.R
import com.glitchcraftlabs.qrstorage.data.repository.Repository
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllScansViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val history = repository.history
    var checkedRadioButtonId = R.id.radio_recently_added

    init {
        viewModelScope.launch{ repository.getAllHistory(Query.Direction.ASCENDING) }
    }

    fun sortByNew() {
        viewModelScope.launch{ repository.getAllHistory(Query.Direction.ASCENDING) }
        checkedRadioButtonId = R.id.radio_recently_added
    }

    fun sortByOld() {
        viewModelScope.launch{ repository.getAllHistory(Query.Direction.DESCENDING) }
        checkedRadioButtonId = R.id.radio_oldest_first
    }

    fun sortByTag() {
        viewModelScope.launch{ repository.getAllHistoryOrderByTag() }
        checkedRadioButtonId = R.id.radio_tag
    }

    fun searchByTag(tag: String) {
        viewModelScope.launch{ repository.searchByTag(tag) }
    }


}