package com.kushwaha.musicplayer

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateListOf

object MusicPlayerState {
    var musicList = mutableStateListOf<Pair<String, String>>()
}

fun fetchMusicFiles(context: Context) {
    MusicPlayerState.musicList.clear()
    val contentResolver: ContentResolver = context.contentResolver
    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA)
    val cursor = contentResolver.query(uri, projection, null, null, null)

    cursor?.use {
        val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        while (it.moveToNext()) {
            val songName = it.getString(nameColumn)
            val songPath = it.getString(dataColumn)
            MusicPlayerState.musicList.add(Pair(songName, songPath))
        }
    }
}
