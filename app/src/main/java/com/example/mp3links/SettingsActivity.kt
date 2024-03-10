package com.example.mp3links

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.getAbsolutePath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val TAG = "SettingsActivity"

class SettingsActivity : ComponentActivity() {
    private lateinit var settingsViewModel: SettingsViewModel
    private val directoryPickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
            it?.let {
                try {
                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    settingsViewModel.appDataPath(
                        DocumentFileCompat.fromUri(this, it)?.getAbsolutePath(this)
                            ?: it.toString(), it
                    )
                } catch (e: SecurityException) {
                    Log.e(TAG, "directoryPickerLauncher: takePersistableUriPermission", e)
                }
            } ?: Log.d(TAG, "directoryPickerLauncher: user cancelled")
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsViewModel: SettingsViewModel by viewModels { SettingsViewModel.Factory }
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
            directoryPickerLauncher.launch(Uri.parse(it))
        }.launchIn(this)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, DocumentFileCompat.getAccessibleAbsolutePaths(this).toString())
    }
}