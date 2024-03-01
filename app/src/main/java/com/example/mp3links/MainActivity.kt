package com.example.mp3links

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.mp3links.ui.theme.MP3LinksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MP3LinksTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    MP3LinksScreen()
                }
            }
//            TestMutableStateList()
        }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MP3LinksScreen() {
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
            Row {
                AlbumDropDownPreview()
                Button(onClick = {}) {
                    Text("Load")
                }
            }
            Text(text = "List Music: ", fontWeight = FontWeight.Bold, fontSize = 30.sp)
            SongItemPreview()
        }
    })
}

@Composable
fun GreetingPreview() {
    MP3LinksTheme {
        MP3LinksScreen()

    }
}