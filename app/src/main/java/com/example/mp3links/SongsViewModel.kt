package com.example.mp3links

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.io.path.Path

class SongsViewModel : ViewModel() {
    private val _openGenericFileActivityLiveEvent = Channel<String>(capacity = Channel.BUFFERED)
    val openGenericFileActivityLiveEvent = _openGenericFileActivityLiveEvent.receiveAsFlow()
    private val _showNotifyToastLiveEvent = Channel<String>(capacity = Channel.BUFFERED)
    val showNotifyToastLiveEvent = _showNotifyToastLiveEvent.receiveAsFlow()

    private val _albums = listOf<Album>().toMutableStateList()
    val albums: List<Album>
        get() = _albums

    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum = _selectedAlbum.asStateFlow()
    private val _downloadingInformation = MutableStateFlow(DownloadingInformation())
    val downloadingInformation = _downloadingInformation.asStateFlow()

    private val fileCommander by lazy { FileCommander("", 0, "", "") }

    fun setSelectedAlbum(album: Album) {
        if (_albums.contains(album)) {
            _selectedAlbum.value = album
            // fetch songs here
        } else Log.e("UI", "Selecting non-exist album")
    }

    suspend fun reloadAlbumList() {
        _downloadingInformation.value = DownloadingInformation(
            """Connection Settings:
            |Host:
            |Port: 0
            |Username:
            |Password:
        """.trimMargin()
        )
        _downloadingInformation.value.state.value = DownloadingState.DOWNLOADING_DATABASE
        try {
            fileCommander.retrieveDatabase()
        } catch (e: IOException) {
            _showNotifyToastLiveEvent.send(
                e.message ?: "Unknown Error connecting to database server"
            )
            Log.e("Network", "Connect to database error", e)
            return
        } finally {
            _downloadingInformation.value.state.value = DownloadingState.DOWNLOADING_NOT_DOWNLOADING
        }
        _albums.clear()
        fileCommander.albumList?.let { _albums.addAll(it) }
    }

    fun reloadAlbumListTest(string: String) {
        fileCommander.retrieveDatabaseTest(string)
        _albums.clear()
        fileCommander.albumList?.let { _albums.addAll(it) }
        if (_albums.size > 0) setSelectedAlbum(_albums.first())
    }

    fun downloadSong(song: Song) = viewModelScope.launch {
        _downloadingInformation.value = DownloadingInformation("Song", false)
        _downloadingInformation.value.state.value = DownloadingState.DOWNLOADING_SONG
        try {
            fileCommander.retrieveFile(song) { bytesSoFar, totalBytes ->
                _downloadingInformation.value.bytesSoFar.value = bytesSoFar
                _downloadingInformation.value.totalBytes.value = totalBytes
            }
        } catch (e: IOException) {
            _showNotifyToastLiveEvent.send(
                e.message ?: "Unknown Error connecting to database server"
            )
            Log.e("Network", "Connect to database error", e)
            return@launch
        } finally {
            _downloadingInformation.value.state.value = DownloadingState.DOWNLOADING_NOT_DOWNLOADING
        }
    }

    fun playSong(song: Song) = viewModelScope.launch {
        _openGenericFileActivityLiveEvent.send(
            Path(
                fileCommander.appDataDirectory, song.path
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