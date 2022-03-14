package com.hootor.tmc_2.domain.settings

interface SettingsRepository {

    suspend fun getSettings():SettingsItem

    suspend fun setSettings(settingsItem: SettingsItem)

    suspend fun settingsDone(): Boolean
}