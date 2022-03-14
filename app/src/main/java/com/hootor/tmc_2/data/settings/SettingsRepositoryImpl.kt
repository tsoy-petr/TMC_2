package com.hootor.tmc_2.data.settings

import com.hootor.tmc_2.data.Prefs
import com.hootor.tmc_2.domain.settings.SettingsItem
import com.hootor.tmc_2.domain.settings.SettingsRepository
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val prefs: Prefs
): SettingsRepository {

    override suspend fun getSettings(): SettingsItem = prefs.getSettings()

    override suspend fun setSettings(settingsItem: SettingsItem) = prefs.setSettings(settingsItem)

    override suspend fun settingsDone(): Boolean {
        val currSettings = prefs.getSettings()
        return (currSettings.serverUrl?.isNotEmpty() == true
                && currSettings.serverPort?.isNotEmpty() == true
                && currSettings.userName?.isNotEmpty() == true
                && currSettings.userPass?.isNotEmpty() == true)
    }
}