package com.fedeveloper95.games.elements.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fedeveloper95.games.services.mainactivity.GameViewModel

@Composable
fun AppIcon(
    packageName: String,
    modifier: Modifier = Modifier,
    customIconUri: String? = null,
    viewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current
    val cacheKey = customIconUri ?: packageName

    // Prova a leggere subito dalla cache per evitare sfarfallii iniziali se già presente
    var bitmap by remember(cacheKey) { mutableStateOf(viewModel.getCachedBitmap(cacheKey)) }
    var builtInIconData by remember(cacheKey) { mutableStateOf(viewModel.getCachedVectorIcon(cacheKey)) }

    // Carica asincronamente solo se non è già in cache
    if (bitmap == null && builtInIconData == null) {
        LaunchedEffect(packageName, customIconUri) {
            val loadedData = viewModel.loadIcon(context, packageName, customIconUri)
            if (loadedData is Pair<*, *>) {
                @Suppress("UNCHECKED_CAST")
                builtInIconData = loadedData as Pair<androidx.compose.ui.graphics.vector.ImageVector, Color>
            } else if (loadedData is androidx.compose.ui.graphics.ImageBitmap) {
                bitmap = loadedData
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