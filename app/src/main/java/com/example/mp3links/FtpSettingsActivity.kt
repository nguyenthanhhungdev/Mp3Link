package com.example.mp3links

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi

class FtpSettingsActivity : ComponentActivity() {
    private lateinit var ftpSettingsViewModel: FtpSettingsViewModel
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ftpSettingsViewModel: FtpSettingsViewModel by viewModels()
        this.ftpSettingsViewModel = ftpSettingsViewModel
        setContent {
            SettingsPage(viewModel = ftpSettingsViewModel)
        }
    }
}