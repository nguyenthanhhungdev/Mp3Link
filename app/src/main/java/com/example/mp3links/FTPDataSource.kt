package com.example.mp3links

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPReply
import org.apache.commons.net.ftp.FTPSClient
import org.apache.commons.net.io.CopyStreamEvent
import org.apache.commons.net.io.CopyStreamListener
import org.apache.commons.net.util.TrustManagerUtils
import java.io.FileOutputStream

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
        sourceFile: String,
        destFile: String,
        progressListener: (Long, Long) -> Unit
    ) {
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
        client.retrieveFile(sourceFile, FileOutputStream(destFile))
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