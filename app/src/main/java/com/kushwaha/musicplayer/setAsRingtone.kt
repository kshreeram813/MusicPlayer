package com.kushwaha.musicplayer

import android.content.ContentValues
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.provider.MediaStore
import java.io.File

// Function to set the song as a ringtone
fun setAsRingtone(context: Context, songPath: String) {
    val uri = Uri.fromFile(File(songPath))

    // Check if the ringtone already exists
    val projection = arrayOf(MediaStore.Audio.Media._ID)
    val selection = "${MediaStore.Audio.Media.DATA} = ?"
    val selectionArgs = arrayOf(songPath)

    val cursor = context.contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )

    // If the ringtone already exists, do not insert it again
    if (cursor != null && cursor.moveToFirst()) {
        cursor.close()
        // Use the existing URI
        RingtoneManager.setActualDefaultRingtoneUri(
            context,
            RingtoneManager.TYPE_RINGTONE,
            uri
        )
        return
    }

    cursor?.close()

    // Prepare values for the new ringtone
    val values = ContentValues().apply {
        put(MediaStore.Audio.Media.DATA, songPath)
        put(MediaStore.Audio.Media.IS_RINGTONE, true)
        put(MediaStore.Audio.Media.IS_NOTIFICATION, false)
        put(MediaStore.Audio.Media.IS_MUSIC, true)
    }

    // Insert the ringtone into the media store
    val newUri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)

    // Set the ringtone using RingtoneManager
    newUri?.let {
        RingtoneManager.setActualDefaultRingtoneUri(
            context,
            RingtoneManager.TYPE_RINGTONE,
            it
        )
    }
}
