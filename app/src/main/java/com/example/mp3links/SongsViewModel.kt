package com.example.mp3links

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import kotlin.io.path.Path

private const val TAG = "SongsViewModel"

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val fileCommanderFactory: FileCommander.FileCommanderFactory
) : ViewModel(), DefaultLifecycleObserver {
    private var fileCommander: FileCommander = fileCommanderFactory.create(FtpSettings.defaultValue)
    private val _openGenericFileActivityLiveEvent = Channel<String>(capacity = Channel.BUFFERED)
    val openGenericFileActivityLiveEvent = _openGenericFileActivityLiveEvent.receiveAsFlow()
    private val _showNotifyToastLiveEvent = Channel<String>(capacity = Channel.BUFFERED)
    val showNotifyToastLiveEvent = _showNotifyToastLiveEvent.receiveAsFlow()
    private val _albums = MutableStateFlow(listOf<Album>())
    val albums = _albums.asStateFlow()
    private val _downloadingInformation = MutableStateFlow(DownloadingInformation())
    val downloadingInformation = _downloadingInformation.asStateFlow()
    private val ftpSettings: StateFlow<FtpSettings> = settingsRepository.ftpSettingsFlow.stateIn(
        viewModelScope, SharingStarted.Lazily, FtpSettings.defaultValue
    )
    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum = _selectedAlbum.asStateFlow()
    fun setSelectedAlbum(album: Album) {
        if (_albums.value.contains(album)) {
            _selectedAlbum.value = album
            // fetch songs here
        } else Log.wtf(TAG, "Selecting non-exist album")
    }

    init {
        ftpSettings.onEach {
            Log.v(TAG, "init: received a ftp settings update event")
            ftpSettingsUpdated()
        }.launchIn(viewModelScope)
    }

    private fun ftpSettingsUpdated() = viewModelScope.launch {
        Log.d(
            TAG,
            "ftpSettingsUpdated: reloading with new ftp settings ${settingsRepository.getFtpSettings()}"
        )
        fileCommander = fileCommanderFactory.create(settingsRepository.getFtpSettings())
        reloadAlbumList()
    }

    suspend fun reloadAlbumList() {
        settingsRepository.getFtpSettings().run {
            _downloadingInformation.value = DownloadingInformation(
                """Connection Settings:
            |Host: $sourceHost
            |Port: $sourcePort
            |Username: $sourceUsername
            |Password: $sourcePassword
        """.trimMargin()
            )
        }
        _downloadingInformation.value.state.value = DownloadingState.DOWNLOADING_DATABASE
        try {
            Log.i(TAG, "reloadAlbumList: reloading from ftp server")
            fileCommander.retrieveDatabase()
        } catch (e: IOException) {
            _showNotifyToastLiveEvent.send(
                e.message ?: "Unknown Error connecting to database server"
            )
            Log.w(TAG, "Connect to database error", e)
            return
        } finally {
            _downloadingInformation.value.state.value = DownloadingState.DOWNLOADING_NOT_DOWNLOADING
        }
        Log.v(TAG, "reloadAlbumList: adding ${fileCommander.albumList?.size} albums")
        repopulateUiAlbumList()
    }

    private fun repopulateUiAlbumList() {
        fileCommander.albumList?.let {
            _selectedAlbum.value = null
            _albums.value = it
            if (_albums.value.isNotEmpty()) setSelectedAlbum(_albums.value.first())
        }
    }

    suspend fun reloadAlbumListTest(string: String) {
        fileCommander.retrieveDatabaseTest(string)
        repopulateUiAlbumList()
    }

    fun downloadSong(song: Song) = viewModelScope.launch {
        _downloadingInformation.value = DownloadingInformation(song.path, false)
        _downloadingInformation.value.state.value = DownloadingState.DOWNLOADING_SONG
        try {
            Log.d(TAG, "downloadSong: $song")
            fileCommander.retrieveFile(song) { bytesSoFar, totalBytes ->
                _downloadingInformation.value.bytesSoFar.value = bytesSoFar
                _downloadingInformation.value.totalBytes.value = totalBytes
            }
        } catch (e: IOException) {
            _showNotifyToastLiveEvent.send(
                e.message ?: "Unknown Error connecting to database server"
            )
            Log.w(TAG, "Connect to database error", e)
            return@launch
        } finally {
            _downloadingInformation.value.state.value = DownloadingState.DOWNLOADING_NOT_DOWNLOADING
        }
        _albums.value = albums.value.map { album ->
            if (album === selectedAlbum.value) {
                album.copy(songs = selectedAlbum.value!!.songs.map { oldSong ->
                    if (oldSong === song) song.copy(
                        downloaded = fileCommander.isSongDownloaded(song.path)
                    ) else oldSong
                }).also { _selectedAlbum.value = it }
            } else album
        }
    }

    fun playSong(song: Song) = viewModelScope.launch {
        Log.d(TAG, "playSong: $song")
        _openGenericFileActivityLiveEvent.send(
            Path(
                settingsRepository.getStorageSettings().appDataDir, song.path
            ).toString()
        )
    }
}

enum class DownloadingState {
    DOWNLOADING_NOT_DOWNLOADING, DOWNLOADING_DATABASE, DOWNLOADING_SONG,
}

data class DownloadingInformation(
    val text: String = "Text",
    val progressIsIndeterminate: Boolean = true,
    val state: MutableState<DownloadingState> = mutableStateOf(DownloadingState.DOWNLOADING_NOT_DOWNLOADING),
    val bytesSoFar: MutableState<Long> = mutableLongStateOf(0L),
    val totalBytes: MutableState<Long> = mutableLongStateOf(0L),
)