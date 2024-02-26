package com.example.mp3links

import android.content.Context
import com.example.mp3link.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList

class DriveInstance {
    private var drive: Drive? = null
    fun requestInstance(context: Context) {
        GoogleSignIn.getLastSignedInAccount(context)?.let { googleAccount ->

            // get credentials
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE, DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = googleAccount.account!!

            // get Drive Instance
            drive = Drive
                .Builder(
                    AndroidHttp.newCompatibleTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
                )
                .setApplicationName(context.getString(R.string.app_name))
                .build()
        }
    }

    fun getAllFoldersFromDrive(): List<String> {
        val folders = mutableListOf<String>()

        drive?.files()?.list()?.setQ("mimeType='application/vnd.google-apps.folder'")?.execute()?.files?.forEach { file ->
            folders.add(file.name)
        }

        return folders
    }
}
