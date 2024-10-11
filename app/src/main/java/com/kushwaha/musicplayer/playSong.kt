package com.kushwaha.musicplayer

import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private var elapsedTime by mutableStateOf(0)
private var currentSongIndex = 0

object SongDurationState {
    var songDuration by mutableStateOf(0) // This will hold the duration in milliseconds
    var currentTime by mutableStateOf("00:00")
    var endTime by mutableStateOf("00:00") // This will represent the remaining time
}
object MediaPlayerState {
     var mediaPlayer: MediaPlayer? = null
     var isPlaying by mutableStateOf(false)
     var currentSong by mutableStateOf<String?>(null)
     var songProgress by mutableStateOf(0f)
     var job: Job? = null // Job for managing coroutine
}

 fun playSong(songPath: String, isNewSong: Boolean) {
    if (isNewSong) {
        MediaPlayerState.mediaPlayer?.release()
        MediaPlayerState.mediaPlayer = MediaPlayer().apply {
            setDataSource(songPath)
            prepare()
            start()
            SongDurationState.songDuration = duration
            elapsedTime = 0
            currentSongIndex = MusicPlayerState.musicList.indexOfFirst { it.second == songPath }
            MediaPlayerState.currentSong = MusicPlayerState.musicList[currentSongIndex].first

            // Automatically play the next song when the current song finishes
            setOnCompletionListener {
                nextSong()
            }
        }

        // Launch a coroutine to update progress
        MediaPlayerState.job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                if (MediaPlayerState.mediaPlayer?.isPlaying == true) {
                    elapsedTime = MediaPlayerState.mediaPlayer?.currentPosition ?: 0
                    MediaPlayerState.songProgress = elapsedTime.toFloat() / SongDurationState.songDuration

                    // Update current time
                    SongDurationState.currentTime = formatTime(elapsedTime)

                    // Update end time as remaining time
                    SongDurationState.endTime = formatTime(SongDurationState.songDuration - elapsedTime)
                }
                delay(1000) // Update every second
            }
        }
        MediaPlayerState.isPlaying = true
    } else {
        MediaPlayerState.mediaPlayer?.start()
        MediaPlayerState.isPlaying = true
    }
}

 fun pauseSong() {
     MediaPlayerState.mediaPlayer?.pause()
     MediaPlayerState.job?.cancel() // Cancel the progress update coroutine
     MediaPlayerState.isPlaying = false
}

 fun nextSong() {
    currentSongIndex = (currentSongIndex + 1) % MusicPlayerState.musicList.size
    val nextSongPath = MusicPlayerState.musicList[currentSongIndex].second
    playSong(nextSongPath, isNewSong = true)
}

 fun previousSong() {
    currentSongIndex = if (currentSongIndex - 1 < 0) MusicPlayerState.musicList.size - 1 else currentSongIndex - 1
    val prevSongPath = MusicPlayerState.musicList[currentSongIndex].second
    playSong(prevSongPath, isNewSong = true)
}

private fun formatTime(milliseconds: Int): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}