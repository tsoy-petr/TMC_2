package com.hootor.tmc_2.domain.settings

import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend fun getSetting(): SettingsItem = settingsRepository.getSettings()

}