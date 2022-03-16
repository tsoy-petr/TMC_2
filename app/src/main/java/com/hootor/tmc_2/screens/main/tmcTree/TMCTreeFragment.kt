package com.hootor.tmc_2.screens.main.tmcTree

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hootor.tmc_2.App
import com.hootor.tmc_2.R
import com.hootor.tmc_2.databinding.FragmentScanningBinding
import com.hootor.tmc_2.databinding.FragmentTmcTreeBinding
import com.hootor.tmc_2.di.ViewModelFactory
import com.hootor.tmc_2.screens.main.scanning.qr.ScanningViewModel
import com.hootor.tmc_2.screens.main.scanning.tmc.ScanningTMCViewModel
import javax.inject.Inject

class TMCTreeFragment : Fragment(R.layout.fragment_tmc_tree) {

    private lateinit var binding: FragmentTmcTreeBinding

    private lateinit var viewModel: TMCTreeViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as App).component
    }

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTmcTreeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, viewModelFactory)[TMCTreeViewModel::class.java]

        return binding.root
    }
}