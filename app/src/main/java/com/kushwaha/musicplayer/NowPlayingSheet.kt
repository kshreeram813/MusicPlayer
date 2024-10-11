package com.kushwaha.musicplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

object ShowBottomSheetState {
    var showBottomSheet by mutableStateOf(false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingSheet() {
    ModalBottomSheet(
        onDismissRequest = { ShowBottomSheetState.showBottomSheet = false },
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
            IconButton(onClick = { ShowBottomSheetState.showBottomSheet = false }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Close and go back",
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
                text = MediaPlayerState.currentSong ?: "Select a Song",
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
                    value = MediaPlayerState.songProgress,
                    onValueChange = { newValue ->
                        val newPosition = (newValue * songDurationState.songDuration).toInt()
                        MediaPlayerState.mediaPlayer?.seekTo(newPosition)
                        MediaPlayerState.songProgress = newValue
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
                    Text(text = songDurationState.currentTime, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6A6A6A)))
                    Text(text = songDurationState.endTime, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6A6A6A)))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MonomorphicButton(iconRes = R.drawable.ic_prev, onClick = { previousSong() })
                MonomorphicButton(
                    iconRes = if (MediaPlayerState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                    onClick = { if (MediaPlayerState.isPlaying) pauseSong() else playSong(MediaPlayerState.currentSong!!, false) }
                )
                MonomorphicButton(iconRes = R.drawable.ic_next, onClick = { nextSong() })
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Motivational message
            Text(
                text = "🎵 Enjoy the music and let it brighten your day. 🎶 Keep having fun with your favorite songs!",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF0097A7),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}