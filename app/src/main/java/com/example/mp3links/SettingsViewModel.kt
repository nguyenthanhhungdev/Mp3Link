package com.example.mp3links

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val TAG = "SettingsViewModel"

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    private val _openUserSelectAppDataUriActivityLiveEvent =
        Channel<String>(capacity = Channel.BUFFERED)
    val openUserSelectAppDataUriActivityLiveEvent =
        _openUserSelectAppDataUriActivityLiveEvent.receiveAsFlow()

    private suspend fun getFtpSettings() = settingsRepository.getFtpSettings()
    private val _ftpSettingsFlow = MutableStateFlow(FtpSettings.defaultValue)
    val ftpSettingsFlow: StateFlow<FtpSettings> = _ftpSettingsFlow.asStateFlow()
    private suspend fun getStorageSettings() = settingsRepository.getStorageSettings()
    private val _storageSettingsFlow = MutableStateFlow(StorageSettings.defaultValue)
    val storageSettingsFlow: StateFlow<StorageSettings> = _storageSettingsFlow.asStateFlow()

    init {
        viewModelScope.launch {
            Log.d(TAG, "init: starter settings ${getFtpSettings()} ${getStorageSettings()}")
            _ftpSettingsFlow.value = getFtpSettings()
            _storageSettingsFlow.value = getStorageSettings()
        }
    }

    fun sourceHost(sourceHost: String) = viewModelScope.launch {
        Log.d(TAG, "sourceHost: update setting to $sourceHost")
        settingsRepository.updateFtpSettings(
            SettingType.SourceHost, sourceHost
        )
        _ftpSettingsFlow.value = getFtpSettings()
    }

    fun sourcePort(sourcePort: Int) = viewModelScope.launch {
        Log.d(TAG, "sourceHost: update setting to $sourcePort")
        settingsRepository.updateFtpSettings(
            SettingType.SourcePort, sourcePort
        )
        _ftpSettingsFlow.value = getFtpSettings()
    }

    fun sourceUsername(sourceUsername: String) = viewModelScope.launch {
        Log.d(TAG, "sourceHost: update setting to $sourceUsername")
        settingsRepository.updateFtpSettings(
            SettingType.SourceUsername, sourceUsername
        )
        _ftpSettingsFlow.value = getFtpSettings()
    }

    fun sourcePassword(sourcePassword: String) = viewModelScope.launch {
        Log.d(TAG, "sourceHost: update setting to $sourcePassword")
        settingsRepository.updateFtpSettings(
            SettingType.SourcePassword, sourcePassword
        )
        _ftpSettingsFlow.value = getFtpSettings()
    }

    fun requestAppDataUri() = viewModelScope.launch {
        _openUserSelectAppDataUriActivityLiveEvent.send(settingsRepository.getStorageSettings().appDataViewDir)
    }

    fun appDataPath(viewDir: String, uri: Uri) = viewModelScope.launch {
        Log.d(TAG, "appDataUri: update setting to $uri ($viewDir)")
        settingsRepository.updateAppDataUriSettings(
            viewDir, uri.toString()
        )
        _storageSettingsFlow.value = getStorageSettings()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FtpComposeApplication)
                SettingsViewModel(application.settingsRepository)
            }
        }

    }

    enum class DialogEnum {
        DIALOG_NOT_SHOWN, DIALOG_SOURCE_HOST, DIALOG_SOURCE_PORT, DIALOG_SOURCE_USERNAME, DIALOG_SOURCE_PASSWORD
    }
}

