package com.example.mp3links

import android.net.InetAddresses
import android.os.Build
import android.util.Log
import android.util.Patterns
import androidx.annotation.RequiresApi
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

private const val TAG = "FtpSettingsRepository"

class FtpSettingsRepository(private val dataStore: DataStore<FtpSettings>) {
    val flow = dataStore.data
    suspend fun getFtpSettings(): FtpSettings = dataStore.data.first()

    private suspend fun saveFtpSettings(ftpSettings: FtpSettings) {
        Log.d(TAG, "saveFtpSettings: saving with DataStore, value $ftpSettings")
        dataStore.updateData { ftpSettings }
    }

    suspend fun updateFtpSettings(type: SettingType, value: String) {
        saveFtpSettings(getFtpSettings().let {
            when (type) {
                SettingType.SourceHost -> it.copy(sourceHost = value)
                SettingType.SourceUsername -> it.copy(sourceUsername = value)
                SettingType.SourcePassword -> it.copy(sourcePassword = value)
                else -> {
                    Log.wtf(
                        TAG, "Updating wrong setting value type (type=$type value=$value)"
                    )
                    return
                }
            }
        })
    }

    suspend fun updateFtpSettings(type: SettingType, value: Int) {
        saveFtpSettings(getFtpSettings().let {
            when (type) {
                SettingType.SourcePort -> it.copy(sourcePort = value)
                else -> {
                    Log.wtf(
                        TAG, "Updating wrong setting value type (type=$type value=$value)"
                    )
                    return
                }
            }
        })
    }
}

@Serializable
data class FtpSettings(
    val sourceHost: String,
    val sourcePort: Int,
    val sourceUsername: String,
    val sourcePassword: String
)

object FtpSettingsSerializer : Serializer<FtpSettings> {
    override val defaultValue = FtpSettings("0.0.0.0", 2121, "username", "password")
    override suspend fun readFrom(input: InputStream): FtpSettings {
        try {
            return Json.decodeFromString(
                FtpSettings.serializer(), input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            throw CorruptionException("Unable to read FtpSettings", e)
        }
    }

    override suspend fun writeTo(t: FtpSettings, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(Json.encodeToString(FtpSettings.serializer(), t).encodeToByteArray())
        }
    }
}

sealed class SettingType {

    abstract val validator: (String) -> Boolean

    data object SourceHost : SettingType() {
        @RequiresApi(Build.VERSION_CODES.Q)
        override val validator: (String) -> Boolean = { text ->
            InetAddresses.isNumericAddress(text) || Patterns.WEB_URL.matcher(text)
                .matches() || Patterns.IP_ADDRESS.matcher(text).matches()
        }
    }

    data object SourcePort : SettingType() {
        override val validator: (String) -> Boolean = { text ->
            (text.toUIntOrNull() ?: 0u) in 1u..65535u
        }
    }

    data object SourceUsername : SettingType() {
        override val validator: (String) -> Boolean =
            { text -> text.isNotEmpty() && text.all { char -> char.isLetterOrDigit() } }
    }

    data object SourcePassword : SettingType() {
        override val validator: (String) -> Boolean =
            { text -> text.all { char -> char.code in 32..127 } } // printable ascii
    }
}