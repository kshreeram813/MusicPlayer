package com.kushwaha.musicplayer

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun MonomorphicButton(iconRes: Int, onClick: () -> Unit) {
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