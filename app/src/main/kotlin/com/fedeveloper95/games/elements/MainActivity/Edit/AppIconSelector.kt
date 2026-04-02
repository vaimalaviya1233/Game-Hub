package com.fedeveloper95.games.elements.MainActivity.Edit

import android.content.Intent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fedeveloper95.games.R
import com.fedeveloper95.games.elements.ui.AppIcon
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppIconSelector(
    onDismiss: () -> Unit,
    onAppSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val packageManager = context.packageManager

    val installedApps = remember {
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        packageManager.queryIntentActivities(intent, 0).mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            val name = resolveInfo.loadLabel(packageManager).toString()
            if (pkg != null) Pair(pkg, name) else null
        }.sortedBy { it.second }.distinctBy { it.first }
    }

    val filteredApps = remember(searchQuery, installedApps) {
        installedApps.filter { (_, name) ->
            searchQuery.isEmpty() || name.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Text(
                text = stringResource(R.string.select_app_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
            )

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = {
                    Text(
                        stringResource(R.string.search_apps),
                        fontFamily = GoogleSansFlex
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = GoogleSansFlex)
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(
                    items = filteredApps,
                    key = { _, app -> app.first }
                ) { index, app ->
                    GroupedIconAppItem(
                        packageName = app.first,
                        appName = app.second,
                        isSingle = filteredApps.size == 1,
                        isFirst = index == 0,
                        isLast = index == filteredApps.size - 1,
                        onSelect = {
                            scope.launch {
                                sheetState.hide()
                                onAppSelected(app.first)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupedIconAppItem(
    packageName: String,
    appName: String,
    isSingle: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onSelect: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topRound = if (isSingle || isFirst) 20.dp else 4.dp
    val bottomRound = if (isSingle || isLast) 20.dp else 4.dp

    val targetTopStart = if (isPressed) 20.dp else topRound
    val targetTopEnd = if (isPressed) 20.dp else topRound
    val targetBottomStart = if (isPressed) 20.dp else bottomRound
    val targetBottomEnd = if (isPressed) 20.dp else bottomRound

    val topStart by animateDpAsState(targetTopStart, tween(200), label = "")
    val topEnd by animateDpAsState(targetTopEnd, tween(200), label = "")
    val bottomStart by animateDpAsState(targetBottomStart, tween(200), label = "")
    val bottomEnd by animateDpAsState(targetBottomEnd, tween(200), label = "")

    val shape = RoundedCornerShape(topStart, topEnd, bottomStart, bottomEnd)

    val bgColor = if (isPressed) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(shape)
            .background(bgColor)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onSelect
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(
            packageName = packageName,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = appName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = GoogleSansFlex
            )
        }
    }
}