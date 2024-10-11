package com.kushwaha.musicplayer

import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

var songDuration by mutableStateOf(0)
private var elapsedTime by mutableStateOf(0)
var mediaPlayer: MediaPlayer? = null
var musicList = mutableStateListOf<Pair<String, String>>()
var isPlaying by mutableStateOf(false)
var currentSong by mutableStateOf<String?>(null)
private var currentSongIndex = 0
var songProgress by mutableStateOf(0f)
private var job: Job? = null // Job for managing coroutine
var currentTime by mutableStateOf("00:00")
var endTime by mutableStateOf("00:00") // This will represent the remaining time

 fun playSong(songPath: String, isNewSong: Boolean) {
    if (isNewSong) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(songPath)
            prepare()
            start()
            songDuration = duration
            elapsedTime = 0
            currentSongIndex = musicList.indexOfFirst { it.second == songPath }
            currentSong = musicList[currentSongIndex].first

            // Automatically play the next song when the current song finishes
            setOnCompletionListener {
                nextSong()
            }
        }

        // Launch a coroutine to update progress
        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                if (mediaPlayer?.isPlaying == true) {
                    elapsedTime = mediaPlayer?.currentPosition ?: 0
                    songProgress = elapsedTime.toFloat() / songDuration

                    // Update current time
                    currentTime = formatTime(elapsedTime)

                    // Update end time as remaining time
                    endTime = formatTime(songDuration - elapsedTime)
                }
                delay(1000) // Update every second
            }
        }
        isPlaying = true
    } else {
        mediaPlayer?.start()
        isPlaying = true
    }
}

 fun pauseSong() {
    mediaPlayer?.pause()
    job?.cancel() // Cancel the progress update coroutine
    isPlaying = false
}

 fun nextSong() {
    currentSongIndex = (currentSongIndex + 1) % musicList.size
    val nextSongPath = musicList[currentSongIndex].second
    playSong(nextSongPath, isNewSong = true)
}

 fun previousSong() {
    currentSongIndex = if (currentSongIndex - 1 < 0) musicList.size - 1 else currentSongIndex - 1
    val prevSongPath = musicList[currentSongIndex].second
    playSong(prevSongPath, isNewSong = true)
}

private fun formatTime(milliseconds: Int): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}