package com.example.mp3links

import android.util.Log
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.io.path.Path

class SongsViewModel : ViewModel() {
    private val _openGenericFileActivityLiveEvent = Channel<String>(capacity = Channel.BUFFERED)
    val openGenericFileActivityLiveEvent = _openGenericFileActivityLiveEvent.receiveAsFlow()


    private val _albums = listOf<Album>().toMutableStateList()
    val albums: List<Album>
        get() = _albums

    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum = _selectedAlbum.asStateFlow()
    var downloadingInformation = DownloadingInformation()
        private set

    private val fileCommander = FileCommander("", 0, "", "")

    init {
        viewModelScope.launch { fileCommander.retrieveDatabase() }
    }

    fun setSelectedAlbum(album: Album) {
        if (_albums.contains(album)) {
            _selectedAlbum.value = album
            // fetch songs here
        } else Log.e("UI", "Selecting non-exist album")
    }

    suspend fun reloadAlbumList() {
        downloadingInformation.setDownloadingState(DownloadingState.DOWNLOADING_DATABASE)
        fileCommander.retrieveDatabase()
        _albums.clear()
        fileCommander.albumList?.let { _albums.addAll(it) }
        downloadingInformation.setDownloadingState(DownloadingState.DOWNLOADING_NOT_DOWNLOADING)
    }

    fun downloadSong(song: Song, progressListener: (Long, Long) -> Unit) = viewModelScope.launch {
        downloadingInformation.setDownloadingState(DownloadingState.DOWNLOADING_SONG)
        fileCommander.retrieveFile(song, progressListener)
        downloadingInformation.setDownloadingState(DownloadingState.DOWNLOADING_NOT_DOWNLOADING)
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

class DownloadingInformation(
    val text: String = "Text",
    val totalBytes: Long = 0L,
    val progressIsIndeterminate: Boolean = true,
) {
    private val _downloadingState = MutableStateFlow(DownloadingState.DOWNLOADING_NOT_DOWNLOADING)
    val downloadingState = _downloadingState.asStateFlow()
    fun setDownloadingState(newDownloadingState: DownloadingState) {
        _downloadingState.value = newDownloadingState
    }

    private val _bytesSoFar: MutableStateFlow<Long> = MutableStateFlow(0L)
    val bytesSoFar: StateFlow<Long> = _bytesSoFar.asStateFlow()
    fun setBytesSoFar(newBytesSoFar: Long) {
        _bytesSoFar.value = newBytesSoFar
    }
}