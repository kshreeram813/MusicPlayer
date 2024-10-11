package com.kushwaha.musicplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kushwaha.musicplayer.ui.theme.MusicPlayerTheme

class MainActivity : ComponentActivity() {
    private var searchQuery by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF6F7575)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SongListUI(MusicPlayerState.musicList, searchQuery, onSearchQueryChanged = { searchQuery = it }) { songPath ->
                            MediaPlayerState.currentSong = MusicPlayerState.musicList.find { it.second == songPath }?.first
                            playSong(songPath, isNewSong = true)
                            ShowBottomSheetState.showBottomSheet = true // Show the bottom sheet when a song is selected
                        }
                        // Bottom Sheet for Now Playing
                        if (ShowBottomSheetState.showBottomSheet) {
                            NowPlayingSheet()
                        }
                    }
                }
            }
        }
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:" + this.packageName)
                startActivityForResult(intent, 101)
            } else {
                fetchMusicFiles(this)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            } else {
                fetchMusicFiles(this)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && android.os.Environment.isExternalStorageManager()) {
                fetchMusicFiles(this)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchMusicFiles(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaPlayerState.mediaPlayer?.release()
        MediaPlayerState.job?.cancel() // Cancel the progress update coroutine
    }
}
