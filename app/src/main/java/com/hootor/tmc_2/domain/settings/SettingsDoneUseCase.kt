package com.hootor.tmc_2.domain.settings

import javax.inject.Inject

class SettingsDoneUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend fun settingsDone() = settingsRepository.settingsDone()
}