package com.example.mp3links

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mp3link.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true, showSystemUi = true)
fun SettingsPage() {
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "Settings") }, modifier = Modifier.fillMaxWidth())
    }, content = { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SettingsCardText(
                icon = painterResource(id = R.drawable.source_host),
                title = "Source Host",
                desc = "The internet address of the FTP server"
            ) {}
            SettingsCardText(
                icon = painterResource(id = R.drawable.source_port),
                title = "Source Port",
                desc = "The opened FTP port of the source host"
            ) {}
            SettingsCardText(
                icon = painterResource(id = R.drawable.source_account_username),
                title = "Source Account Username",
                desc = "The authorized account username to login into the FTP server"
            ) {}
            SettingsCardText(
                icon = painterResource(id = R.drawable.source_account_password),
                title = "Source Account Password",
                desc = "The account password of said username"
            ) {}
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCardText(
    modifier: Modifier = Modifier, icon: Painter, title: String, desc: String, onClick: () -> Unit
) {
    OutlinedCard(
        onClick = { onClick() },
        modifier = modifier
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = modifier.padding(horizontal = 20.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = modifier
                )
                Text(
                    text = desc, fontSize = 14.sp, modifier = modifier
                )
            }
        }
    }
}