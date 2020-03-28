package com.comida.indice.syncyourlights.helper

import android.content.Context
import android.content.SharedPreferences

class SharedPrefsController(context: Context) {

    private val sharedPref: SharedPreferences = context.getSharedPreferences(
        Companion.PREF_NAME,
        Companion.PRIVATE_MODE
    )

    fun saveLastIp(ip: String) {
        sharedPref.edit().apply {
            putString(IP_NAME, ip)
            apply()
        }
    }

    fun saveLastUserName(userName: String) {
        sharedPref.edit().apply {
            putString(USER_NAME, userName)
            apply()
        }
    }

    fun getLastIp(): String = sharedPref.getString(IP_NAME, "") ?: ""

    fun getLastUserName(): String = sharedPref.getString(USER_NAME, "") ?: ""

    companion object {
        private const val PRIVATE_MODE = 0
        private const val PREF_NAME = "SyncYourLightsPrefs"
        private const val IP_NAME = "ipAddress"
        private const val USER_NAME = "myUserName"
    }
}