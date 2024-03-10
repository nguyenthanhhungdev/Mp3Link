package com.example.mp3links

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val SETTINGS_NAME = "settings.json"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(SETTINGS_NAME)

class FtpComposeApplication : Application() {
    lateinit var settingsRepository: SettingsRepository
    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(dataStore)
    }
}