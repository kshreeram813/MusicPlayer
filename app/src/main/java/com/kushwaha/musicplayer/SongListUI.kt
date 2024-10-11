package com.kushwaha.musicplayer

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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun SongListUI(
    musicList: List<Pair<String, String>>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onPlayPauseClick: (String) -> Unit,
    onRenameSong: (String, String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    var showSearchBox by remember { mutableStateOf(false) }

    // Filter music list based on search query
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
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // Search Box
            if (showSearchBox) {
                SearchBox(
                    searchQuery = searchQuery,
                    onSearchQueryChanged = onSearchQueryChanged,
                    playingSongColor = playingSongColor,
                    onClose = { showSearchBox = false }
                )
            }

            // Header
            Text(
                text = if (showSearchBox) "Search Results" else "My List",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFF6A6A6A),
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Song List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(filteredMusicList) { song ->
                    SongItem(
                        song = song,
                        isPlaying = song.first == MediaPlayerState.currentSong,
                        isFavorite = MusicPlayerState.favoriteSongs.contains(song.first), // Pass favorite status
                        onPlayPauseClick = onPlayPauseClick,
                        onToggleFavorite = onToggleFavorite,
                        onRenameSong = onRenameSong
                    )
                }
            }
        }

        // Floating Button for Search Box
        FloatingActionButton(
            onClick = { showSearchBox = !showSearchBox },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = playingSongColor
        ) {
            Icon(
                painter = painterResource(id = if (showSearchBox) R.drawable.close else R.drawable.ic_search),
                contentDescription = if (showSearchBox) "Close Search" else "Open Search",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SearchBox(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    playingSongColor: Color,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, playingSongColor, shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "Search",
            tint = playingSongColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

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

        Icon(
            painter = painterResource(id = R.drawable.close),
            contentDescription = "Close",
            tint = Color.Red,
            modifier = Modifier
                .size(24.dp)
                .clickable { onClose() }
        )
    }
}

@Composable
fun SongItem(
    song: Pair<String, String>,
    isPlaying: Boolean,
    isFavorite: Boolean, // Accept the favorite state
    onPlayPauseClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRenameSong: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val playingSongColor = Color(0xFF76C7C0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isPlaying) playingSongColor.copy(alpha = 0.3f) else Color.White)
            .border(1.dp, if (isPlaying) playingSongColor else Color(0xFFE0E0E0), shape = RoundedCornerShape(12.dp))
            .clickable { onPlayPauseClick(song.second) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.img), // Placeholder image
            contentDescription = "Album Art",
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = song.first,
            style = MaterialTheme.typography.titleMedium.copy(
                color = if (isPlaying) playingSongColor else Color(0xFF6A6A6A)
            ),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )

        // Favorite button
        IconButton(onClick = { onToggleFavorite(song.first) }) {
            Image(
                painter = painterResource(id = if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border),
                contentDescription = "Toggle Favorite",
                modifier = Modifier.size(24.dp)
            )
        }

        IconButton(onClick = { expanded = true }) {
            Icon(painter = painterResource(id = R.drawable.ic_threedot), contentDescription = "Options")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
            offset = DpOffset(x = (200).dp, y = 0.dp) // Adjust the offset to match the desired position
        ) {
            DropdownMenuItem(
                text = {
                    Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites")
                },
                onClick = {
                    onToggleFavorite(song.first)
                    expanded = false
                }
            )

            DropdownMenuItem(text = { Text("Rename") }, onClick = {
                onRenameSong(song.first, song.second) // Provide song title and path
                expanded = false
            })
        }
    }
}