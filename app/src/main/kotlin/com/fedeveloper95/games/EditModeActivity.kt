package com.fedeveloper95.games

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fedeveloper95.games.elements.ui.ExpressiveIconButton
import com.fedeveloper95.games.elements.ui.GameHubTheme
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import com.fedeveloper95.games.services.mainactivity.GameApp
import com.fedeveloper95.games.services.mainactivity.GameViewModel
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
    val configuration = LocalConfiguration.current
    val isExpandedScreen = configuration.screenWidthDp >= 600

    val games by viewModel.games.collectAsState()
    val prefs = remember { context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE) }

    val sortType = remember { mutableStateOf(prefs.getString("pref_sort_type", "Alphabetical") ?: "Alphabetical") }
    val currentCardStyle = remember { mutableStateOf(prefs.getString("pref_card_style", "Default") ?: "Default") }

    val currentViewType = remember(currentCardStyle.value) {
        when (currentCardStyle.value) {
            "Grid" -> ViewType.Grid
            else -> ViewType.List
        }
    }

    val onMove = { fromIndex: Int, toIndex: Int ->
        val currentList = games.toMutableList()
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

    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(listState) { from, to -> onMove(from.index, to.index) }

    val gridState = rememberLazyGridState()
    val reorderGridState = rememberReorderableLazyGridState(gridState) { from, to -> onMove(from.index, to.index) }

    val expandedListGridState = rememberLazyGridState()
    val reorderExpandedListGridState = rememberReorderableLazyGridState(expandedListGridState) { from, to -> onMove(from.index, to.index) }

    LaunchedEffect(Unit) {
        viewModel.loadGames(context)
    }

    BackHandler {
        onBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .then(if (isExpandedScreen) Modifier.padding(horizontal = 64.dp) else Modifier)
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
                        onClick = { onBack() },
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

            if (games.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_no_cards),
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    if (currentViewType == ViewType.Grid) {
                        val gridShape = remember { RoundedCornerShape(24.dp) }
                        LazyVerticalGrid(
                            state = gridState,
                            columns = if (isExpandedScreen) GridCells.Adaptive(160.dp) else GridCells.Fixed(2),
                            contentPadding = PaddingValues(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(games, key = { it.packageName }) { game ->
                                ReorderableItem(reorderGridState, key = game.packageName) { isDragging ->
                                    val elevation by animateDpAsState(
                                        targetValue = if (isDragging) 12.dp else 0.dp,
                                        animationSpec = tween(200),
                                        label = "elevation"
                                    )
                                    val scale by animateFloatAsState(
                                        targetValue = if (isDragging) 1.05f else 1f,
                                        animationSpec = tween(200),
                                        label = "scale"
                                    )

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
                        if (isExpandedScreen) {
                            LazyVerticalGrid(
                                state = expandedListGridState,
                                columns = GridCells.Adaptive(minSize = 340.dp),
                                contentPadding = PaddingValues(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 100.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(games, key = { it.packageName }) { game ->
                                    ReorderableItem(reorderExpandedListGridState, key = game.packageName) { isDragging ->
                                        val elevation by animateDpAsState(
                                            targetValue = if (isDragging) 16.dp else 0.dp,
                                            animationSpec = tween(200),
                                            label = "elevation"
                                        )
                                        val scale by animateFloatAsState(
                                            targetValue = if (isDragging) 1.05f else 1f,
                                            animationSpec = tween(200),
                                            label = "scale"
                                        )
                                        val shape = RoundedCornerShape(28.dp)

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .graphicsLayer {
                                                    scaleX = scale
                                                    scaleY = scale
                                                    shadowElevation = elevation.toPx()
                                                    this.shape = shape
                                                    clip = false
                                                }
                                                .background(MaterialTheme.colorScheme.background)
                                        ) {
                                            EditGameListItem(
                                                game = game,
                                                shape = shape,
                                                dragHandleModifier = Modifier.draggableHandle()
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 100.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(items = games, key = { _, item -> item.packageName }) { index, game ->
                                    ReorderableItem(reorderState, key = game.packageName) { isDragging ->
                                        val topRound = if (games.size == 1 || index == 0) 28.dp else 4.dp
                                        val bottomRound = if (games.size == 1 || index == games.size - 1) 28.dp else 4.dp

                                        val topStart by animateDpAsState(targetValue = if (isDragging) 28.dp else topRound, animationSpec = tween(200), label = "")
                                        val topEnd by animateDpAsState(targetValue = if (isDragging) 28.dp else topRound, animationSpec = tween(200), label = "")
                                        val bottomStart by animateDpAsState(targetValue = if (isDragging) 28.dp else bottomRound, animationSpec = tween(200), label = "")
                                        val bottomEnd by animateDpAsState(targetValue = if (isDragging) 28.dp else bottomRound, animationSpec = tween(200), label = "")

                                        val shape = RoundedCornerShape(topStart, topEnd, bottomStart, bottomEnd)

                                        val elevation by animateDpAsState(
                                            targetValue = if (isDragging) 16.dp else 0.dp,
                                            animationSpec = tween(200),
                                            label = "elevation"
                                        )
                                        val scale by animateFloatAsState(
                                            targetValue = if (isDragging) 1.05f else 1f,
                                            animationSpec = tween(200),
                                            label = "scale"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .graphicsLayer {
                                                    scaleX = scale
                                                    scaleY = scale
                                                    shadowElevation = elevation.toPx()
                                                    this.shape = shape
                                                    clip = false
                                                }
                                                .background(MaterialTheme.colorScheme.background)
                                        ) {
                                            EditGameListItem(
                                                game = game,
                                                shape = shape,
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
        }
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
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                GameIconDisplay(
                    game = game,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
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
                contentDescription = null,
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
    shape: Shape,
    dragHandleModifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .clip(shape),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameIconDisplay(
                game = game,
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
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
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = dragHandleModifier
            )
        }
    }
}