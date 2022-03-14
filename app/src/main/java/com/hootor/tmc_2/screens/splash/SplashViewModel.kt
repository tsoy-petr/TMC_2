package com.hootor.tmc_2.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hootor.tmc_2.domain.settings.GetSettingsUseCase
import com.hootor.tmc_2.domain.settings.SetSettingsUseCase
import com.hootor.tmc_2.domain.settings.SettingsDoneUseCase
import com.hootor.tmc_2.utils.MutableLiveEvent
import com.hootor.tmc_2.utils.publishEvent
import com.hootor.tmc_2.utils.share
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    private val settingsDoneUseCase: SettingsDoneUseCase,
) : ViewModel() {

    private val _launchMainScreenEvent = MutableLiveEvent<Boolean>()
    val launchMainScreenEvent = _launchMainScreenEvent.share()

    init {
        viewModelScope.launch {
            _launchMainScreenEvent.publishEvent(settingsDoneUseCase.settingsDone())
        }
    }
}