package com.fedeveloper95.games.elements.SettingsActivity

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.fedeveloper95.games.PREF_THEME
import com.fedeveloper95.games.R
import com.fedeveloper95.games.elements.ui.GoogleSansFlex

@Composable
fun ThemePopup(
    currentTheme: Int,
    onThemeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false),
        icon = {
            Icon(
                imageVector = Icons.Rounded.Palette,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.settings_theme_title),
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val options = listOf(
                    0 to stringResource(R.string.settings_theme_system),
                    1 to stringResource(R.string.settings_theme_light),
                    2 to stringResource(R.string.settings_theme_dark)
                )

                options.forEach { (index, title) ->
                    val isSelected = currentTheme == index
                    val containerColor =
                        if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                    val contentColor =
                        if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(containerColor)
                            .clickable {
                                prefs.edit().putInt(PREF_THEME, index).apply()
                                onThemeSelected(index)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = title,
                            fontFamily = GoogleSansFlex,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = contentColor
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 6.dp
    )
}