package com.example.mp3links

import android.content.Context

class Helper {
    companion object {
        fun getFakeAlbumListString(context: Context) =
            context.assets.open("album_list_test.json").bufferedReader().use { it.readText() }
    }
}