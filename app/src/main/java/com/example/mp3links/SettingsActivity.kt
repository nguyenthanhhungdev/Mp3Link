package com.example.mp3links

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anggrayudi.storage.SimpleStorageHelper
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.FileFullPath
import com.anggrayudi.storage.file.getAbsolutePath
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val TAG = "SettingsActivity"

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var storageHelper: SimpleStorageHelper

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storageHelper = SimpleStorageHelper(this)
        val settingsViewModel: SettingsViewModel by viewModels()
        this.settingsViewModel = settingsViewModel
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(TAG, "onCreate: repeatOnLifecycle STARTED")
                openUserSelectAppDataUriActivity(settingsViewModel)
            }
        }
        setContent {
            SettingsPage(viewModel = settingsViewModel)
        }
    }

    private fun CoroutineScope.openUserSelectAppDataUriActivity(settingsViewModel: SettingsViewModel) {
        settingsViewModel.openUserSelectAppDataUriActivityLiveEvent.onEach {
            Log.d(TAG, "openUserSelectAppDataUriActivity: received event")
            storageHelper.run {
                onFolderSelected = { _, folder ->
                    settingsViewModel.appDataDir(folder.getAbsolutePath(this@SettingsActivity))
                }
                openFolderPicker(
                    initialPath = FileFullPath(
                        this@SettingsActivity, it
                    ),
                )
            }
        }.launchIn(this)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, DocumentFileCompat.getAccessibleAbsolutePaths(this).toString())
    }
}