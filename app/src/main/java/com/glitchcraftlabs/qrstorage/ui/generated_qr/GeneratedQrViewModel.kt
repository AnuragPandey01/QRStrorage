package com.glitchcraftlabs.qrstorage.ui.generated_qr

import androidx.lifecycle.ViewModel
import com.glitchcraftlabs.qrstorage.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GeneratedQrViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {


    suspend fun updateHistory(oldTag: String, newTag:String) =
        repository.updateTag(
            oldTag = oldTag,
            newTag = newTag
        )
}