package com.glitchcraftlabs.qrstorage.ui.generated_qr

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.glitchcraftlabs.qrstorage.R
import com.glitchcraftlabs.qrstorage.databinding.FragmentGeneratedQrBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GeneratedQrFragment : Fragment(R.layout.fragment_generated_qr) {

    private val viewModel: GeneratedQrViewModel by viewModels()
    private var _binding: FragmentGeneratedQrBinding? = null
    private val binding: FragmentGeneratedQrBinding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGeneratedQrBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}