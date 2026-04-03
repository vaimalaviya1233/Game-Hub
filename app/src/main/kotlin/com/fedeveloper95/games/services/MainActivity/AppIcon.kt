package com.fedeveloper95.games.elements.ui

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppIcon(
    packageName: String,
    modifier: Modifier = Modifier,
    customIconUri: String? = null
) {
    val context = LocalContext.current
    var bitmap by remember(packageName, customIconUri) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var builtInIconData by remember(customIconUri) { mutableStateOf<Pair<androidx.compose.ui.graphics.vector.ImageVector, Color>?>(null) }

    LaunchedEffect(packageName, customIconUri) {
        withContext(Dispatchers.IO) {
            builtInIconData = null
            bitmap = null
            try {
                if (customIconUri?.startsWith("app://") == true) {
                    val targetPkg = customIconUri.removePrefix("app://")
                    val drawable = context.packageManager.getApplicationIcon(targetPkg)
                    bitmap = drawable.toBitmap().asImageBitmap()
                } else if (customIconUri?.startsWith("builtin://") == true) {
                    // Formato: builtin://icon_name#AARRGGBB
                    val parts = customIconUri.removePrefix("builtin://").split("#")
                    val iconName = parts[0]
                    val color = if (parts.size > 1) Color(parts[1].toInt()) else Color.Gray

                    val iconVector = when (iconName) {
                        "star" -> Icons.Rounded.Star
                        "controller" -> Icons.Rounded.SportsEsports
                        "bolt" -> Icons.Rounded.Bolt
                        "heart" -> Icons.Rounded.Favorite
                        "gamepad" -> Icons.Rounded.Gamepad
                        "videogame" -> Icons.Rounded.VideogameAsset
                        "toy" -> Icons.Rounded.SmartToy
                        else -> Icons.Rounded.SportsEsports
                    }
                    builtInIconData = Pair(iconVector, color)
                } else if (!customIconUri.isNullOrEmpty()) {
                    val uri = Uri.parse(customIconUri)
                    val androidBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }

                    val softwareBitmap = androidBitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, false)
                    bitmap = softwareBitmap.asImageBitmap()
                } else {
                    val drawable = context.packageManager.getApplicationIcon(packageName)
                    bitmap = drawable.toBitmap().asImageBitmap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    val drawable = context.packageManager.getApplicationIcon(packageName)
                    bitmap = drawable.toBitmap().asImageBitmap()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    if (builtInIconData != null) {
        Icon(
            imageVector = builtInIconData!!.first,
            contentDescription = "App Icon",
            modifier = modifier,
            tint = builtInIconData!!.second
        )
    } else if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = "App Icon",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(modifier = modifier)
    }
}