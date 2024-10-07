package com.kushwaha.musicplayer

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kushwaha.musicplayer.ui.theme.MusicPlayerTheme
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    private var songDuration by mutableStateOf(0)
    private var elapsedTime by mutableStateOf(0)
    private var mediaPlayer: MediaPlayer? = null
    private var musicList = mutableStateListOf<Pair<String, String>>()
    private var searchQuery by mutableStateOf("")
    private var isPlaying by mutableStateOf(false)
    private var currentSong by mutableStateOf<String?>(null)
    private var currentSongIndex = 0
    private var showBottomSheet by mutableStateOf(false)
    private var songProgress by mutableStateOf(0f)
    private var job: Job? = null // Job for managing coroutine
    private var currentTime by mutableStateOf("00:00")
    private var endTime by mutableStateOf("00:00") // This will represent the remaining time


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF6F7575)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SongListUI(musicList, searchQuery, onSearchQueryChanged = { searchQuery = it }) { songPath ->
                            currentSong = musicList.find { it.second == songPath }?.first
                            playSong(songPath, isNewSong = true)
                            showBottomSheet = true // Show the bottom sheet when a song is selected
                        }
                        // Bottom Sheet for Now Playing
                        if (showBottomSheet) {
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
                fetchMusicFiles()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            } else {
                fetchMusicFiles()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && android.os.Environment.isExternalStorageManager()) {
                fetchMusicFiles()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchMusicFiles()
        }
    }

    private fun fetchMusicFiles() {
        musicList.clear()
        val contentResolver: ContentResolver = contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            while (it.moveToNext()) {
                val songName = it.getString(nameColumn)
                val songPath = it.getString(dataColumn)
                musicList.add(Pair(songName, songPath))
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NowPlayingSheet() {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color(0xFFF7E7E7)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                IconButton(onClick = { showBottomSheet = false }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Close",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Image(
                    painter = painterResource(id = R.drawable.img),
                    contentDescription = "Album Art",
                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(50.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentSong ?: "Select a Song",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF6A6A6A),
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .wrapContentHeight()
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .padding(4.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .horizontalScroll(rememberScrollState())
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Slider for song progress
                Column {
                    Slider(
                        value = songProgress,
                        onValueChange = { newValue ->
                            val newPosition = (newValue * songDuration).toInt()
                            mediaPlayer?.seekTo(newPosition)
                            songProgress = newValue
                        },
                        modifier = Modifier.fillMaxWidth(),
                        valueRange = 0f..1f,
                        steps = 0
                    )

                    // Timer text below the slider, in a single line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = currentTime, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6A6A6A)))
                        Text(text = endTime, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6A6A6A)))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Control buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NeumorphicButton(iconRes = R.drawable.ic_prev, onClick = { previousSong() })
                    NeumorphicButton(
                        iconRes = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                        onClick = { if (isPlaying) pauseSong() else playSong(currentSong!!, false) }
                    )
                    NeumorphicButton(iconRes = R.drawable.ic_next, onClick = { nextSong() })
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Motivational message
                Text(
                    text = "🎵 Thank you for listening! Enjoy the music and let it brighten your day. 🎶 Keep having fun with your favorite songs!",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF6A6A6A),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }



    private fun formatTime(milliseconds: Int): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
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
                        val isPlaying = song.first == currentSong

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


    private fun playSong(songPath: String, isNewSong: Boolean) {
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

    private fun pauseSong() {
        mediaPlayer?.pause()
        job?.cancel() // Cancel the progress update coroutine
        isPlaying = false
    }

    private fun nextSong() {
        currentSongIndex = (currentSongIndex + 1) % musicList.size
        val nextSongPath = musicList[currentSongIndex].second
        playSong(nextSongPath, isNewSong = true)
    }

    private fun previousSong() {
        currentSongIndex = if (currentSongIndex - 1 < 0) musicList.size - 1 else currentSongIndex - 1
        val prevSongPath = musicList[currentSongIndex].second
        playSong(prevSongPath, isNewSong = true)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        job?.cancel() // Cancel the progress update coroutine
    }
}

@Composable
fun NeumorphicButton(iconRes: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clickable(
                onClick = onClick,
                indication = null, // Remove the ripple effect
                interactionSource = remember { MutableInteractionSource() } // Required to suppress ripple effect
            )
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(28.dp)) // Add border
            .padding(4.dp), // Add some padding to create a subtle effect
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}