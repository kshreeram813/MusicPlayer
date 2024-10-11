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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SongListUI(
        musicList: List<Pair<String, String>>,
        searchQuery: String,
        onSearchQueryChanged: (String) -> Unit,
        onPlayPauseClick: (String) -> Unit
    ) {
        // Track whether the search box is visible or not
        var showSearchBox by remember { mutableStateOf(false) }

        // Filter the music list based on search query when search is active
        val filteredMusicList = if (showSearchBox && searchQuery.isNotEmpty()) {
            musicList.filter { it.first.contains(searchQuery, ignoreCase = true) }
        } else {
            musicList
        }

        val playingSongColor = Color(0xFF76C7C0) // Highlight color for the playing song

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F7F9)) // Light background for neumorphism
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Show the search box if 'showSearchBox' is true
                if (showSearchBox) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(24.dp)) // Rounded corners for the search box
                            .background(Color.White, shape = RoundedCornerShape(24.dp))
                            .border(1.dp, playingSongColor, shape = RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search), // Replace with search icon resource
                            contentDescription = "Search",
                            tint = playingSongColor,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Input field for search query
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChanged,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Search Songs") },
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = playingSongColor,
                                unfocusedIndicatorColor = Color.Gray,
                                cursorColor = playingSongColor,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            singleLine = true
                        )


                        Spacer(modifier = Modifier.width(8.dp))

                        // Close button to collapse the search box
                        Icon(
                            painter = painterResource(id = R.drawable.close), // Replace with close icon resource
                            contentDescription = "Close",
                            tint = Color.Red,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    showSearchBox = false // Collapse the search box
                                }
                        )
                    }
                }

                // "My List" Header
                Text(
                    text = if (showSearchBox) "Search Results" else "My List",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color(0xFF6A6A6A),
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Display the list of songs (either filtered or full list)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(filteredMusicList) { song ->
                        // Determine if this song is currently playing
                        val isPlaying = song.first == MediaPlayerState.currentSong

                        // Song Item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isPlaying) playingSongColor.copy(alpha = 0.3f) else Color(0xFFFFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    color = if (isPlaying) playingSongColor else Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onPlayPauseClick(song.second)
                                    showSearchBox = false // Close search box on song selection
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img), // Placeholder image
                                contentDescription = "Album Art",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // Song Title
                            Text(
                                text = song.first,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = if (isPlaying) playingSongColor else Color(0xFF6A6A6A)
                                ),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Floating button to open or close the search box
            FloatingActionButton(
                onClick = { showSearchBox = !showSearchBox }, // Toggle the search box when clicked
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = playingSongColor
            ) {
                Icon(
                    painter = painterResource(id = if (showSearchBox) R.drawable.close else R.drawable.ic_search), // Change icon based on state
                    contentDescription = if (showSearchBox) "Close Search" else "Open Search",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaPlayerState.mediaPlayer?.release()
        MediaPlayerState.job?.cancel() // Cancel the progress update coroutine
    }
}
