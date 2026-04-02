package com.fedeveloper95.games

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.fedeveloper95.games.services.mainactivity.GameApp
import com.fedeveloper95.games.services.mainactivity.GameViewModel
import com.fedeveloper95.games.services.mainactivity.formatPlayTime
import com.fedeveloper95.games.elements.MainActivity.AddAppsBottomSheet
import com.fedeveloper95.games.elements.MainActivity.CommunityBottomSheet
import com.fedeveloper95.games.elements.MainActivity.DeletePopup
import com.fedeveloper95.games.elements.MainActivity.Edit.EditAppBottomSheet
import com.fedeveloper95.games.elements.ui.GameHubTheme
import com.fedeveloper95.games.elements.ui.ExpressiveIconButton
import com.fedeveloper95.games.elements.ui.AppIcon
import com.fedeveloper95.games.elements.ui.AnimatedPlayButton
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import com.fedeveloper95.games.services.SettingsActivity.Updater

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameHubTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GameHubScreen()
                }
            }
        }
    }
}

enum class ViewType { Pager, Grid, List }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalTextApi::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun GameHubScreen(viewModel: GameViewModel = viewModel()) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isExpandedScreen = configuration.screenWidthDp >= 600

    val games by viewModel.games.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var isInitialLoad by remember { mutableStateOf(true) }

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            isInitialLoad = false
        }
    }

    var showAddSheet by remember { mutableStateOf(false) }
    var showCommunitySheet by remember { mutableStateOf(false) }
    var gameToRemove by remember { mutableStateOf<GameApp?>(null) }
    var gameToEdit by remember { mutableStateOf<GameApp?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val prefs = remember { context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE) }
    val currentCardStyle = remember { mutableStateOf(prefs.getString("pref_card_style", "Default") ?: "Default") }
    val gridColumns = remember { mutableIntStateOf(prefs.getInt("pref_grid_columns", 2)) }
    val showGetMoreGames = remember { mutableStateOf(prefs.getBoolean("pref_show_get_more_games", true)) }
    val autoUpdates = remember { mutableStateOf(prefs.getBoolean("pref_auto_updates", true)) }

    val showUserName = remember { mutableStateOf(prefs.getBoolean("pref_show_user_name", true)) }
    val userName = remember { mutableStateOf(prefs.getString("pref_user_name", "User") ?: "User") }
    val sortType = remember { mutableStateOf(prefs.getString("pref_sort_type", "Alphabetical") ?: "Alphabetical") }
    val statsInterval = remember { mutableFloatStateOf(prefs.getFloat("pref_stats_interval", 3f)) }

    val showLaunchCount = remember { mutableStateOf(prefs.getBoolean("pref_show_launch_count", true)) }
    val showPlayTime = remember { mutableStateOf(prefs.getBoolean("pref_show_play_time", true)) }

    val scope = rememberCoroutineScope()

    val currentVersionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    val customWelcomeFontFamily = FontFamily(
        Font(
            resId = R.font.sans_flex,
            variationSettings = FontVariation.Settings(
                FontVariation.slant(-9f),
                FontVariation.width(111f),
                FontVariation.weight(333),
                FontVariation.Setting("GRAD", 100f),
                FontVariation.Setting("ROND", 100f)
            )
        )
    )

    LaunchedEffect(currentVersionName) {
        val lastVersion = prefs.getString("last_version", null)
        if (lastVersion != currentVersionName) {
            showCommunitySheet = true
        }
    }

    LaunchedEffect(Unit) {
        if (autoUpdates.value) {
            if (Build.VERSION.SDK_INT >= 33) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            val update = Updater.checkForUpdates(currentVersionName)
            if (update != null) {
                Updater.showUpdateNotification(context, update)
            }
        }
    }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "pref_card_style") {
                currentCardStyle.value = sharedPreferences.getString("pref_card_style", "Default") ?: "Default"
            }
            if (key == "pref_grid_columns") {
                gridColumns.intValue = sharedPreferences.getInt("pref_grid_columns", 2)
            }
            if (key == "pref_show_get_more_games") {
                showGetMoreGames.value = sharedPreferences.getBoolean("pref_show_get_more_games", true)
            }
            if (key == "pref_auto_updates") {
                autoUpdates.value = sharedPreferences.getBoolean("pref_auto_updates", true)
            }
            if (key == "pref_show_user_name") {
                showUserName.value = sharedPreferences.getBoolean("pref_show_user_name", true)
            }
            if (key == "pref_user_name") {
                userName.value = sharedPreferences.getString("pref_user_name", "User") ?: "User"
            }
            if (key == "pref_sort_type") {
                sortType.value = sharedPreferences.getString("pref_sort_type", "Alphabetical") ?: "Alphabetical"
                viewModel.loadGames(context)
            }
            if (key == "pref_stats_interval") {
                statsInterval.floatValue = sharedPreferences.getFloat("pref_stats_interval", 3f)
                viewModel.loadGames(context)
            }
            if (key == "pref_show_launch_count") {
                showLaunchCount.value = sharedPreferences.getBoolean("pref_show_launch_count", true)
            }
            if (key == "pref_show_play_time") {
                showPlayTime.value = sharedPreferences.getBoolean("pref_show_play_time", true)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadGames(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val displayGames = remember(games, searchQuery) {
        if (searchQuery.isEmpty()) games
        else games.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val favoriteGames = remember(displayGames) { displayGames.filter { it.isFavorite } }
    val normalGames = remember(displayGames) { displayGames.filter { !it.isFavorite } }

    val openPlayStore: (String) -> Unit = { packageName ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    val launchGame: (GameApp) -> Unit = { game ->
        game.launchIntent?.let {
            viewModel.incrementLaunchCount(context, game.packageName)
            context.startActivity(it)
        }
    }

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    BackHandler(enabled = searchQuery.isNotEmpty()) {
        if (searchQuery.isNotEmpty()) {
            searchQuery = ""
        }
    }

    val currentViewType = remember(currentCardStyle.value) {
        when (currentCardStyle.value) {
            "Horizontal" -> ViewType.Pager
            "Grid" -> ViewType.Grid
            else -> ViewType.List
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            androidx.compose.animation.AnimatedVisibility(
                visible = searchQuery.isEmpty(),
                enter = scaleIn(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
                exit = scaleOut(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
            ) {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_game))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .then(if (isExpandedScreen) Modifier.padding(horizontal = 64.dp) else Modifier)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp)
            ) {
                val buttonsContent = @Composable {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ExpressiveIconButton(
                            onClick = {
                                val intent = Intent(context, EditModeActivity::class.java)
                                context.startActivity(intent)
                            },
                            icon = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_mode_title),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ExpressiveIconButton(
                            onClick = {
                                val intent = Intent(context, SettingsActivity::class.java)
                                context.startActivity(intent)
                            },
                            icon = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (showUserName.value) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${userName.value}'s",
                            style = TextStyle(
                                fontFamily = customWelcomeFontFamily,
                                fontSize = 28.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        buttonsContent()
                    }
                    Text(
                        text = stringResource(R.string.app_name),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold,
                        fontSize = 42.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 48.sp,
                        modifier = Modifier.offset(y = (-4).dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.Bold,
                            fontSize = 42.sp,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 48.sp
                        )
                        buttonsContent()
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HomeSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )

                    val spacing = 12.dp
                    Spacer(modifier = Modifier.height(spacing))

                    Text(
                        text = "${displayGames.size} ${stringResource(R.string.games_count_suffix)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(start = 4.dp),
                        fontFamily = GoogleSansFlex
                    )

                    Spacer(modifier = Modifier.height(spacing))
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (isInitialLoad && isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator(modifier = Modifier.size(64.dp))
                    }
                } else {
                    var isRefreshing by remember { mutableStateOf(false) }
                    val pullRefreshState = rememberPullToRefreshState()
                    val onRefresh: () -> Unit = {
                        isRefreshing = true
                        scope.launch {
                            viewModel.loadGames(context)
                            delay(1000)
                            isRefreshing = false
                        }
                    }

                    PullToRefreshBox(
                        state = pullRefreshState,
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize(),
                        indicator = {
                            PullToRefreshDefaults.LoadingIndicator(
                                state = pullRefreshState,
                                isRefreshing = isRefreshing,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                    ) {
                        if (games.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyState()
                            }
                        } else {
                            AnimatedContent(
                                targetState = currentViewType,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.95f, animationSpec = tween(200)) togetherWith
                                            fadeOut(animationSpec = tween(200))
                                },
                                label = "mainContentAnim"
                            ) { viewType ->
                                when (viewType) {
                                    ViewType.Pager -> {
                                        HorizontalGamePager(
                                            games = displayGames,
                                            showLaunchCount = showLaunchCount.value,
                                            showPlayTime = showPlayTime.value,
                                            gameToRemove = gameToRemove,
                                            onLaunch = launchGame,
                                            onStoreClick = { openPlayStore(it.packageName) },
                                            onDelete = { gameToRemove = it },
                                            onEditApp = { gameToEdit = it }
                                        )
                                    }
                                    ViewType.Grid -> {
                                        LazyVerticalGrid(
                                            state = gridState,
                                            columns = if (isExpandedScreen) GridCells.Adaptive(160.dp) else GridCells.Fixed(gridColumns.intValue),
                                            contentPadding = PaddingValues(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 100.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            if (favoriteGames.isNotEmpty() && searchQuery.isEmpty()) {
                                                item(span = { GridItemSpan(maxLineSpan) }) {
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                                        shape = RoundedCornerShape(32.dp),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                        ),
                                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                                    ) {
                                                        Column(modifier = Modifier.padding(vertical = 16.dp)) {
                                                            Text(
                                                                text = stringResource(R.string.favorites),
                                                                style = MaterialTheme.typography.titleMedium,
                                                                color = MaterialTheme.colorScheme.primary,
                                                                fontFamily = GoogleSansFlex,
                                                                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                                                            )

                                                            val cols = if (isExpandedScreen) 2 else gridColumns.intValue
                                                            val chunkedFavorites = favoriteGames.chunked(cols)

                                                            Column(
                                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                                            ) {
                                                                chunkedFavorites.forEach { rowGames ->
                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                                                    ) {
                                                                        rowGames.forEach { game ->
                                                                            Box(modifier = Modifier.weight(1f)) {
                                                                                SwipeableGameContainer(
                                                                                    item = game,
                                                                                    isDeleteCandidate = gameToRemove?.packageName == game.packageName,
                                                                                    showLaunchCount = showLaunchCount.value,
                                                                                    showPlayTime = showPlayTime.value,
                                                                                    onDelete = { gameToRemove = game },
                                                                                    shape = RoundedCornerShape(24.dp),
                                                                                    fullSwipeStats = true
                                                                                ) {
                                                                                    GridGameCard(
                                                                                        game = game,
                                                                                        columns = cols,
                                                                                        onLaunch = { launchGame(game) },
                                                                                        onLongClick = { gameToEdit = game }
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                        repeat(cols - rowGames.size) {
                                                                            Spacer(modifier = Modifier.weight(1f))
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            items(if (searchQuery.isEmpty()) normalGames else displayGames, key = { it.packageName }) { game ->
                                                SwipeableGameContainer(
                                                    item = game,
                                                    isDeleteCandidate = gameToRemove?.packageName == game.packageName,
                                                    showLaunchCount = showLaunchCount.value,
                                                    showPlayTime = showPlayTime.value,
                                                    onDelete = { gameToRemove = game },
                                                    shape = RoundedCornerShape(24.dp),
                                                    fullSwipeStats = true
                                                ) {
                                                    GridGameCard(
                                                        game = game,
                                                        columns = if (isExpandedScreen) 2 else gridColumns.intValue,
                                                        onLaunch = { launchGame(game) },
                                                        onLongClick = { gameToEdit = game }
                                                    )
                                                }
                                            }
                                            if (searchQuery.isEmpty() && showGetMoreGames.value) {
                                                item(span = { GridItemSpan(maxLineSpan) }) {
                                                    GetMoreGamesCard(context)
                                                }
                                            }
                                        }
                                    }
                                    ViewType.List -> {
                                        if (isExpandedScreen) {
                                            LazyVerticalGrid(
                                                columns = GridCells.Adaptive(minSize = 340.dp),
                                                contentPadding = PaddingValues(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 100.dp),
                                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                if (favoriteGames.isNotEmpty() && searchQuery.isEmpty()) {
                                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                                        Card(
                                                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                                            shape = RoundedCornerShape(32.dp),
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                            ),
                                                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                                        ) {
                                                            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                                                                Text(
                                                                    text = stringResource(R.string.favorites),
                                                                    style = MaterialTheme.typography.titleMedium,
                                                                    color = MaterialTheme.colorScheme.primary,
                                                                    fontFamily = GoogleSansFlex,
                                                                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                                                                )
                                                                favoriteGames.forEachIndexed { index, game ->
                                                                    val shape = when {
                                                                        favoriteGames.size == 1 -> RoundedCornerShape(24.dp)
                                                                        index == 0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                                                                        index == favoriteGames.size - 1 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                                                                        else -> RoundedCornerShape(4.dp)
                                                                    }
                                                                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                                                                        SwipeableGameContainer(
                                                                            item = game,
                                                                            isDeleteCandidate = gameToRemove?.packageName == game.packageName,
                                                                            showLaunchCount = showLaunchCount.value,
                                                                            showPlayTime = showPlayTime.value,
                                                                            onDelete = { gameToRemove = game },
                                                                            shape = shape
                                                                        ) {
                                                                            GameListItem(
                                                                                game = game,
                                                                                isSingle = favoriteGames.size == 1,
                                                                                isFirst = index == 0,
                                                                                isLast = index == favoriteGames.size - 1,
                                                                                onLaunch = { launchGame(game) },
                                                                                onStoreClick = { openPlayStore(game.packageName) },
                                                                                onLongClick = { gameToEdit = game }
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                items(items = if (searchQuery.isEmpty()) normalGames else displayGames, key = { it.packageName }) { game ->
                                                    val shape = RoundedCornerShape(28.dp)
                                                    SwipeableGameContainer(
                                                        item = game,
                                                        isDeleteCandidate = gameToRemove?.packageName == game.packageName,
                                                        showLaunchCount = showLaunchCount.value,
                                                        showPlayTime = showPlayTime.value,
                                                        onDelete = { gameToRemove = game },
                                                        shape = shape
                                                    ) {
                                                        GameListItem(
                                                            game = game,
                                                            isSingle = true,
                                                            isFirst = true,
                                                            isLast = true,
                                                            onLaunch = { launchGame(game) },
                                                            onStoreClick = { openPlayStore(game.packageName) },
                                                            onLongClick = { gameToEdit = game }
                                                        )
                                                    }
                                                }
                                                if (searchQuery.isEmpty() && showGetMoreGames.value) {
                                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                                        Spacer(modifier = Modifier.height(12.dp))
                                                        GetMoreGamesCard(context)
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
                                                if (favoriteGames.isNotEmpty() && searchQuery.isEmpty()) {
                                                    item {
                                                        Card(
                                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                                            shape = RoundedCornerShape(32.dp),
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                            ),
                                                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                                        ) {
                                                            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                                                                Text(
                                                                    text = stringResource(R.string.favorites),
                                                                    style = MaterialTheme.typography.titleMedium,
                                                                    color = MaterialTheme.colorScheme.primary,
                                                                    fontFamily = GoogleSansFlex,
                                                                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
                                                                )
                                                                favoriteGames.forEachIndexed { index, game ->
                                                                    val shape = when {
                                                                        favoriteGames.size == 1 -> RoundedCornerShape(24.dp)
                                                                        index == 0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                                                                        index == favoriteGames.size - 1 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                                                                        else -> RoundedCornerShape(4.dp)
                                                                    }
                                                                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                                                                        SwipeableGameContainer(
                                                                            item = game,
                                                                            isDeleteCandidate = gameToRemove?.packageName == game.packageName,
                                                                            showLaunchCount = showLaunchCount.value,
                                                                            showPlayTime = showPlayTime.value,
                                                                            onDelete = { gameToRemove = game },
                                                                            shape = shape
                                                                        ) {
                                                                            GameListItem(
                                                                                game = game,
                                                                                isSingle = favoriteGames.size == 1,
                                                                                isFirst = index == 0,
                                                                                isLast = index == favoriteGames.size - 1,
                                                                                onLaunch = { launchGame(game) },
                                                                                onStoreClick = { openPlayStore(game.packageName) },
                                                                                onLongClick = { gameToEdit = game }
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                val currentList = if (searchQuery.isEmpty()) normalGames else displayGames

                                                itemsIndexed(items = currentList, key = { _, item -> item.packageName }) { index, game ->
                                                    val shape = when {
                                                        currentList.size == 1 -> RoundedCornerShape(28.dp)
                                                        index == 0 -> RoundedCornerShape(
                                                            topStart = 28.dp,
                                                            topEnd = 28.dp,
                                                            bottomStart = 4.dp,
                                                            bottomEnd = 4.dp
                                                        )
                                                        index == currentList.size - 1 -> RoundedCornerShape(
                                                            topStart = 4.dp,
                                                            topEnd = 4.dp,
                                                            bottomStart = 28.dp,
                                                            bottomEnd = 28.dp
                                                        )
                                                        else -> RoundedCornerShape(4.dp)
                                                    }

                                                    SwipeableGameContainer(
                                                        item = game,
                                                        isDeleteCandidate = gameToRemove?.packageName == game.packageName,
                                                        showLaunchCount = showLaunchCount.value,
                                                        showPlayTime = showPlayTime.value,
                                                        onDelete = { gameToRemove = game },
                                                        shape = shape
                                                    ) {
                                                        GameListItem(
                                                            game = game,
                                                            isSingle = currentList.size == 1,
                                                            isFirst = index == 0,
                                                            isLast = index == currentList.size - 1,
                                                            onLaunch = { launchGame(game) },
                                                            onStoreClick = { openPlayStore(game.packageName) },
                                                            onLongClick = { gameToEdit = game }
                                                        )
                                                    }
                                                }
                                                if (searchQuery.isEmpty() && showGetMoreGames.value) {
                                                    item { Spacer(modifier = Modifier.height(12.dp)); GetMoreGamesCard(context) }
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
    }

    if (gameToRemove != null) {
        DeletePopup(
            game = gameToRemove!!,
            onConfirm = {
                gameToRemove?.let { viewModel.hideGame(context, it.packageName) }
                gameToRemove = null
            },
            onUninstall = {
                gameToRemove?.let {
                    viewModel.hideGame(context, it.packageName)
                    try {
                        val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:${it.packageName}"))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                gameToRemove = null
            },
            onDismiss = { gameToRemove = null }
        )
    }

    if (gameToEdit != null) {
        EditAppBottomSheet(
            game = gameToEdit!!,
            onDismiss = { gameToEdit = null }
        )
    }

    if (showAddSheet) {
        AddAppsBottomSheet(
            allApps = viewModel.allApps.collectAsState().value,
            onDismiss = { showAddSheet = false },
            onAdd = { pkgs ->
                pkgs.forEach { pkg ->
                    viewModel.addManualGame(context, pkg)
                }
                showAddSheet = false
            }
        )
    }

    if (showCommunitySheet) {
        CommunityBottomSheet(
            onDismiss = {
                showCommunitySheet = false
                prefs.edit().putString("last_version", currentVersionName).apply()
            }
        )
    }
}

@Composable
fun HomeSearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.search_apps), fontFamily = GoogleSansFlex) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
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
}

@Composable
fun AnimatedShopButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 15 else 50,
        animationSpec = tween(durationMillis = 200),
        label = "btnMorph"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(cornerPercent),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        interactionSource = interactionSource
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.ShoppingBag,
                contentDescription = "Store"
            )
        }
    }
}

private fun Color.darken(factor: Float = 0.8f): Color {
    return Color(
        red = this.red * factor,
        green = this.green * factor,
        blue = this.blue * factor,
        alpha = this.alpha
    )
}

@Composable
fun GameIconDisplay(game: GameApp, modifier: Modifier = Modifier) {
    val customIconUri = game.customIconUri
    val isBuiltIn = customIconUri?.startsWith("builtin://") == true

    if (isBuiltIn) {
        val uriStr = customIconUri!!
        val name = uriStr.substringAfter("builtin://").substringBefore("#")
        val colorHex = uriStr.substringAfter("#").toIntOrNull() ?: 0
        val color = if (colorHex != 0) Color(colorHex) else MaterialTheme.colorScheme.primary

        val iconVector = when(name) {
            "star" -> Icons.Rounded.Star
            "controller" -> Icons.Rounded.SportsEsports
            "bolt" -> Icons.Rounded.Bolt
            "heart" -> Icons.Rounded.Favorite
            "gamepad" -> Icons.Rounded.Gamepad
            "videogame" -> Icons.Rounded.VideogameAsset
            "toy" -> Icons.Rounded.SmartToy
            "puzzle" -> Icons.Rounded.Extension
            "dice" -> Icons.Rounded.Casino
            "rocket" -> Icons.Rounded.RocketLaunch
            "trophy" -> Icons.Rounded.EmojiEvents
            "explore" -> Icons.Rounded.Explore
            else -> Icons.Rounded.Gamepad
        }

        Box(
            modifier = modifier.background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = color.darken(0.5f),
                modifier = Modifier.fillMaxSize(0.56f)
            )
        }
    } else {
        AppIcon(
            packageName = game.packageName,
            customIconUri = customIconUri,
            modifier = modifier
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HorizontalGamePager(
    games: List<GameApp>,
    showLaunchCount: Boolean,
    showPlayTime: Boolean,
    gameToRemove: GameApp?,
    onLaunch: (GameApp) -> Unit,
    onStoreClick: (GameApp) -> Unit,
    onDelete: (GameApp) -> Unit,
    onEditApp: (GameApp) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { games.size })

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 24.dp),
        pageSpacing = 16.dp,
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.Top
    ) { page ->
        val game = games[page]

        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
        val scale = lerp(1f, 0.9f, pageOffset.absoluteValue.coerceIn(0f, 1f))
        val alpha = lerp(1f, 0.6f, pageOffset.absoluteValue.coerceIn(0f, 1f))

        SwipeableGameContainer(
            item = game,
            isDeleteCandidate = gameToRemove?.packageName == game.packageName,
            showLaunchCount = showLaunchCount,
            showPlayTime = showPlayTime,
            onDelete = { onDelete(game) },
            orientation = Orientation.Vertical,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
        ) {
            HorizontalGameCard(
                game = game,
                onLaunch = { onLaunch(game) },
                onStoreClick = { onStoreClick(game) },
                onLongClick = { onEditApp(game) }
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HorizontalGameCard(
    game: GameApp,
    onLaunch: () -> Unit,
    onStoreClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.72f)
            .clip(RoundedCornerShape(32.dp))
            .combinedClickable(
                onClick = onLaunch,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GameIconDisplay(
                game = game,
                modifier = Modifier
                    .size(140.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = game.name,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSansFlex
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedShopButton(onClick = onStoreClick)

            Spacer(modifier = Modifier.weight(1f))

            AnimatedPlayButton(onClick = onLaunch)
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GridGameCard(
    game: GameApp,
    columns: Int,
    onLaunch: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .combinedClickable(
                onClick = onLaunch,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (columns <= 2) 16.dp else 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val iconFraction = if (columns <= 2) 0.55f else 0.8f

                GameIconDisplay(
                    game = game,
                    modifier = Modifier
                        .fillMaxWidth(iconFraction)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                )

                if (columns <= 2) {
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
            }
        }
    }
}

@Composable
fun SwipeableGameContainer(
    item: GameApp,
    isDeleteCandidate: Boolean,
    showLaunchCount: Boolean,
    showPlayTime: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
    shape: Shape = RoundedCornerShape(24.dp),
    fullSwipeStats: Boolean = false,
    content: @Composable () -> Unit
) {
    var limit by remember { mutableFloatStateOf(0f) }

    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val density = LocalDensity.current

    val isDark = isSystemInDarkTheme()
    val deleteBackgroundColor = if (isDark) Color(0xFFF2B8B5) else Color(0xFFB3261E)
    val deleteIconColor = if (isDark) Color(0xFF601410) else Color(0xFFFFFFFF)

    val statsBackgroundColor = MaterialTheme.colorScheme.primary
    val statsContentColor = MaterialTheme.colorScheme.onPrimary

    val canSwipeStats = showLaunchCount || showPlayTime
    val isVertical = orientation == Orientation.Vertical

    val bgShape = shape

    LaunchedEffect(isDeleteCandidate) {
        if (!isDeleteCandidate && offset.value < 0f && isVertical) {
            offset.animateTo(0f, tween(200))
        } else if (!isDeleteCandidate && offset.value > 0f && !isVertical) {
            offset.animateTo(0f, tween(200))
        }
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { limit = if (isVertical) it.height.toFloat() else it.width.toFloat() }
            .draggable(
                orientation = orientation,
                state = rememberDraggableState { delta ->
                    scope.launch {
                        val current = offset.value
                        var target = current + delta

                        val min = if (isVertical) {
                            -limit
                        } else {
                            if (canSwipeStats) -limit else 0f
                        }

                        val max = if (isVertical) {
                            if (canSwipeStats) limit else 0f
                        } else {
                            limit
                        }

                        target = target.coerceIn(min, max)
                        offset.snapTo(target)
                    }
                },
                onDragStopped = {
                    val currentVal = offset.value

                    if (isVertical) {
                        if (currentVal < 0) {
                            if (currentVal < -limit * 0.5f) {
                                scope.launch {
                                    offset.animateTo(-limit, tween(200))
                                    onDelete()
                                }
                            } else {
                                scope.launch { offset.animateTo(0f, tween(200)) }
                            }
                        } else if (currentVal > 0) {
                            val statsFraction = if (fullSwipeStats) 1f else 0.5f
                            val target = limit * statsFraction
                            val threshold = limit * 0.2f
                            if (currentVal > threshold) {
                                scope.launch { offset.animateTo(target, tween(200)) }
                            } else {
                                scope.launch { offset.animateTo(0f, tween(200)) }
                            }
                        }
                    } else {
                        if (currentVal > 0) {
                            if (currentVal > limit * 0.5f) {
                                scope.launch {
                                    offset.animateTo(limit, tween(200))
                                    onDelete()
                                }
                            } else {
                                scope.launch { offset.animateTo(0f, tween(200)) }
                            }
                        } else if (currentVal < 0) {
                            val statsFraction = if (fullSwipeStats) 1f else 0.5f
                            val target = -(limit * statsFraction)
                            val threshold = -(limit * 0.2f)
                            if (currentVal < threshold) {
                                scope.launch { offset.animateTo(target, tween(200)) }
                            } else {
                                scope.launch { offset.animateTo(0f, tween(200)) }
                            }
                        }
                    }
                }
            )
    ) {
        Layout(
            content = {
                Box(
                    modifier = Modifier
                        .clip(bgShape)
                        .background(deleteBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = deleteIconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(bgShape)
                        .background(statsBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    val statsFraction = if (fullSwipeStats) 1f else 0.5f
                    val reqModifier = if (isVertical) Modifier.requiredHeight(with(density) { (limit * statsFraction).toDp() })
                    else Modifier.requiredWidth(with(density) { (limit * statsFraction).toDp() })

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(8.dp).then(reqModifier)
                    ) {
                        if (isVertical) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (showLaunchCount) {
                                    Icon(Icons.Default.PlayArrow, null, tint = statsContentColor, modifier = Modifier.size(16.dp))
                                    Text(" ${item.launchCount}", style = MaterialTheme.typography.labelMedium, color = statsContentColor, maxLines = 1, softWrap = false, overflow = TextOverflow.Clip, fontFamily = GoogleSansFlex)
                                }
                                if (showLaunchCount && showPlayTime) Spacer(Modifier.width(12.dp))
                                if (showPlayTime) {
                                    Icon(Icons.Outlined.Timer, null, tint = statsContentColor, modifier = Modifier.size(16.dp))
                                    Text(" ${formatPlayTime(item.totalPlayTime)}", style = MaterialTheme.typography.labelMedium, color = statsContentColor, maxLines = 1, softWrap = false, overflow = TextOverflow.Clip, fontFamily = GoogleSansFlex)
                                }
                            }
                        } else {
                            if (showLaunchCount) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayArrow, null, tint = statsContentColor, modifier = Modifier.size(16.dp))
                                    Text(" ${item.launchCount}", style = MaterialTheme.typography.labelMedium, color = statsContentColor, maxLines = 1, softWrap = false, overflow = TextOverflow.Clip, fontFamily = GoogleSansFlex)
                                }
                            }
                            if (showLaunchCount && showPlayTime) Spacer(Modifier.height(4.dp))
                            if (showPlayTime) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Outlined.Timer, null, tint = statsContentColor, modifier = Modifier.size(16.dp))
                                    Text(formatPlayTime(item.totalPlayTime), style = MaterialTheme.typography.labelMedium, color = statsContentColor, textAlign = TextAlign.Center, maxLines = 1, softWrap = false, overflow = TextOverflow.Clip, fontFamily = GoogleSansFlex)
                                }
                            }
                        }
                    }
                }

                Box { content() }
            }
        ) { measurables, constraints ->
            val contentPlaceable = measurables[2].measure(constraints)
            val height = contentPlaceable.height
            val width = contentPlaceable.width

            val offsetVal = offset.value

            val action1Size = if (offsetVal > 0 && !isVertical) offsetVal.roundToInt()
            else if (offsetVal < 0 && isVertical) offsetVal.absoluteValue.roundToInt()
            else 0

            val action2Size = if (offsetVal < 0 && !isVertical) offsetVal.absoluteValue.roundToInt()
            else if (offsetVal > 0 && isVertical) offsetVal.roundToInt()
            else 0

            val deletePlaceable = measurables[0].measure(Constraints.fixed(if (isVertical) width else action1Size, if (isVertical) action1Size else height))
            val statsPlaceable = measurables[1].measure(Constraints.fixed(if (isVertical) width else action2Size, if (isVertical) action2Size else height))

            layout(width, height) {
                if (action1Size > 0) {
                    if (isVertical) {
                        deletePlaceable.place(0, height - action1Size)
                    } else {
                        deletePlaceable.place(0, 0)
                    }
                }

                if (action2Size > 0) {
                    if (isVertical) {
                        statsPlaceable.place(0, 0)
                    } else {
                        statsPlaceable.place(width - action2Size, 0)
                    }
                }

                if (isVertical) {
                    contentPlaceable.place(0, offsetVal.roundToInt())
                } else {
                    contentPlaceable.place(offsetVal.roundToInt(), 0)
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GameListItem(
    game: GameApp,
    isSingle: Boolean = false,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onLaunch: () -> Unit,
    onStoreClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topRound = if (isSingle || isFirst) 28.dp else 4.dp
    val bottomRound = if (isSingle || isLast) 28.dp else 4.dp

    val topStart by animateDpAsState(targetValue = if (isPressed) 28.dp else topRound, animationSpec = tween(200), label = "")
    val topEnd by animateDpAsState(targetValue = if (isPressed) 28.dp else topRound, animationSpec = tween(200), label = "")
    val bottomStart by animateDpAsState(targetValue = if (isPressed) 28.dp else bottomRound, animationSpec = tween(200), label = "")
    val bottomEnd by animateDpAsState(targetValue = if (isPressed) 28.dp else bottomRound, animationSpec = tween(200), label = "")

    val shape = RoundedCornerShape(topStart, topEnd, bottomStart, bottomEnd)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .clip(shape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onLaunch,
                onLongClick = onLongClick
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
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
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = GoogleSansFlex
                )
            }

            AnimatedShopButton(onClick = onStoreClick)

            Spacer(modifier = Modifier.width(4.dp))
            AnimatedPlayButton(onClick = onLaunch)
        }
    }
}

@Composable
fun GetMoreGamesCard(context: Context) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 15 else 50,
        animationSpec = tween(200),
        label = "btnMorph"
    )

    Surface(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse("market://search?q=games&c=apps") }
            try { context.startActivity(intent) } catch (e: Exception) {}
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(cornerPercent),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp,
        shadowElevation = 6.dp,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Apps,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.download_games),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = GoogleSansFlex
            )
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_launcher_monochrome),
            contentDescription = null,
            modifier = Modifier.size(180.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.no_games_title), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, fontFamily = GoogleSansFlex)
        Text(stringResource(R.string.no_games_subtitle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = GoogleSansFlex)
    }
}