package com.kushwaha.musicplayer

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@Composable
fun MoreOptionsButton(
    onSleepModeSelected: () -> Unit,
    onAudioEffectSelected: () -> Unit,
    modifier: Modifier = Modifier // Add this line to accept a modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) { // Use the modifier here
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .padding(end = 12.dp) // Space from the right to fit next to the search button
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_threedot), // Replace with your three-dot icon resource
                contentDescription = "More options",
            )
        }
        // Popup menu for options
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Sleep Mode") },
                onClick = {
                    onSleepModeSelected()
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Audio Effects") },
                onClick = {
                    onAudioEffectSelected()
                    expanded = false
                }
            )
        }
    }
}
