package com.example.mp3links

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mp3links.ui.theme.MP3LinksTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLConnection

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: SongsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: SongsViewModel by viewModels()
        this.viewModel = viewModel
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.openGenericFileActivityLiveEvent.onEach { path ->
                    startActivity(Intent(Intent.ACTION_VIEW).apply {
                        val file = File(path)
                        FileProvider.getUriForFile(
                            this@MainActivity,
                            "${applicationContext.packageName}.fileprovider",
                            file
                        ).let {
                            setDataAndType(
                                Uri.fromFile(file),
                                MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
                                    ?: URLConnection.guessContentTypeFromName(file.name)
                            )
                            flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                    })
                }.launchIn(lifecycleScope)
                viewModel.showNotifyToastLiveEvent.onEach { text ->
                    Toast.makeText(this@MainActivity, text, Toast.LENGTH_LONG).show()
                }.launchIn(lifecycleScope)
                viewModel.reloadAlbumListTest(Helper.getFakeAlbumListString(this@MainActivity))
                viewModel.reloadAlbumList()
            }
        }
        setContent {
            MP3LinksTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    MP3LinksScreen(viewModel)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MP3LinksScreen(viewModel: SongsViewModel) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary
            ), title = { Text("MP3 Drive") }, modifier = Modifier.fillMaxWidth()
        )
    }, content = { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AlbumList(albums = viewModel.albums,
                selectedAlbumState = viewModel.selectedAlbum,
                onAlbumChange = { viewModel.setSelectedAlbum(it) },
                onAlbumReload = { coroutineScope.launch { viewModel.reloadAlbumList() } })
            val selectedAlbum by viewModel.selectedAlbum.collectAsState()
            SongItemList(itemList = selectedAlbum?.songs ?: emptyList(),
                onSongDownload = { song -> coroutineScope.launch { viewModel.downloadSong(song) } },
                onSongPlay = { song -> viewModel.playSong(song) })
        }
    })
    val downloadingInformation by viewModel.downloadingInformation.collectAsState()
    val downloadingState by downloadingInformation.state
    when (downloadingState) {
        DownloadingState.DOWNLOADING_NOT_DOWNLOADING -> {}
        else -> DownloadingDialog(information = downloadingInformation)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TestMutableStateList() {
    val _songList = listOf(Song("Nhac dam cuoi", "", false), Song("Nhac dam ma", "", true))
    val songList = _songList.toMutableStateList()
    Column {
        Button(onClick = { songList.replaceAll { song -> song.copy(downloaded = !song.downloaded) } }) {
            Text("Change state")
        }
        SongItemList(itemList = songList, onSongDownload = {}, onSongPlay = {})
    }
}
