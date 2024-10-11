package com.kushwaha.musicplayer

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
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
    private var showRenameDialog by mutableStateOf(false)
    private var songToRename by mutableStateOf("") // Song name to rename
    private var newSongName by mutableStateOf("") // New song name
    private var selectedTabIndex by mutableStateOf(0) // 0 for All Songs, 1 for Favorites

    private lateinit var sharedPreferences: SharedPreferences
    private val favoritesKey = "favorites"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("MusicPlayerPrefs", MODE_PRIVATE)
        loadFavorites()

        setContent {
            MusicPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF6F7575)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color(0xFF3D3D3D)) {
                            Tab(
                                selected = selectedTabIndex == 0,
                                onClick = { selectedTabIndex = 0 },
                                text = { Text("All Songs", color = Color.White) }
                            )
                            Tab(
                                selected = selectedTabIndex == 1,
                                onClick = { selectedTabIndex = 1 },
                                text = { Text("Favorites", color = Color.White) }
                            )
                        }

                        // Song List UI based on selected tab
                        if (selectedTabIndex == 0) {
                            SongListUI(
                                MusicPlayerState.musicList,
                                searchQuery,
                                onSearchQueryChanged = { searchQuery = it },
                                onPlayPauseClick = { songPath ->
                                    playSong(songPath, isNewSong = true)
                                    ShowBottomSheetState.showBottomSheet = true // Show the bottom sheet when a song is played
                                },
                                onRenameSong = { songName, songPath ->
                                    songToRename = songName
                                    newSongName = songName
                                    showRenameDialog = true
                                },
                                onToggleFavorite = { songName -> toggleFavorite(songName) }
                            )
                        } else {
                            SongListUI(
                                MusicPlayerState.favoriteSongs.map { songName ->
                                    MusicPlayerState.musicList.find { it.first == songName } ?: Pair(songName, "")
                                },
                                searchQuery,
                                onSearchQueryChanged = { searchQuery = it },
                                onPlayPauseClick = { songPath ->
                                    playSong(songPath, isNewSong = true)
                                    ShowBottomSheetState.showBottomSheet = true // Show the bottom sheet when a song is played
                                },
                                onRenameSong = { songName, songPath ->
                                    songToRename = songName
                                    newSongName = songName
                                    showRenameDialog = true
                                },
                                onToggleFavorite = { songName -> toggleFavorite(songName) }
                            )
                        }

                        // Bottom Sheet for Now Playing
                        if (ShowBottomSheetState.showBottomSheet) {
                            NowPlayingSheet() // Display Now Playing sheet
                        }

                        // Rename Dialog
                        if (showRenameDialog) {
                            RenameDialog(
                                songToRename,
                                newSongName,
                                onRename = { newName ->
                                    renameSong(songToRename, newName,this@MainActivity)
                                    songToRename = newName
                                    showRenameDialog = false
                                },
                                onDismiss = { showRenameDialog = false }
                            )
                        }
                    }
                }
            }
        }
        checkAndRequestPermissions()
    }

    fun toggleFavorite(songTitle: String) {
        if (MusicPlayerState.favoriteSongs.contains(songTitle)) {
            MusicPlayerState.favoriteSongs.remove(songTitle) // Remove from favorites
        } else {
            MusicPlayerState.favoriteSongs.add(songTitle) // Add to favorites
        }
    }

     fun saveFavorites() {
        val editor = sharedPreferences.edit()
        editor.putStringSet(favoritesKey, MusicPlayerState.favoriteSongs.toSet())
        editor.apply()
    }

    private fun loadFavorites() {
        val favoritesSet = sharedPreferences.getStringSet(favoritesKey, setOf())
        MusicPlayerState.favoriteSongs.clear()
        MusicPlayerState.favoriteSongs.addAll(favoritesSet ?: setOf())
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
