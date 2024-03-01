package com.example.mp3links

import android.util.Log
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SongsViewModel : ViewModel() {
    private val fileCommander = FileCommander("", 0, "", "")

    init {
        viewModelScope.launch { fileCommander.retrieveDatabase() }
    }

    private val _albums = listOf<Album>().toMutableStateList()
    val albums: List<Album>
        get() = _albums

    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum = _selectedAlbum.asStateFlow()
    fun setSelectedAlbum(album: Album) {
        if (_albums.contains(album)) {
            _selectedAlbum.value = album
            // fetch songs here
        } else Log.e("UI", "Selecting non-exist album")
    }

    fun replaceAlbumList(newDatabase: Database) {
        _albums.clear()
        _albums.addAll(fileCommander.albumList ?: emptyList())
    }

    fun downloadSong(song: Song, progressListener: (Long, Long) -> Unit) {
        viewModelScope.launch { fileCommander.retrieveFile(song, progressListener) }
    }

    fun playSong(song: Song) {
        TODO()
    }
}


