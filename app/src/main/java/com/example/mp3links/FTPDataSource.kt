package com.example.mp3links

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPReply
import org.apache.commons.net.ftp.FTPSClient
import org.apache.commons.net.io.CopyStreamEvent
import org.apache.commons.net.io.CopyStreamListener
import org.apache.commons.net.util.TrustManagerUtils
import java.io.FileOutputStream

private const val TAG = "FTPDataSource"

class FTPDataSource(
    private val sourceIP: String,
    private val sourcePort: Int,
    private val username: String,
    private val password: String
) {
    private val client: FTPSClient = FTPSClient("TLS", true)

    init {
        client.trustManager = TrustManagerUtils.getAcceptAllTrustManager()
    }

    private var currentFileTotalSize: Long = -1
    private fun retrieveFile(
        sourceFile: String, destFile: String, progressListener: (Long, Long) -> Unit
    ) {
        Log.i(TAG, "retrieveFile: downloading file $sourceFile into $destFile")
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
            if (client.size(sourceFile) == FTPReply.FILE_STATUS) client.replyString.split(" ")[1].toLong() else -1
        Log.d(TAG, "retrieveFile: download start with size $currentFileTotalSize")
        client.retrieveFile(sourceFile, FileOutputStream(destFile))
        Log.d(TAG, "retrieveFile: disconnecting from the server")
        client.logout()
        client.disconnect()
    }

    suspend fun retrieveFileAsync(
        sourceFile: String, destFile: String, progressListener: (Long, Long) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            retrieveFile(sourceFile, destFile, progressListener)
        }
    }
}