package com.example.mp3links

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "FtpSettingsViewModel"

class FtpSettingsViewModel(private val ftpSettingsRepository: FtpSettingsRepository) : ViewModel() {
    private suspend fun getFtpSettings() = ftpSettingsRepository.getFtpSettings()
    private val _stateFlow = MutableStateFlow(FtpSettingsSerializer.defaultValue)
    val stateFlow: StateFlow<FtpSettings> = _stateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            Log.d(TAG, "init: starter settings ${getFtpSettings()}")
            _stateFlow.value = getFtpSettings()
        }
    }

    fun sourceHost(sourceHost: String) = viewModelScope.launch {
        Log.d(TAG, "sourceHost: update setting to $sourceHost")
        ftpSettingsRepository.updateFtpSettings(
            SettingType.SourceHost, sourceHost
        )
        _stateFlow.value = getFtpSettings()
    }

    fun sourcePort(sourcePort: Int) = viewModelScope.launch {
        Log.d(TAG, "sourceHost: update setting to $sourcePort")
        ftpSettingsRepository.updateFtpSettings(
            SettingType.SourcePort, sourcePort
        )
        _stateFlow.value = getFtpSettings()
    }

    fun sourceUsername(sourceUsername: String) = viewModelScope.launch {
        Log.d(TAG, "sourceHost: update setting to $sourceUsername")
        ftpSettingsRepository.updateFtpSettings(
            SettingType.SourceUsername, sourceUsername
        )
        _stateFlow.value = getFtpSettings()
    }

    fun sourcePassword(sourcePassword: String) = viewModelScope.launch {
        Log.d(TAG, "sourceHost: update setting to $sourcePassword")
        ftpSettingsRepository.updateFtpSettings(
            SettingType.SourcePassword, sourcePassword
        )
        _stateFlow.value = getFtpSettings()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FtpComposeApplication)
                FtpSettingsViewModel(application.ftpSettingsRepository)
            }
        }

    }

    enum class DialogEnum {
        DIALOG_NOT_SHOWN, DIALOG_SOURCE_HOST, DIALOG_SOURCE_PORT, DIALOG_SOURCE_USERNAME, DIALOG_SOURCE_PASSWORD
    }
}

