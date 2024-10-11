package com.kushwaha.musicplayer

import android.util.Log
import java.io.File

fun renameSong(oldName: String, newName: String,activity: MainActivity) {
    try {
        val songPath = MusicPlayerState.musicList.find { it.first == oldName }?.second
        Log.d("RenameSong", "Old file path: $songPath")

        if (songPath != null) {
            val oldFile = File(songPath)

            // Check if the old file exists
            if (!oldFile.exists()) {
                Log.e("RenameSong", "Old file does not exist: $oldName")
                return
            }

            // Create the new file object
            val newFile = File(oldFile.parent, newName)

            // Check if the new file already exists
            if (newFile.exists()) {
                Log.e("RenameSong", "File with the new name already exists")
                return
            }

            // Perform the renaming
            if (oldFile.renameTo(newFile)) {
                Log.d("RenameSong", "Renamed successfully: $oldName to $newName")

                // Update the music list state immediately
                val index = MusicPlayerState.musicList.indexOfFirst { it.first == oldName }
                if (index != -1) {
                    MusicPlayerState.musicList[index] = Pair(newName, newFile.path)
                }

                // Save the updated favorites
                activity.saveFavorites()
            } else {
                Log.e("RenameSong", "Failed to rename file")
            }
        } else {
            Log.e("RenameSong", "Song not found: $oldName")
        }
    } catch (e: Exception) {
        Log.e("RenameSong", "Exception during renaming: ${e.message}")
        e.printStackTrace()
    }
}