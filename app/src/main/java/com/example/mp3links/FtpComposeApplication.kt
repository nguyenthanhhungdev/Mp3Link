package com.example.mp3links

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore

private const val FTP_SETTINGS_NAME = "ftp_settings.json"
val Context.dataStore: DataStore<FtpSettings> by dataStore(
    FTP_SETTINGS_NAME, FtpSettingsSerializer
)

class FtpComposeApplication : Application() {
    lateinit var ftpSettingsRepository: FtpSettingsRepository
    override fun onCreate() {
        super.onCreate()
        ftpSettingsRepository = FtpSettingsRepository(dataStore)
    }
}