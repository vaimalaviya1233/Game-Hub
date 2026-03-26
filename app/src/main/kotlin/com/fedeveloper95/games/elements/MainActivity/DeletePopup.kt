package com.fedeveloper95.games.elements.MainActivity

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.fedeveloper95.games.services.mainactivity.GameApp
import com.fedeveloper95.games.R

@Composable
fun DeletePopup(
    game: GameApp,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 15 else 50,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnMorph"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Delete, null) },
        title = { Text(stringResource(R.string.remove_game_title)) },
        text = { Text(stringResource(R.string.remove_game_desc, game.name)) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(cornerPercent),
                interactionSource = interactionSource
            ) { Text(stringResource(R.string.remove)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}