package com.hootor.tmc_2.screens.main.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hootor.tmc_2.domain.settings.GetSettingsUseCase
import com.hootor.tmc_2.domain.settings.SetSettingsUseCase
import com.hootor.tmc_2.domain.settings.SettingsItem
import com.hootor.tmc_2.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val setSettingsUseCase: SetSettingsUseCase,
) : ViewModel() {

    private val _settings = MutableLiveEvent<SettingsItem>()
    val settings = _settings.share()

    private val _navigateToTabsEvent = MutableUnitLiveEvent()
    val navigateToTabsEvent = _navigateToTabsEvent.share()

    init {
        viewModelScope.launch {
            _settings.publishEvent(withContext(Dispatchers.IO) {
                return@withContext getSettingsUseCase.getSetting()
            })
        }
    }

    fun setSettings(
        serverUrl: String,
        serverPort: String,
        userName: String,
        userPass: String,
        fromTabs: Boolean,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                setSettingsUseCase.setSettings(
                    SettingsItem(serverUrl, serverPort, userName, userPass)
                )
            }
            if (!fromTabs) launchTabsScreen()
        }
    }

    private fun launchTabsScreen() = _navigateToTabsEvent.publishEvent()
}