package com.hootor.tmc_2.domain.settings

import javax.inject.Inject

class SetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
){
    suspend fun setSettings(settingsItem: SettingsItem) = settingsRepository.setSettings(settingsItem)
}