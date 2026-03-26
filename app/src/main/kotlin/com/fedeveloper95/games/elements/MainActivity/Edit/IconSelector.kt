package com.fedeveloper95.games.elements.MainActivity.Edit

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fedeveloper95.games.R
import com.fedeveloper95.games.elements.ui.GoogleSansFlex

@Composable
fun ExpressiveTextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 15 else 50,
        animationSpec = tween(durationMillis = 200),
        label = "btnMorph"
    )

    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(cornerPercent),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun IconSelector(
    currentIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val icons = listOf(
        "default" to Icons.Default.Gamepad,
        "bolt" to ImageVector.vectorResource(R.drawable.bolt),
        "heart" to ImageVector.vectorResource(R.drawable.heart),
        "star" to ImageVector.vectorResource(R.drawable.star)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.edit_app_icon_title),
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                val rows = icons.chunked(4)
                rows.forEachIndexed { index, rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { (name, icon) ->
                            val isSelected = currentIcon == name
                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()

                            val cornerPercent by animateIntAsState(
                                targetValue = if (isPressed || isSelected) 25 else 50,
                                animationSpec = tween(durationMillis = 200),
                                label = "iconCorner"
                            )

                            val containerColor by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                animationSpec = tween(durationMillis = 200),
                                label = "iconContainerColor"
                            )

                            val iconTint by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                animationSpec = tween(durationMillis = 200),
                                label = "iconTintColor"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(cornerPercent))
                                    .background(containerColor)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null
                                    ) { onIconSelected(name) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = name,
                                    tint = iconTint,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        val remaining = 4 - rowItems.size
                        if (remaining > 0) {
                            repeat(remaining) {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                    if (index < rows.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            ExpressiveTextButton(
                onClick = onDismiss,
                text = stringResource(R.string.cancel)
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(32.dp)
    )
}