package com.example.mp3links

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
@Preview(showBackground = true)
fun SongItemPreview() {
    SongItemList(itemList = listOf(Song("Nhac dam cuoi", "", false), Song("Nhac dam ma", "", true)),
        onSongDownload = {},
        onSongPlay = {})
}

@Composable
@Preview(showBackground = true)
fun AlbumDropDownPreview() {
    AlbumList(albums = listOf(
        Album("Nhac dam cuoi", emptyList()), Album("Nhac dam ma", emptyList())
    ), selectedAlbumState = MutableStateFlow<Album?>(null), onAlbumChange = {})
}

@Composable
fun SongItemList(
    modifier: Modifier = Modifier,
    itemList: List<Song>,
    onSongDownload: (songViewItem: Song) -> Unit,
    onSongPlay: (songViewItem: Song) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(itemList) {
            SongItem(item = it, onDownload = onSongDownload, onPlay = onSongPlay)
        }
    }
}

@Composable
fun SongItem(
    modifier: Modifier = Modifier,
    item: Song,
    onDownload: (songViewItem: Song) -> Unit,
    onPlay: (songViewItem: Song) -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            5.dp
        )
    ) {
        val downloaded = item.downloaded
        Text(text = item.name, modifier = modifier)
        Button(onClick = { onDownload(item) }, enabled = !downloaded, modifier = modifier) {
            Text(text = "Download")
        }
        Button(
            onClick = { onPlay(item) }, enabled = downloaded, modifier = modifier
        ) {
            Text(text = "Play")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumList(
    modifier: Modifier = Modifier,
    albums: List<Album>,
    selectedAlbumState: StateFlow<Album?>,
    onAlbumChange: (Album) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        var menuExpanded by remember {
            mutableStateOf(false)
        }
        val selectedAlbum by selectedAlbumState.collectAsState(null)
        val selectedText = selectedAlbum?.name ?: "No album"
        ExposedDropdownMenuBox(
            expanded = menuExpanded, onExpandedChange = { menuExpanded = it }, modifier = modifier
        ) {
            TextField(value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = menuExpanded
                    )
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }) {
                albums.forEach { album ->
                    DropdownMenuItem(text = { Text(text = album.name) }, onClick = {
                        menuExpanded = false
                        onAlbumChange(album)
                    })
                }
            }
        }
    }
}