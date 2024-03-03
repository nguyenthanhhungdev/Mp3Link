package com.example.mp3links

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class Helper {
    companion object {
        fun getGoogleSignInClient(context: Context): GoogleSignInClient {
            val signInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
//                .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE))
                    .build()

            return GoogleSignIn.getClient(context, signInOptions)
        }

        fun getFakeAlbumListString(context: Context) =
            context.assets.open("album_list_test.json").bufferedReader().use { it.readText() }
    }
}