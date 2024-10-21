package com.kushwaha.musicplayer

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
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

    // New variables for folder management
    private var folderName by mutableStateOf("")
    private var showCreateFolderDialog by mutableStateOf(false)
    private val foldersKey = "folders" // Key for storing folders

    object showSearchBoxState {
        var showSearchBox by mutableStateOf(false)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        sharedPreferences = getSharedPreferences("MusicPlayerPrefs", MODE_PRIVATE)
        loadFavorites()
        loadFolders() // Load folders from SharedPreferences


        setContent {
            MusicPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFFFFF)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        // App Header
                        Text(
                            text = "MusicPlayer",
                            style = MaterialTheme.typography.headlineMedium.copy(color = Color.Black),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.End // Aligns elements to the end (right side)
                        ) {
                            // Floating Action Button for Search Box
                            FloatingActionButton(
                                onClick = {
                                    showSearchBoxState.showSearchBox =
                                        !showSearchBoxState.showSearchBox
                                },
                                modifier = Modifier
                                    .padding(end = 8.dp) // Add a little space between the buttons
                                    .size(56.dp), // Button size
                                containerColor = Color(0xFF76C7C0)
                            ) {
                                Icon(
                                    painter = painterResource(id = if (showSearchBoxState.showSearchBox) R.drawable.close else R.drawable.ic_search),
                                    contentDescription = if (showSearchBoxState.showSearchBox) "Close Search" else "Open Search",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            // More Options Button (Three-dot button)
                            MoreOptionsButton(
                                onSleepModeSelected = { handleSleepMode() },
                                onAudioEffectSelected = { handleAudioEffects() },
                                modifier = Modifier
                                    .size(56.dp)
                                    .padding(top = 4.dp) // Adjust this value to move the button down slightly
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp)) // Space between the header and the tabs

                        // Tab layout
                        TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color(
                            0xFFFFFFFF
                        )
                        ) {
                            Tab(
                                selected = selectedTabIndex == 0,
                                onClick = { selectedTabIndex = 0 },
                                text = { Text("Songs", color = Color.Black) }
                            )
                            Tab(
                                selected = selectedTabIndex == 1,
                                onClick = { selectedTabIndex = 1 },
                                text = { Text("My Favorites", color = Color.Black) }
                            )
                            Tab(
                                selected = selectedTabIndex == 2,
                                onClick = { selectedTabIndex = 2 },
                                text = { Text("Folder List", color = Color.Black) }
                            )
                            Tab(
                                selected = selectedTabIndex == 3,
                                onClick = { selectedTabIndex = 3 },
                                text = { Text("Recently Played", color = Color.Black) }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp)) // Additional gap between tabs and content

                        // Song List UI based on selected tab
                        when (selectedTabIndex) {
                            0 -> {
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
                            }
                            1 -> {
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

                            2 -> {
                                // Playlists Tab UI implementation
                                Box(modifier = Modifier.fillMaxSize()) { // Use Box to layer the FAB over the content
                                    Column {
                                        // LazyColumn for displaying folder names
                                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                                            items(MusicPlayerState.folders) { folder ->
                                                Text(
                                                    text = folder,
                                                    modifier = Modifier
                                                        .padding(8.dp)
                                                        .fillMaxWidth()
                                                        .clickable { /* Handle folder click */ }
                                                        .border(
                                                            1.dp,
                                                            Color.Gray,
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(8.dp)
                                                )
                                            }
                                        }
                                    }
                                    // Floating Action Button for creating a folder
                                    FloatingActionButton(
                                        onClick = { showCreateFolderDialog = true },
                                        modifier = Modifier
                                            .padding(end = 8.dp) // Add a little space between the buttons
                                            .size(85.dp)
                                            .align(Alignment.BottomEnd) // Aligns the FAB to the bottom end (right corner)
                                        .padding(bottom = 25.dp, end = 16.dp), // Adjusted padding to move it up
                                    containerColor = Color(0xFF76C7C0)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_createfolder),
                                            contentDescription = "Create Folder",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }

                            3 -> {
                                // Recently Played Tab UI implementation (to be added)
                            }
                        }

                        // Create Folder Dialog
                        if (showCreateFolderDialog) {
                            CreateFolderDialog(
                                onDismiss = { showCreateFolderDialog = false },
                                onCreate = { folderName ->
                                    createFolder(folderName)
                                }
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
                                    renameSong(songToRename, newName, this@MainActivity)
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

    private fun createFolder(folderName: String) {
        if (folderName.isNotEmpty()) {
            MusicPlayerState.folders.add(folderName) // Add new folder
            saveFolders() // Save updated folders to SharedPreferences
            showCreateFolderDialog = false // Dismiss dialog
        }
    }

    private fun saveFolders() {
        val editor = sharedPreferences.edit()
        editor.putStringSet(foldersKey, MusicPlayerState.folders.toSet())
        editor.apply()
    }

    private fun loadFolders() {
        val foldersSet = sharedPreferences.getStringSet(foldersKey, setOf())
        MusicPlayerState.folders.clear()
        MusicPlayerState.folders.addAll(foldersSet ?: setOf())
    }

    fun toggleFavorite(songTitle: String) {
        if (MusicPlayerState.favoriteSongs.contains(songTitle)) {
            MusicPlayerState.favoriteSongs.remove(songTitle) // Remove from favorites
        } else {
            MusicPlayerState.favoriteSongs.add(songTitle) // Add to favorites
        }
        saveFavorites() // Save the updated favorites to SharedPreferences
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
    // Implement the handlers for sleep mode and audio effects
    private fun handleSleepMode() {
        // Logic to handle sleep mode
    }

    private fun handleAudioEffects() {
        // Logic to handle audio effects
    }
}
