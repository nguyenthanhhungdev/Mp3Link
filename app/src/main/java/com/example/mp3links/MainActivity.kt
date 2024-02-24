package com.example.mp3links

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.mp3links.ui.theme.MP3LinksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MP3LinksTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MP3LinksScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MP3LinksScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = { Text("MP3 Links") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        content = { value ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(70.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
            ) {
//                OutlinedTextField(
//                    value = link,
//                    onValueChange = { link = it },
//                    label = { Text("Enter MP3 link") },
//                    modifier = Modifier.fillMaxWidth()
//                )
                val options = listOf("Option 1", "Option 2", "Option 3", "Option 4", "Option 5")
                var isExpanded by remember { mutableStateOf(false) }
                var selectedOptionText by remember { mutableStateOf(options[0]) }

                Box (

                ){
                    ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = {
                        newValue ->
                            isExpanded = newValue
                    }) {
                        TextField(value = selectedOptionText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(text = "Album") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                            },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                            options.forEach() {
                                    selectedOption ->
                                DropdownMenuItem(onClick = {
                                    selectedOptionText = selectedOption
                                    isExpanded = false

                                },
                                    text = { Text(text = selectedOption) }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* Tải MP3 từ liên kết */ },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Load")
                }
                Spacer(modifier = Modifier.height(30.dp))
                Text(text = "List Musics: ", fontWeight = FontWeight.Bold, fontSize = 30.sp)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn() {
                    items(100) {
                        Row {
                            Text(text = "Bài $it")
                            Button(onClick = {}) {
                                Text(text = "Dowload")
                            }
                            Button(onClick = { /*TODO*/ }) {
                                Text(text = "Play")
                            }
                        }
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    MP3LinksTheme {
        MP3LinksScreen()

    }
}