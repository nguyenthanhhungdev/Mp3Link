package com.example.mp3links

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

private const val TAG = "FileCommander"

class FileCommander(
    private val sourceIP: String,
    private val sourcePort: Int,
    private val username: String,
    private val password: String,
    private val settingsRepository: SettingsRepository
) {
    constructor(ftpSettings: FtpSettings, settingsRepository: SettingsRepository) : this(
        ftpSettings.sourceHost,
        ftpSettings.sourcePort,
        ftpSettings.sourceUsername,
        ftpSettings.sourcePassword,
        settingsRepository
    )

    private val databaseFile = "database.json"
    private val dataSource by lazy { FtpDataSource(sourceIP, sourcePort, username, password) }
    private var albumListSerialize: AlbumListSerialize? = null
    var albumList: List<Album>? = null
        private set
    private val fileAccessLock = ReentrantReadWriteLock()
    private suspend fun getAppDataDirectory() = settingsRepository.getStorageSettings().appDataUri

    suspend fun retrieveDatabase(): AlbumListSerialize {
        Log.d(TAG, "retrieveDatabase: download database soon")
        // fake delay to test UI
//        delay(2000)
        val localDatabaseFile = Path(getAppDataDirectory(), databaseFile).toString()
        fileAccessLock.write {
            dataSource.retrieveFileAsync(
                databaseFile, localDatabaseFile
            ) { _, _ -> }
        }
        val albumListSerialize =
            Json.decodeFromString<AlbumListSerialize>(Path(localDatabaseFile).readText())
        this.albumListSerialize = albumListSerialize
        albumList = albumListSerialize.albums.map { album ->
            Album(album.name, album.songs.map { song ->
                Song(
                    song.name, song.path, isSongDownloaded(song.path)
                )
            })
        }
        return albumListSerialize
    }

    suspend fun retrieveDatabaseTest(string: String) {
        val albumListSerialize = Json.decodeFromString<AlbumListSerialize>(string)
        this.albumListSerialize = albumListSerialize
        albumList = albumListSerialize.albums.map { album ->
            Album(album.name, album.songs.map { song ->
                Song(
                    song.name, song.path, isSongDownloaded(song.path)
                )
            })
        }
    }

    suspend fun retrieveFile(
        song: Song, progressListener: (Long, Long) -> Unit
    ) {
        retrieveFile(song.path, progressListener)
    }

    private suspend fun retrieveFile(path: String, progressListener: (Long, Long) -> Unit) {
        fileAccessLock.write {
            dataSource.retrieveFileAsync(
                path, Path(getAppDataDirectory(), path).toString(), progressListener
            )
        }
    }

    private suspend fun isSongDownloaded(path: String): Boolean = withContext(Dispatchers.IO) {
        Path(getAppDataDirectory(), path).exists()
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