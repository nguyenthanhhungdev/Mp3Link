package com.example.mp3links

import android.os.Environment
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

class FileCommander(
    private val sourceIP: String,
    private val sourcePort: Int,
    private val username: String,
    private val password: String
) {
    private val databaseFile = "database.json"
    val appDataDirectory = Environment.getExternalStorageDirectory().toString()
    private var dataSource: FTPDataSource? = null
    private var albumListSerialize: AlbumListSerialize? = null
    var albumList: List<Album>? = null
        private set
    private val fileAccessLock = ReentrantReadWriteLock()
    suspend fun retrieveDatabase(): AlbumListSerialize? {
        if (dataSource === null) {
            dataSource = FTPDataSource(sourceIP, sourcePort, username, password)
        }
        val localDatabaseFile = Path(appDataDirectory, databaseFile).toString()
        fileAccessLock.write {
            dataSource!!.retrieveFileAsync(
                databaseFile, localDatabaseFile
            ) { _, _ -> }
        }
        albumListSerialize =
            Json.decodeFromString<AlbumListSerialize>(Path(localDatabaseFile).readText())
        albumList = albumListSerialize!!.albums.map { album ->
            Album(album.name, album.songs.map { song ->
                Song(
                    song.name, song.path, isSongDownloaded(song.path)
                )
            })
        }
        return albumListSerialize
    }

    suspend fun retrieveFile(
        song: Song, progressListener: (Long, Long) -> Unit
    ) {
        retrieveFile(song.path, progressListener)
    }

    private suspend fun retrieveFile(path: String, progressListener: (Long, Long) -> Unit) {
        fileAccessLock.write {
            dataSource!!.retrieveFileAsync(
                path, Path(appDataDirectory, path).toString(), progressListener
            )
        }
    }

    fun isSongDownloaded(song: Song): Boolean {
        return isSongDownloaded(song.path)
    }

    private fun isSongDownloaded(path: String): Boolean {
        return Path(appDataDirectory, path).exists()
    }
}

@Serializable
data class AlbumListSerialize(val albums: List<AlbumSerialize>)

@Serializable
data class AlbumSerialize(val name: String, val songs: List<SongSerialize>)

@Serializable
data class SongSerialize(val name: String, val path: String)

data class Song(val name: String, val path: String, val downloaded: Boolean)
data class Album(val name: String, val songs: List<Song>)