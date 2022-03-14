package com.hootor.tmc_2.screens.main.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.hootor.tmc_2.App
import com.hootor.tmc_2.R
import com.hootor.tmc_2.databinding.FragmentSettingsBinding
import com.hootor.tmc_2.di.ViewModelFactory
import com.hootor.tmc_2.utils.findTopNavController
import com.hootor.tmc_2.utils.getTrimText
import com.hootor.tmc_2.utils.observeEvent
import javax.inject.Inject

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding

    private lateinit var viewModel: SettingsViewModel

    private val args by navArgs<SettingsFragmentArgs>()

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as App).component
    }

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)

        binding.saveSettingsButton.setOnClickListener {
            viewModel.setSettings(
                serverUrl = binding.serverTextInput.getTrimText(),
                serverPort = binding.serverPortTextInput.getTrimText(),
                userName = binding.userNameTextInput.getTrimText(),
                userPass = binding.userPasswordTextInput.getTrimText(),
                fromTabs = isFromTabs()
            )
        }

        viewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]

        launchReadSettings()
        observeNavigateToTabsEvent()

    }

    private fun launchReadSettings() {
        viewModel.settings.observeEvent(viewLifecycleOwner) {
            binding.serverEditText.setText(it.serverUrl)
            binding.serverPortEditText.setText(it.serverPort)
            binding.userNameEditText.setText(it.userName)
            binding.userPasswordEditText.setText(it.userPass)
        }
    }

    private fun observeNavigateToTabsEvent() =
        viewModel.navigateToTabsEvent.observeEvent(viewLifecycleOwner) {
            findTopNavController().navigate(R.id.action_settingsFragmentLaunch_to_tabsFragment)
        }

    private fun isFromTabs() =
        try {
            args.fromTabs
        } catch (e: Exception) {
            false
        }


}