package com.example.mp3links

import android.net.InetAddresses
import android.os.Build
import android.util.Log
import android.util.Patterns
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

private const val TAG = "FtpSettingsRepository"

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    val ftpSettingsFlow = dataStore.data.map { preferences ->
        FtpSettings(
            preferences[FtpSettings.sourceHostPreferencesKey] ?: FtpSettings.DEFAULT_SOURCE_HOST,
            preferences[FtpSettings.sourcePortPreferencesKey] ?: FtpSettings.DEFAULT_SOURCE_PORT,
            preferences[FtpSettings.sourceUsernamePreferencesKey]
                ?: FtpSettings.DEFAULT_SOURCE_USERNAME,
            preferences[FtpSettings.sourcePasswordPreferencesKey]
                ?: FtpSettings.DEFAULT_SOURCE_PASSWORD
        )
    }.distinctUntilChanged()

    suspend fun getFtpSettings(): FtpSettings = ftpSettingsFlow.first()


    suspend fun updateFtpSettings(type: SettingType, value: String) {
        saveFtpSettings(getFtpSettings().let {
            when (type) {
                SettingType.SourceHost -> it.copy(sourceHost = value)
                SettingType.SourceUsername -> it.copy(sourceUsername = value)
                SettingType.SourcePassword -> it.copy(sourcePassword = value)
                else -> {
                    Log.wtf(
                        TAG, "Updating wrong setting value type (type=$type value=$value)"
                    )
                    return
                }
            }
        })
    }

    suspend fun updateFtpSettings(type: SettingType, value: Int) {
        saveFtpSettings(getFtpSettings().let {
            when (type) {
                SettingType.SourcePort -> it.copy(sourcePort = value)
                else -> {
                    Log.wtf(
                        TAG, "Updating wrong setting value type (type=$type value=$value)"
                    )
                    return
                }
            }
        })
    }

    private suspend fun saveFtpSettings(ftpSettings: FtpSettings) {
        Log.d(TAG, "saveFtpSettings: saving with DataStore, value $ftpSettings")
        dataStore.edit { settings ->
            settings[FtpSettings.sourceHostPreferencesKey] = ftpSettings.sourceHost
            settings[FtpSettings.sourcePortPreferencesKey] = ftpSettings.sourcePort
            settings[FtpSettings.sourceUsernamePreferencesKey] = ftpSettings.sourceUsername
            settings[FtpSettings.sourcePasswordPreferencesKey] = ftpSettings.sourcePassword
        }
    }

    private val storageSettingsFlow = dataStore.data.map { preferences ->
        StorageSettings(
            preferences[StorageSettings.appDataViewDirPreferencesKey]
                ?: StorageSettings.DEFAULT_APPDATA_DIR,
            preferences[StorageSettings.appDataUriPreferencesKey]
                ?: StorageSettings.DEFAULT_APPDATA_DIR
        )
    }.distinctUntilChanged()

    suspend fun getStorageSettings() = storageSettingsFlow.first()
    suspend fun updateAppDataUriSettings(viewDir: String, uri: String) {
        saveStorageSettings(
            getStorageSettings().copy(
                appDataViewDir = viewDir, appDataUri = uri
            )
        )
    }

    private suspend fun saveStorageSettings(storageSettings: StorageSettings) {
        Log.d(TAG, "saveStorageSettings: saving with DataStore, value $storageSettings")
        dataStore.edit { settings ->
            settings[StorageSettings.appDataViewDirPreferencesKey] = storageSettings.appDataViewDir
        }
    }
}

@Serializable
data class FtpSettings(
    val sourceHost: String,
    val sourcePort: Int,
    val sourceUsername: String,
    val sourcePassword: String
) {
    companion object {
        const val DEFAULT_SOURCE_HOST = "10.0.2.2"
        const val DEFAULT_SOURCE_PORT = 2121
        const val DEFAULT_SOURCE_USERNAME = "username"
        const val DEFAULT_SOURCE_PASSWORD = "password"
        val defaultValue = FtpSettings(
            DEFAULT_SOURCE_HOST,
            DEFAULT_SOURCE_PORT,
            DEFAULT_SOURCE_USERNAME,
            DEFAULT_SOURCE_PASSWORD
        )
        val sourceHostPreferencesKey = stringPreferencesKey("source_host")
        val sourcePortPreferencesKey = intPreferencesKey("source_port")
        val sourceUsernamePreferencesKey = stringPreferencesKey("source_username")
        val sourcePasswordPreferencesKey = stringPreferencesKey("source_password")
    }
}


sealed class SettingType {
    sealed interface UserInputSettings {
        val validator: (String) -> Boolean
    }

    data object SourceHost : SettingType(), UserInputSettings {
        @RequiresApi(Build.VERSION_CODES.Q)
        override val validator: (String) -> Boolean = { text ->
            InetAddresses.isNumericAddress(text) || Patterns.WEB_URL.matcher(text)
                .matches() || Patterns.IP_ADDRESS.matcher(text).matches()
        }
    }

    data object SourcePort : SettingType(), UserInputSettings {
        override val validator: (String) -> Boolean = { text ->
            (text.toUIntOrNull() ?: 0u) in 1u..65535u
        }
    }

    data object SourceUsername : SettingType(), UserInputSettings {
        override val validator: (String) -> Boolean =
            { text -> text.isNotEmpty() && text.all { char -> char.isLetterOrDigit() } }
    }

    data object SourcePassword : SettingType(), UserInputSettings {
        override val validator: (String) -> Boolean =
            { text -> text.all { char -> char.code in 32..127 } } // printable ascii
    }

    data object AppDataUri : SettingType()
}

@Serializable
data class StorageSettings(val appDataViewDir: String, val appDataUri: String) {
    companion object {
        const val DEFAULT_APPDATA_DIR = "/"
        val defaultValue = StorageSettings(DEFAULT_APPDATA_DIR, DEFAULT_APPDATA_DIR)
        val appDataViewDirPreferencesKey = stringPreferencesKey("appdata_viewdir")
        val appDataUriPreferencesKey = stringPreferencesKey("appdata_uri")
    }
}