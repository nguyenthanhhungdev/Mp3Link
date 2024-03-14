package com.example.mp3links

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.mp3link.R
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
    ),
        selectedAlbumState = MutableStateFlow<Album?>(null),
        onAlbumChange = {},
        onAlbumReload = {})
}

@Composable
@Preview
fun DownloadingDialogPreview() {
    DownloadingDialog(modifier = Modifier, information = DownloadingInformation())
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongItem(
    modifier: Modifier = Modifier,
    item: Song,
    onDownload: (songViewItem: Song) -> Unit,
    onPlay: (songViewItem: Song) -> Unit
) {
    OutlinedCard(modifier = modifier.padding(horizontal = 5.dp, vertical = 3.dp)) {
        Row(
            modifier = modifier.padding(start = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val downloaded = item.downloaded
            Text(
                text = item.name, maxLines = 1, modifier = modifier
                    .weight(1f)
                    .basicMarquee()
            )
            IconButton(
                onClick = { onDownload(item) },
                enabled = !downloaded,
                modifier = modifier
                    .fillMaxHeight()
                    .padding(0.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (downloaded) R.drawable.downloaded else R.drawable.download),
                    contentDescription = if (downloaded) "Already downloaded" else "Download",
                    modifier = modifier
                )
            }
            IconButton(
                onClick = { onPlay(item) },
                enabled = downloaded,
                modifier = modifier
                    .fillMaxHeight()
                    .padding(0.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (downloaded) R.drawable.play else R.drawable.not_playable),
                    contentDescription = if (downloaded) "Play" else "Not playable",
                    modifier = modifier
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AlbumList(
    modifier: Modifier = Modifier,
    albums: List<Album>,
    selectedAlbumState: StateFlow<Album?>,
    onAlbumChange: (Album) -> Unit,
    onAlbumReload: () -> Unit
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = modifier.weight(1f)
        ) {
            var menuExpanded by remember {
                mutableStateOf(false)
            }
            ExposedDropdownMenuBox(
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = it },
                modifier = modifier
            ) {
                val selectedAlbum by selectedAlbumState.collectAsState(null)
                TextField(value = selectedAlbum?.name ?: "No album",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = menuExpanded
                        )
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    label = {
                        Text(
                            text = "Album select", fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                    },
                    textStyle = TextStyle.Default.copy(fontSize = 12.sp, color = Color.Gray),
                    singleLine = true,
                    isError = selectedAlbum === null,
                    modifier = modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }) {
                    albums.forEach { album ->
                        DropdownMenuItem(text = {
                            Text(
                                text = album.name,
                                color = if (album === selectedAlbum) Color.Gray else Color.Unspecified
                            )
                        }, onClick = {
                            menuExpanded = false
                            onAlbumChange(album)
                        })
                    }
                }
            }
        }
        IconButton(onClick = onAlbumReload) {
            Icon(
                painter = painterResource(id = R.drawable.refresh),
                contentDescription = "Refresh Album List"
            )
        }
    }
}

@Composable
fun DownloadingDialog(
    modifier: Modifier = Modifier, information: DownloadingInformation,
) {
    val dialogProperties =
        DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    val downloadingType by information.state
    AlertDialog(onDismissRequest = { },
        confirmButton = {},
        dismissButton = {},
        properties = dialogProperties,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.downloading),
                contentDescription = "Downloading",
                modifier = modifier
            )
        },
        title = {
            Text(
                text = "Downloading " + when (downloadingType) {
                    DownloadingState.DOWNLOADING_NOT_DOWNLOADING -> "[Nothing]"
                    DownloadingState.DOWNLOADING_DATABASE -> "Database"
                    DownloadingState.DOWNLOADING_SONG -> "Song"
                }, modifier = modifier
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = modifier
            ) {
                val bytesSoFar by information.bytesSoFar
                val totalBytes by information.totalBytes
                if (information.text.isNotBlank()) Text(
                    text = information.text, modifier = modifier
                )
                val progress = bytesSoFar.toFloat().div(totalBytes)
                if (totalBytes != 0L) {
                    Text(
                        text = "$bytesSoFar/$totalBytes (${"%.2f".format(progress * 100)})",
                        fontSize = 12.sp, color = Color.Gray,
                        modifier = modifier.align(Alignment.End),
                    )
                }
                if (information.progressIsIndeterminate) LinearProgressIndicator(modifier = modifier) else LinearProgressIndicator(
                    progress = { bytesSoFar.toFloat().div(totalBytes) }, modifier = modifier
                )
            }
        })
}
