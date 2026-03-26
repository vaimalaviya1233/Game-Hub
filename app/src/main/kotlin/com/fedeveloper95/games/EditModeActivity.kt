package com.fedeveloper95.games

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fedeveloper95.games.services.mainactivity.GameApp
import com.fedeveloper95.games.services.mainactivity.GameViewModel
import com.fedeveloper95.games.elements.ui.AppIcon
import com.fedeveloper95.games.elements.ui.ExpressiveIconButton
import com.fedeveloper95.games.elements.ui.GameHubTheme
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyListState

class EditModeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EditModeScreen(onBack = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditModeScreen(onBack: () -> Unit, viewModel: GameViewModel = viewModel()) {
    val context = LocalContext.current
    val games by viewModel.games.collectAsState()
    val prefs = remember { context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE) }

    val sortType = remember { mutableStateOf(prefs.getString("pref_sort_type", "Alphabetical") ?: "Alphabetical") }
    val currentCardStyle = remember { mutableStateOf(prefs.getString("pref_card_style", "Default") ?: "Default") }

    var showSaveDialog by remember { mutableStateOf(false) }

    val currentViewType = remember(currentCardStyle.value) {
        when (currentCardStyle.value) {
            "Grid" -> ViewType.Grid
            else -> ViewType.List
        }
    }

    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        val currentList = games.toMutableList()
        val fromIndex = from.index
        val toIndex = to.index

        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)
            viewModel.updateGamesOrder(currentList)
            if (sortType.value != "Custom") {
                viewModel.switchToCustomSort(context)
                sortType.value = "Custom"
            }
        }
    }

    val gridState = rememberLazyGridState()
    val reorderGridState = rememberReorderableLazyGridState(gridState) { from, to ->
        val currentList = games.toMutableList()
        val fromIndex = from.index
        val toIndex = to.index

        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)
            viewModel.updateGamesOrder(currentList)
            if (sortType.value != "Custom") {
                viewModel.switchToCustomSort(context)
                sortType.value = "Custom"
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadGames(context)
    }

    BackHandler {
        showSaveDialog = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.edit_mode_title),
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ExpressiveIconButton(
                        onClick = { showSaveDialog = true },
                        icon = Icons.Default.Close,
                        contentDescription = stringResource(R.string.discard),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ExpressiveIconButton(
                        onClick = {
                            viewModel.saveOrder(context)
                            onBack()
                        },
                        icon = Icons.Default.Check,
                        contentDescription = stringResource(R.string.save),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (currentViewType == ViewType.Grid) {
                    val gridShape = remember { RoundedCornerShape(24.dp) }
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(games, key = { it.packageName }) { game ->
                            ReorderableItem(reorderGridState, key = game.packageName) { isDragging ->
                                val elevation by animateDpAsState(if (isDragging) 12.dp else 0.dp, label = "elevation")
                                val scale by animateFloatAsState(if (isDragging) 1.05f else 1f, label = "scale")

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                            shadowElevation = elevation.toPx()
                                            shape = gridShape
                                            clip = false
                                        }
                                ) {
                                    EditGridGameCard(
                                        game = game,
                                        dragHandleModifier = Modifier.draggableHandle()
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val listShape = remember { RoundedCornerShape(28.dp) }
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items = games, key = { it.packageName }) { game ->
                            ReorderableItem(reorderState, key = game.packageName) { isDragging ->
                                val elevation by animateDpAsState(if (isDragging) 16.dp else 0.dp, label = "elevation")
                                val scale by animateFloatAsState(if (isDragging) 1.05f else 1f, label = "scale")

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                            shadowElevation = elevation.toPx()
                                            shape = listShape
                                            clip = false
                                        }
                                        .background(MaterialTheme.colorScheme.background)
                                ) {
                                    EditGameListItem(
                                        game = game,
                                        isDragging = isDragging,
                                        dragHandleModifier = Modifier.draggableHandle()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.save_order_title),
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.save_order_description),
                    fontFamily = GoogleSansFlex
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveOrder(context)
                        showSaveDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.save),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSaveDialog = false
                    onBack()
                }) {
                    Text(
                        text = stringResource(R.string.discard),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
private fun EditGridGameCard(
    game: GameApp,
    dragHandleModifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppIcon(
                    packageName = game.packageName,
                    customIconUri = game.customIconUri,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                Icons.Default.DragHandle,
                contentDescription = "Drag",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = dragHandleModifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
            )
        }
    }
}

@Composable
private fun EditGameListItem(
    game: GameApp,
    isDragging: Boolean = false,
    dragHandleModifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                packageName = game.packageName,
                customIconUri = game.customIconUri,
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(22.dp))
            )
            Spacer(modifier = Modifier.width(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    fontFamily = GoogleSansFlex
                )
            }

            Icon(
                Icons.Default.DragHandle,
                contentDescription = "Drag",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = dragHandleModifier
            )
        }
    }
}