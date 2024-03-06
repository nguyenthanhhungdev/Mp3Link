package com.example.mp3links

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


class FtpSettingsViewModel(private val ftpSettingsRepository: FtpSettingsRepository) : ViewModel() {
    suspend fun getFtpSettings() = ftpSettingsRepository.getFtpSettings()
    private val _stateFlow = MutableStateFlow<FtpSettings?>(null)
    val stateFlow: StateFlow<FtpSettings?> = _stateFlow.asStateFlow()
    fun sourceHost(sourceHost: String) = viewModelScope.launch {
        ftpSettingsRepository.updateFtpSettings(
            FtpSettingsRepository.SettingType.SourceHost, sourceHost
        )
        _stateFlow.value = getFtpSettings()
    }

    fun sourcePort(sourcePort: Int) = viewModelScope.launch {
        ftpSettingsRepository.updateFtpSettings(
            FtpSettingsRepository.SettingType.SourcePort, sourcePort
        )
        _stateFlow.value = getFtpSettings()
    }

    fun sourceUsername(sourceUsername: String) = viewModelScope.launch {
        ftpSettingsRepository.updateFtpSettings(
            FtpSettingsRepository.SettingType.SourceUsername, sourceUsername
        )
        _stateFlow.value = getFtpSettings()
    }

    fun sourcePassword(sourcePassword: String) = viewModelScope.launch {
        ftpSettingsRepository.updateFtpSettings(
            FtpSettingsRepository.SettingType.SourcePassword, sourcePassword
        )
        _stateFlow.value = getFtpSettings()
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FtpComposeApplication)
                FtpSettingsViewModel(application.ftpSettingsRepository)
            }
        }
    }
}

