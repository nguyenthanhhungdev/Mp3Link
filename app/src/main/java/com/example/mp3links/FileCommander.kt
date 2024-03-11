package com.example.mp3links

import android.content.Context
import android.os.Environment
import android.util.Log
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.openInputStream
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write
import kotlin.io.path.Path

private const val TAG = "FileCommander"

class FileCommander @AssistedInject constructor(
    @ApplicationContext private val context: Context,
    @Assisted private val ftpSettings: FtpSettings,
    private val settingsRepository: SettingsRepository,
    private val ftpDataSourceFactory: FtpDataSource.FtpDataSourceFactory
) {
    @AssistedFactory
    interface FileCommanderFactory {
        fun create(ftpSettings: FtpSettings): FileCommander
    }

    private val databaseFile = "database.json"
    private val dataSource by lazy {
        ftpSettings.run {
            ftpDataSourceFactory.create(sourceHost, sourcePort, sourceUsername, sourcePassword)
        }
    }
    private var albumListSerialize: AlbumListSerialize? = null
    var albumList: List<Album>? = null
        private set
    private val fileAccessLock = ReentrantReadWriteLock()

    //    private suspend fun getAppDataDir() = settingsRepository.getStorageSettings().appDataDir
    private suspend fun getAppDataDir() =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath

    suspend fun retrieveDatabase(): AlbumListSerialize {
        Log.d(TAG, "retrieveDatabase: download database soon")
        // fake delay to test UI
//        delay(2000)
        val localDatabaseFile = Path(getAppDataDir(), databaseFile).toString()
        fileAccessLock.write {
            dataSource.retrieveFileAsync(
                databaseFile, localDatabaseFile
            ) { _, _ -> }
        }
        val databaseText = DocumentFileCompat.fromFullPath(
            context, localDatabaseFile
        )?.openInputStream(context)?.bufferedReader().use { it?.readText() }
        if (databaseText === null) {
            throw IOException("Database file downloaded(?) but still can't be opened")
        }
        val albumListSerialize: AlbumListSerialize
        try {
            albumListSerialize = Json.decodeFromString<AlbumListSerialize>(databaseText)
        } catch (e: SerializationException) {
            throw IOException("Downloaded database file corrupted", e)
        }
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
                path, Path(getAppDataDir(), path).toString(), progressListener
            )
        }
    }

    suspend fun isSongDownloaded(path: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext Path(getAppDataDir(), path).toString().let {
            DocumentFileCompat.doesExist(context, it) && (DocumentFileCompat.fromFullPath(
                context, it
            )?.length() ?: -1) > 0
        }
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