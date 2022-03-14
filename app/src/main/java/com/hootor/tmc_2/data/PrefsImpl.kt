package com.hootor.tmc_2.data

import android.content.SharedPreferences
import com.hootor.tmc_2.domain.settings.SettingsItem
import com.hootor.tmc_2.services.ServiceFactory
import okhttp3.Credentials

interface Prefs {
    fun setSettings(settingsItem: SettingsItem)
    fun getSettings(): SettingsItem
    fun getUrlServer():String
    fun getCredentials() : String
}


class PrefsImpl(
    private val prefs: SharedPreferences
) : Prefs {

    companion object {
        const val SERVER_URL_KEY = "SERVER_URL_KEY"
        const val SERVER_PORT_KEY = "SERVER_PORT_KEY"
        const val USER_NAME_KEY = "USER_NAME_KEY"
        const val PASS_KEY = "PASS_KEY"
    }

    override fun setSettings(settingsItem: SettingsItem) {
        prefs.edit().apply {
            putString(SERVER_URL_KEY, settingsItem.serverUrl)
            putString(SERVER_PORT_KEY, settingsItem.serverPort)
            putString(USER_NAME_KEY, settingsItem.userName)
            putString(PASS_KEY, settingsItem.userPass)
        }.apply()
    }

    override fun getSettings() = SettingsItem(
        serverUrl = prefs.getString(SERVER_URL_KEY, ""),
        serverPort = prefs.getString(SERVER_PORT_KEY, ""),
        userName = prefs.getString(USER_NAME_KEY, ""),
        userPass = prefs.getString(PASS_KEY, "")
    )

    override fun getUrlServer(): String {
        val url = prefs.getString(SERVER_URL_KEY, "") ?: ServiceFactory.BASE_URL
        val port = prefs.getString(SERVER_PORT_KEY, "") ?: 80
        return "$url:$port"
    }

    override fun getCredentials() = Credentials.basic(prefs.getString(USER_NAME_KEY, "") ?: "", prefs.getString(PASS_KEY, "") ?:"")

}