package com.example.mp3links

import android.content.Context
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.openOutputStream
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPConnectionClosedException
import org.apache.commons.net.ftp.FTPReply
import org.apache.commons.net.ftp.FTPSClient
import org.apache.commons.net.io.CopyStreamEvent
import org.apache.commons.net.io.CopyStreamListener
import org.apache.commons.net.util.TrustManagerUtils
import java.io.File
import java.io.IOException
import java.io.OutputStream
import kotlin.io.path.Path

private const val TAG = "FtpDataSource"

class FtpDataSource @AssistedInject constructor(
    @ApplicationContext private val context: Context,
    @Assisted("source_ip") private val sourceIP: String,
    @Assisted private val sourcePort: Int,
    @Assisted("source_username") private val username: String,
    @Assisted("source_password") private val password: String,
    @Assisted secureFtp: Boolean = false
) {
    @AssistedFactory
    interface FtpDataSourceFactory {
        fun create(
            @Assisted("source_ip") sourceIP: String,
            sourcePort: Int,
            @Assisted("source_username") username: String,
            @Assisted("source_password") password: String,
            secureFtp: Boolean = false
        ): FtpDataSource
    }

    private val client: FTPClient = if (secureFtp) FTPSClient("TLS", true) else FTPClient()

    init {
        Log.d(TAG, "init: is secure ftp $secureFtp")
        if (secureFtp) (client as FTPSClient).trustManager =
            TrustManagerUtils.getAcceptAllTrustManager()
    }

    private var currentFileTotalSize: Long = -1
    private fun retrieveFile(
        sourceFile: String, destFile: String, progressListener: (Long, Long) -> Unit
    ) {
        Log.i(TAG, "retrieveFile: downloading file $sourceFile into $destFile")
        try {
            Log.d(TAG, "retrieveFile: open file stream")
            val stream = openFileStream(destFile)
            Log.d(TAG, "retrieveFile: logging into ftp server")
            client.connect(sourceIP, sourcePort)
            client.login(username, password)
            client.setFileType(FTP.BINARY_FILE_TYPE)
            client.enterLocalPassiveMode()
            client.copyStreamListener = object : CopyStreamListener {
                override fun bytesTransferred(event: CopyStreamEvent?) {
                    event?.run { progressListener(totalBytesTransferred, currentFileTotalSize) }
                }

                override fun bytesTransferred(
                    totalBytesTransferred: Long, bytesTransferred: Int, streamSize: Long
                ) {
                    progressListener(totalBytesTransferred, currentFileTotalSize)
                }

            }
            currentFileTotalSize =
                if (client.size(sourceFile) == FTPReply.FILE_STATUS) client.replyString.trim()
                    .split(" ")[1].toLong() else -1
            Log.d(TAG, "retrieveFile: download start with size $currentFileTotalSize")
            stream.use {
                if (!client.retrieveFile(
                        sourceFile, it
                    )
                ) throw FTPConnectionClosedException("retrieveFile returned false, file may not exist on server")
            }
        } finally {
            if (client.isConnected) {
                Log.d(TAG, "retrieveFile: disconnecting from the server")
                client.logout()
                client.disconnect()
            } else Log.d(TAG, "retrieveFile: not yet connected to the server")
        }
    }

    private fun openFileStream(destFile: String): OutputStream {
        try {
            Path(destFile).parent.toString().let { parent ->
                DocumentFileCompat.mkdirs(context, parent)
                if (!DocumentFileCompat.doesExist(
                        context, parent
                    )
                ) throw IOException("mkdirs failed to parent $parent of $destFile")
                (DocumentFileCompat.fromFullPath(context, destFile, requiresWriteAccess = true)
                    ?: DocumentFile.fromFile(File(parent)).createFile(
                        "application/*", DocumentFileCompat.getFileNameFromUrl(destFile)
                    )).let { documentFile ->
                    if (documentFile === null) {
                        throw IOException("DocumentFileCompat.fromFullPath and createFile returned null, $destFile")
                    }
                    documentFile.openOutputStream(context, false).let { stream ->
                        if (stream === null) {
                            throw IOException("openOutputStream returned null, $destFile")
                        }
                        return stream
                    }
                }
            }
        } catch (e: IOException) {
            throw IOException("Failed to open file stream", e)
        }
    }

    suspend fun retrieveFileAsync(
        sourceFile: String, destFile: String, progressListener: (Long, Long) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            retrieveFile(sourceFile, destFile, progressListener)
        }
    }
}