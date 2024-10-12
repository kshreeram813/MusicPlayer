import androidx.compose.material3.Text

//package com.kushwaha.musicplayer
//
//import android.content.ContentValues
//import android.content.Context
//import android.media.RingtoneManager
//import android.net.Uri
//import android.provider.MediaStore
//import java.io.File
//
//// Function to set the song as a ringtone
//fun setAsRingtone(songPath: String) {
//    val uri = Uri.fromFile(File(songPath)) // Convert the file path to a URI
//    val values = ContentValues().apply {
//        put(MediaStore.Audio.Media.DATA, songPath) // Use Media.Audio.Media.DATA
//        put(MediaStore.Audio.Media.IS_RINGTONE, true) // Use Media.Audio.Media.IS_RINGTONE
//        put(MediaStore.Audio.Media.IS_NOTIFICATION, false) // Use Media.Audio.Media.IS_NOTIFICATION
//        put(MediaStore.Audio.Media.IS_MUSIC, true) // Use Media.Audio.Media.IS_MUSIC
//    }
//
//    // Insert the ringtone into the media store
//    context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
//
//    // Set the ringtone using RingtoneManager
//    RingtoneManager.setActualDefaultRingtoneUri(
//        context,
//        RingtoneManager.TYPE_RINGTONE,
//        uri
//    )
//}


//
//// Set as ringtone option
//DropdownMenuItem(
//text = { Text("Set as Ringtone") },
//onClick = {
//    setAsRingtone(song.second) // Pass context and song path
//    expanded = false
//}
//)