package com.fedeveloper95.games.elements.AdvancedSettingsActivity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.fedeveloper95.games.R
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun RestartPopup(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 15 else 50,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnMorph"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null) },
        title = {
            Text(
                text = stringResource(R.string.dialog_restart_title),
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.dialog_restart_desc),
                fontFamily = GoogleSansFlex
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        confirmButton = {
            Button(
                onClick = {
                    val packageManager = context.packageManager
                    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
                    val componentName = intent?.component
                    val mainIntent = Intent.makeRestartActivityTask(componentName)
                    context.startActivity(mainIntent)
                    Runtime.getRuntime().exit(0)
                },
                shape = RoundedCornerShape(cornerPercent),
                interactionSource = interactionSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(R.string.restart_now),
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

suspend fun exportSettings(context: Context, uri: Uri) {
    withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
            val settings = context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE)

            val prefsJson = JSONObject()
            prefs.all.forEach { (k, v) ->
                when (v) {
                    is Set<*> -> prefsJson.put(k, JSONArray(v))
                    else -> prefsJson.put(k, v)
                }
            }
            root.put("game_hub_prefs", prefsJson)

            val settingsJson = JSONObject()
            settings.all.forEach { (k, v) ->
                when (v) {
                    is Set<*> -> settingsJson.put(k, JSONArray(v))
                    else -> settingsJson.put(k, v)
                }
            }
            root.put("game_hub_settings", settingsJson)

            context.contentResolver.openOutputStream(uri)?.use {
                it.write(root.toString().toByteArray())
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, context.getString(R.string.export_success), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, context.getString(R.string.export_error), Toast.LENGTH_SHORT).show()
            }
        }
    }
}

suspend fun importSettings(context: Context, uri: Uri): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val sb = StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        sb.append(line)
                        line = reader.readLine()
                    }
                }
            }
            val root = JSONObject(sb.toString())

            if (root.has("game_hub_prefs")) {
                val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
                val editor = prefs.edit().clear()
                val json = root.getJSONObject("game_hub_prefs")
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    when (val value = json.get(key)) {
                        is Boolean -> editor.putBoolean(key, value)
                        is Int -> editor.putInt(key, value)
                        is Long -> editor.putLong(key, value)
                        is Double -> editor.putFloat(key, value.toFloat())
                        is String -> editor.putString(key, value)
                        is JSONArray -> {
                            val set = mutableSetOf<String>()
                            for (i in 0 until value.length()) set.add(value.getString(i))
                            editor.putStringSet(key, set)
                        }
                    }
                }
                editor.apply()
            }

            if (root.has("game_hub_settings")) {
                val settings = context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE)
                val editor = settings.edit().clear()
                val json = root.getJSONObject("game_hub_settings")
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = json.get(key)

                    if (key == "pref_stats_interval") {
                        editor.putFloat(key, (value as? Number)?.toFloat() ?: 3f)
                        continue
                    }

                    when (value) {
                        is Boolean -> editor.putBoolean(key, value)
                        is Int -> editor.putInt(key, value)
                        is Long -> editor.putLong(key, value)
                        is Double -> editor.putFloat(key, value.toFloat())
                        is String -> editor.putString(key, value)
                        is JSONArray -> {
                            val set = mutableSetOf<String>()
                            for (i in 0 until value.length()) set.add(value.getString(i))
                            editor.putStringSet(key, set)
                        }
                    }
                }
                editor.apply()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, context.getString(R.string.import_error), Toast.LENGTH_SHORT).show()
            }
            false
        }
    }
}