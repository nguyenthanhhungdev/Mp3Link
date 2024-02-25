package com.example.mp3links

import android.os.Environment
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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
    private val appDataDirectory = Environment.getExternalStorageDirectory().toString()
    private val dataSource = FTPDataSource(sourceIP, sourcePort, username, password)
    private var database: Database? = null
    suspend fun retrieveDatabase() {
        val localDatabaseFile = Path(appDataDirectory, databaseFile).toString()
        dataSource.retrieveFileAsync(
            databaseFile,
            localDatabaseFile
        ) { _, _ -> }
        database = Json.decodeFromString<Database>(Path(localDatabaseFile).readText())
    }

    fun getDatabase(): Database? {
        return database
    }

    suspend fun retrieveFile(album: Album, song: Song, progressListener: (Long, Long) -> Unit) {
        val file = Path(album.name, song.name).toString()
        dataSource.retrieveFileAsync(
            file,
            Path(appDataDirectory, file).toString(),
            progressListener
        )
    }

    fun isSongDownloaded(album: Album, song: Song): Boolean {
        return Path(appDataDirectory, album.name, song.name).exists()
    }
}

@Serializable
data class Database(val albums: List<Album>)

@Serializable
data class Album(val name: String, val songs: List<Song>)

@Serializable
data class Song(val name: String)