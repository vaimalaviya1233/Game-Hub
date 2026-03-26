package com.fedeveloper95.games.elements.MainActivity.Edit

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fedeveloper95.games.elements.ui.GoogleSansFlex

@Composable
fun IconSelector(
    onDismiss: () -> Unit,
    onIconSelected: (String, Int) -> Unit
) {
    val icons = listOf(
        Pair("star", Icons.Rounded.Star),
        Pair("controller", Icons.Rounded.SportsEsports),
        Pair("bolt", Icons.Rounded.Bolt),
        Pair("heart", Icons.Rounded.Favorite),
        Pair("gamepad", Icons.Rounded.Gamepad),
        Pair("videogame", Icons.Rounded.VideogameAsset),
        Pair("toy", Icons.Rounded.SmartToy)
    )

    val colors = listOf(
        Color(0xFFB3261E), Color(0xFFD32F2F), Color(0xFFC2185B), Color(0xFF7B1FA2),
        Color(0xFF512DA8), Color(0xFF303F9F), Color(0xFF1976D2), Color(0xFF0288D1),
        Color(0xFF0097A7), Color(0xFF00796B), Color(0xFF388E3C), Color(0xFF689F38),
        Color(0xFFAFB42B), Color(0xFFFBC02D), Color(0xFFFFA000), Color(0xFFF57C00),
        Color(0xFFE64A19), Color(0xFF5D4037), Color(0xFF616161), Color(0xFF455A64)
    )

    var selectedIconName by remember { mutableStateOf<String?>(null) }
    var selectedColor by remember { mutableStateOf(colors[0]) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true, usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(36.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Personalizza",
                    fontFamily = GoogleSansFlex,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 8.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 72.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(icons) { (name, icon) ->
                        val isSelected = selectedIconName == name
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()

                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.85f else if (isSelected) 1.05f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = ""
                        )

                        val animatedContainerColor by animateColorAsState(
                            targetValue = if (isSelected) selectedColor else MaterialTheme.colorScheme.surfaceContainerHighest,
                            label = ""
                        )
                        val animatedIconColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                            label = ""
                        )

                        Surface(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .scale(scale)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { selectedIconName = name },
                            shape = RoundedCornerShape(if (isSelected) 24.dp else 32.dp),
                            color = animatedContainerColor
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = animatedIconColor,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(colors) { color ->
                        val isSelected = selectedColor == color
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()

                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.8f else if (isSelected) 1.2f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = ""
                        )

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(color)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { selectedColor = color }
                        )
                    }
                }

                val btnInteractionSource = remember { MutableInteractionSource() }
                val btnPressed by btnInteractionSource.collectIsPressedAsState()
                val btnScale by animateFloatAsState(
                    targetValue = if (btnPressed) 0.95f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = ""
                )

                Button(
                    onClick = {
                        selectedIconName?.let { name ->
                            val colorHex = selectedColor.toArgb()
                            val uri = "builtin://$name#$colorHex"
                            onIconSelected(uri, colorHex)
                        }
                    },
                    enabled = selectedIconName != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .height(64.dp)
                        .scale(btnScale),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    interactionSource = btnInteractionSource
                ) {
                    Text(
                        text = "Seleziona",
                        fontFamily = GoogleSansFlex,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}