package com.example.mp3links

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import com.example.mp3link.R
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(viewModel: FtpSettingsViewModel) {
    var settingType by rememberSaveable {
        mutableStateOf(FtpSettingsViewModel.DialogEnum.DIALOG_NOT_SHOWN)
    }
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "Settings") }, modifier = Modifier.fillMaxWidth())
    }, content = { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SettingsCardText(
                icon = painterResource(id = R.drawable.source_host),
                title = "Source Host",
                desc = "The internet address of the FTP server"
            ) { settingType = FtpSettingsViewModel.DialogEnum.DIALOG_SOURCE_HOST }
            SettingsCardText(
                icon = painterResource(id = R.drawable.source_port),
                title = "Source Port",
                desc = "The opened FTP port of the source host"
            ) { settingType = FtpSettingsViewModel.DialogEnum.DIALOG_SOURCE_PORT }
            SettingsCardText(
                icon = painterResource(id = R.drawable.source_account_username),
                title = "Source Account Username",
                desc = "The authorized account username to login into the FTP server"
            ) { settingType = FtpSettingsViewModel.DialogEnum.DIALOG_SOURCE_USERNAME }
            SettingsCardText(
                icon = painterResource(id = R.drawable.source_account_password),
                title = "Source Account Password",
                desc = "The account password of said username"
            ) { settingType = FtpSettingsViewModel.DialogEnum.DIALOG_SOURCE_PASSWORD }
        }
    })
    val ftpSettings = viewModel.stateFlow.collectAsState()
    when (settingType) {
        FtpSettingsViewModel.DialogEnum.DIALOG_SOURCE_HOST -> SettingInputDialog(text = "Source Host",
            currentSettingValue = ftpSettings.value.sourceHost,
            validator = SettingType.SourceHost.validator,
            onApply = {
                viewModel.sourceHost(it)
                settingType = FtpSettingsViewModel.DialogEnum.DIALOG_NOT_SHOWN
            },
            onDismiss = {
                settingType = FtpSettingsViewModel.DialogEnum.DIALOG_NOT_SHOWN
            })

        FtpSettingsViewModel.DialogEnum.DIALOG_SOURCE_PORT -> SettingInputDialog(text = "Source Port",
            currentSettingValue = ftpSettings.value.sourcePort.toString(),
            validator = SettingType.SourcePort.validator,
            onApply = {
                viewModel.sourcePort(it.toInt())
                settingType = FtpSettingsViewModel.DialogEnum.DIALOG_NOT_SHOWN
            },
            onDismiss = {
                settingType = FtpSettingsViewModel.DialogEnum.DIALOG_NOT_SHOWN
            })

        FtpSettingsViewModel.DialogEnum.DIALOG_SOURCE_USERNAME -> SettingInputDialog(text = "Source Username",
            currentSettingValue = ftpSettings.value.sourceUsername,
            validator = SettingType.SourceUsername.validator,
            onApply = {
                viewModel.sourceUsername(it)
                settingType = FtpSettingsViewModel.DialogEnum.DIALOG_NOT_SHOWN
            },
            onDismiss = {
                settingType = FtpSettingsViewModel.DialogEnum.DIALOG_NOT_SHOWN
            })

        FtpSettingsViewModel.DialogEnum.DIALOG_SOURCE_PASSWORD -> SettingInputDialog(text = "Source Password",
            currentSettingValue = ftpSettings.value.sourcePassword,
            passwordInput = true,
            validator = SettingType.SourcePassword.validator,
            onApply = {
                viewModel.sourcePassword(it)
                settingType = FtpSettingsViewModel.DialogEnum.DIALOG_NOT_SHOWN
            },
            onDismiss = {
                settingType = FtpSettingsViewModel.DialogEnum.DIALOG_NOT_SHOWN
            })

        else -> {}
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true, showSystemUi = true)
fun SettingsPagePreview() {
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

@Preview
@Composable
fun SettingInputDialogPreview() {
    SettingInputDialog(
        text = "Preview",
        currentSettingValue = "Content",
        passwordInput = false,
        validator = { text -> text.isDigitsOnly() },
        onDismiss = {},
        onApply = {},
    )
}

@Preview
@Composable
fun SettingInputDialogPasswordPreview() {
    SettingInputDialog(
        text = "Password",
        currentSettingValue = "Content",
        passwordInput = true,
        validator = { text -> text.isDigitsOnly() },
        onDismiss = {},
        onApply = {},
    )
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

@Composable
fun SettingInputDialog(
    modifier: Modifier = Modifier,
    text: String = "Settings Input Dialog",
    currentSettingValue: String = "",
    passwordInput: Boolean = false,
    validator: (String) -> Boolean = { true },
    onDismiss: (String) -> Unit = {},
    onApply: (String) -> Unit = {},
) {
    var beingShown by rememberSaveable { mutableStateOf(false) }
    var settingValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(currentSettingValue)
        )
    }
    val settingValueValid = validator(settingValue.text)
    var passwordUnmasked by remember { mutableStateOf(!passwordInput) }
    val focusRequester = remember { FocusRequester() }
    var focusRequested by rememberSaveable { mutableStateOf(false) }
    AlertDialog(onDismissRequest = {
        onDismiss(settingValue.text)
        beingShown = false
    }, confirmButton = {
        Button(onClick = { onApply(settingValue.text) }, enabled = settingValueValid) {
            Text(text = "OK")
        }
    }, dismissButton = {
        TextButton(onClick = { onDismiss(settingValue.text) }) {
            Text(text = "Cancel")
        }
    }, title = { Text(text = text) }, text = {
        TextField(
            value = settingValue,
            onValueChange = { settingValue = it },
            isError = !settingValueValid,
            visualTransformation = if (passwordUnmasked) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Row(modifier = modifier) {
                    if (passwordInput) if (passwordUnmasked) IconButton(onClick = {
                        passwordUnmasked = false
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.password_masked),
                            contentDescription = "Hide password"
                        )
                    } else IconButton(onClick = { passwordUnmasked = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.password_unmask),
                            contentDescription = "Show password"
                        )
                    }
                    IconButton(onClick = { settingValue = settingValue.copy(text = "") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.clear),
                            contentDescription = "Clear input"
                        )
                    }
                }
            },
            modifier = modifier.focusRequester(focusRequester)
        )
    }, modifier = modifier)
    if (!focusRequested) {
        LaunchedEffect(Unit) {
            settingValue = settingValue.copy(selection = TextRange(settingValue.text.length))
            // wait for recomposition
            delay(200)
            focusRequester.requestFocus()
            focusRequested = true
        }
    }
}

