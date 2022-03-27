package com.hootor.tmc_2.screens.main.tmcTree

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.hootor.tmc_2.App
import com.hootor.tmc_2.R
import com.hootor.tmc_2.databinding.FragmentTmcTreeBinding
import com.hootor.tmc_2.di.ViewModelFactory
import com.hootor.tmc_2.domain.tmc.TMCTree
import com.hootor.tmc_2.screens.main.tmcTree.State.*
import com.hootor.tmc_2.screens.utils.PowerMenuUtils
import com.hootor.tmc_2.utils.publishResults
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class TMCTreeFragment : Fragment(R.layout.fragment_tmc_tree) {

    private lateinit var binding: FragmentTmcTreeBinding
    private lateinit var adapter: TMCTreeAdapter
    private lateinit var viewModel: TMCTreeViewModel
    private lateinit var iconMenu: PowerMenu

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as App).component
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = TMCTreeAdapter(::onClickItem)
    }

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentTmcTreeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, viewModelFactory)[TMCTreeViewModel::class.java]

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { viewState ->
                    when (viewState.state) {
                        Loading -> {
                            binding.rvTreeTMC.isVisible = false
                            binding.progressBar.isVisible = true
                        }
                        is Error -> {
                            binding.rvTreeTMC.isVisible = false
                            binding.progressBar.isVisible = false
                            Toast.makeText(requireContext(), viewState.state.message, Toast.LENGTH_SHORT).show()
                        }
                        Success -> {
                            binding.rvTreeTMC.isVisible = true
                            binding.progressBar.isVisible = false
                            if (viewState.root != null) {
                                adapter.setRoot(viewState.root)
                            }
                        }
                        Empty -> {
                            binding.rvTreeTMC.isVisible = false
                            binding.progressBar.isVisible = false
                            Toast.makeText(requireContext(), getString(R.string.textEmptydata), Toast.LENGTH_SHORT).show()
                        }
                        Init -> {
                            binding.rvTreeTMC.isVisible = false
                            binding.progressBar.isVisible = true
                        }
                    }
                }
            }
        }

        binding.rvTreeTMC.setAdapter(adapter)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iconMenu = PowerMenuUtils.getIconPowerMenu(requireContext(),
            viewLifecycleOwner,
            onIconMenuItemClickListener)
        requireArguments().getString(KEY_ARGS_QR_CODE)?.let { qrCode ->
            viewModel.fetch(qrCode)
        }

    }

    private val onIconMenuItemClickListener =
        OnMenuItemClickListener<PowerMenuItem> { position, item ->
            iconMenu.dismiss()
            when (position) {
                0 -> {
                    viewModel.getCurrentItem()?.let { itemTree ->
                        publishResults(KEY_ARGS_QR_CODE, itemTree.uuid)
                        findNavController().popBackStack()
                    }
                }
            }
        }

    private fun onClickItem(item: TMCTree, itemView: View) {
        if (iconMenu.isShowing) {
            iconMenu.dismiss()
            return
        }
        viewModel.setCurrentItem(item)
        iconMenu.showAsDropDown(itemView, -370, 0)
    }

    companion object {
        const val KEY_ARGS_QR_CODE = "qrCode"
    }

}