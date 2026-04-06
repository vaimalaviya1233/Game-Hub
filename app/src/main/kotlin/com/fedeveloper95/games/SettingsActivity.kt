package com.fedeveloper95.games

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.fedeveloper95.games.elements.SettingsActivity.CardStylePopup
import com.fedeveloper95.games.elements.SettingsActivity.GameOrderPopup
import com.fedeveloper95.games.elements.SettingsActivity.IconPopup
import com.fedeveloper95.games.elements.SettingsActivity.NamePopup
import com.fedeveloper95.games.elements.SettingsActivity.ThemePopup
import com.fedeveloper95.games.elements.UI.ExpressiveIconButton
import com.fedeveloper95.games.elements.ui.GameHubTheme
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import kotlin.math.roundToInt

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen(onBack = { finish() })
                }
            }
        }
    }
}

const val PREF_THEME = "pref_theme"
const val THEME_SYSTEM = 0
const val THEME_LIGHT = 1
const val THEME_DARK = 2

const val PREF_APP_ICON = "pref_app_icon"

const val PREF_CARD_STYLE = "pref_card_style"
const val CARD_STYLE_DEFAULT = "Default"
const val CARD_STYLE_HORIZONTAL = "Horizontal"
const val CARD_STYLE_GRID = "Grid"

const val PREF_GRID_COLUMNS = "pref_grid_columns"

const val PREF_SHOW_GET_MORE_GAMES = "pref_show_get_more_games"
const val PREF_SHOW_LAUNCH_COUNT = "pref_show_launch_count"
const val PREF_AUTO_UPDATES = "pref_auto_updates"

const val PREF_SHOW_USER_NAME = "pref_show_user_name"
const val PREF_USER_NAME = "pref_user_name"

const val PREF_SORT_TYPE = "pref_sort_type"
const val PREF_SHOW_PLAY_TIME = "pref_show_play_time"
const val PREF_STATS_INTERVAL = "pref_stats_interval"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isExpandedScreen = configuration.screenWidthDp >= 600
    val prefs = remember { context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE) }

    var currentTheme by remember { mutableIntStateOf(prefs.getInt(PREF_THEME, THEME_SYSTEM)) }
    var currentAppIcon by remember { mutableStateOf(prefs.getString(PREF_APP_ICON, "Expressive") ?: "Expressive") }
    var currentCardStyle by remember { mutableStateOf(prefs.getString(PREF_CARD_STYLE, CARD_STYLE_DEFAULT) ?: CARD_STYLE_DEFAULT) }
    var gridColumns by remember { mutableIntStateOf(prefs.getInt(PREF_GRID_COLUMNS, 2)) }
    var currentSortType by remember { mutableStateOf(prefs.getString(PREF_SORT_TYPE, "Alphabetical") ?: "Alphabetical") }

    var showGetMoreGames by remember { mutableStateOf(prefs.getBoolean(PREF_SHOW_GET_MORE_GAMES, true)) }
    var showLaunchCount by remember { mutableStateOf(prefs.getBoolean(PREF_SHOW_LAUNCH_COUNT, true)) }
    var showPlayTime by remember { mutableStateOf(prefs.getBoolean(PREF_SHOW_PLAY_TIME, true)) }
    var statsInterval by remember { mutableFloatStateOf(prefs.getFloat(PREF_STATS_INTERVAL, 3f)) }

    var showUserName by remember { mutableStateOf(prefs.getBoolean(PREF_SHOW_USER_NAME, true)) }
    var userName by remember { mutableStateOf(prefs.getString(PREF_USER_NAME, "User") ?: "User") }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showIconDialog by remember { mutableStateOf(false) }
    var showStyleDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }

    val isPixel = remember {
        val brand = Build.BRAND
        val manufacturer = Build.MANUFACTURER
        brand.equals("google", ignoreCase = true) || manufacturer.equals("google", ignoreCase = true)
    }

    val appInfo = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val version = pInfo.versionName ?: "1.0"
            val build = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pInfo.longVersionCode else pInfo.versionCode.toLong()
            "v$version ($build)"
        } catch (e: Exception) {
            context.getString(R.string.unknown)
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Normal
                    )
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                        ExpressiveIconButton(
                            onClick = onBack,
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.discard),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .then(if (isExpandedScreen) Modifier.padding(horizontal = 64.dp) else Modifier)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = padding.calculateBottomPadding() + 48.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_header_appearance),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            SettingsItemCard(
                icon = Icons.Rounded.Palette,
                title = stringResource(R.string.settings_theme_title),
                subtitle = when(currentTheme) {
                    THEME_LIGHT -> stringResource(R.string.settings_theme_light)
                    THEME_DARK -> stringResource(R.string.settings_theme_dark)
                    else -> stringResource(R.string.settings_theme_system)
                },
                containerColor = Color(0xFFfcbd00),
                iconColor = Color(0xFF6d3a01),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                onClick = { showThemeDialog = true }
            )

            Spacer(modifier = Modifier.height(2.dp))

            SettingsItemCard(
                icon = Icons.Rounded.Apps,
                title = stringResource(R.string.settings_app_icon_title),
                subtitle = if (currentAppIcon == "Expressive") stringResource(R.string.settings_app_icon_expressive) else stringResource(R.string.settings_app_icon_flat),
                containerColor = Color(0xFFD8B9FC),
                iconColor = Color(0xFF5629A4),
                shape = RoundedCornerShape(4.dp),
                onClick = { showIconDialog = true }
            )

            Spacer(modifier = Modifier.height(2.dp))

            SettingsItemCard(
                icon = Icons.Rounded.ViewAgenda,
                title = stringResource(R.string.settings_card_style_title),
                subtitle = currentCardStyle,
                containerColor = Color(0xFF80da88),
                iconColor = Color(0xFF00522c),
                shape = RoundedCornerShape(4.dp),
                onClick = { showStyleDialog = true }
            )

            AnimatedVisibility(
                visible = currentCardStyle == CARD_STYLE_GRID,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(2.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.settings_grid_columns_title),
                                fontFamily = GoogleSansFlex,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(2, 3, 4).forEachIndexed { index, columns ->
                                    SegmentedButton(
                                        selected = gridColumns == columns,
                                        onClick = {
                                            gridColumns = columns
                                            prefs.edit().putInt(PREF_GRID_COLUMNS, columns).apply()
                                        },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                                    ) {
                                        Text(
                                            text = columns.toString(),
                                            fontFamily = GoogleSansFlex
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            val nameSwitchShape = if (showUserName) {
                RoundedCornerShape(4.dp)
            } else {
                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
            }

            SettingsSwitchCard(
                icon = Icons.Rounded.Person,
                title = stringResource(R.string.settings_show_name_title),
                subtitle = stringResource(R.string.settings_show_name_desc),
                containerColor = Color(0xFFffb683),
                iconColor = Color(0xFF753403),
                shape = nameSwitchShape,
                checked = showUserName,
                onCheckedChange = {
                    showUserName = it
                    prefs.edit().putBoolean(PREF_SHOW_USER_NAME, it).apply()
                }
            )

            AnimatedVisibility(
                visible = showUserName,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(2.dp))
                    SettingsItemCard(
                        icon = Icons.Rounded.Edit,
                        title = stringResource(R.string.settings_edit_name_title),
                        subtitle = userName,
                        containerColor = Color(0xFFe7e0ec),
                        iconColor = Color(0xFF49454f),
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                        onClick = { showNameDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.settings_header_preferences),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            SettingsItemCard(
                icon = Icons.Rounded.Sort,
                title = stringResource(R.string.settings_sort_title),
                subtitle = when(currentSortType) {
                    "Alphabetical" -> stringResource(R.string.sort_alphabetical)
                    "Time" -> stringResource(R.string.sort_playtime)
                    else -> stringResource(R.string.sort_custom)
                },
                containerColor = Color(0xFF67d4ff),
                iconColor = Color(0xFF004e5d),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                onClick = { showSortDialog = true }
            )

            Spacer(modifier = Modifier.height(2.dp))

            SettingsSwitchCard(
                icon = Icons.Rounded.History,
                title = stringResource(R.string.settings_show_launch_count_title),
                subtitle = stringResource(R.string.settings_show_launch_count_desc),
                containerColor = Color(0xFFd8b9fc),
                iconColor = Color(0xFF5629a4),
                shape = RoundedCornerShape(4.dp),
                checked = showLaunchCount,
                onCheckedChange = {
                    showLaunchCount = it
                    prefs.edit().putBoolean(PREF_SHOW_LAUNCH_COUNT, it).apply()
                }
            )

            Spacer(modifier = Modifier.height(2.dp))

            val playTimeShape = if (showPlayTime) {
                RoundedCornerShape(4.dp)
            } else {
                RoundedCornerShape(4.dp)
            }

            SettingsSwitchCard(
                icon = Icons.Rounded.Timer,
                title = stringResource(R.string.settings_show_playtime_title),
                subtitle = stringResource(R.string.settings_show_playtime_desc),
                containerColor = Color(0xFFffaee4),
                iconColor = Color(0xFF8d0053),
                shape = playTimeShape,
                checked = showPlayTime,
                onCheckedChange = {
                    showPlayTime = it
                    prefs.edit().putBoolean(PREF_SHOW_PLAY_TIME, it).apply()
                }
            )

            AnimatedVisibility(
                visible = showPlayTime,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(2.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFffb3ae)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DateRange,
                                        contentDescription = null,
                                        tint = Color(0xFF8a1a16),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.settings_stats_interval_title),
                                        fontFamily = GoogleSansFlex,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = when(statsInterval.roundToInt()) {
                                            0 -> stringResource(R.string.interval_daily)
                                            1 -> stringResource(R.string.interval_weekly)
                                            2 -> stringResource(R.string.interval_monthly)
                                            else -> stringResource(R.string.interval_yearly)
                                        },
                                        fontFamily = GoogleSansFlex,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Slider(
                                value = statsInterval,
                                onValueChange = {
                                    statsInterval = it
                                    prefs.edit().putFloat(PREF_STATS_INTERVAL, it).apply()
                                },
                                valueRange = 0f..3f,
                                steps = 2
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            val getMoreGamesShape = if (isPixel) RoundedCornerShape(4.dp) else RoundedCornerShape(4.dp)

            SettingsSwitchCard(
                icon = Icons.Rounded.ShoppingBag,
                title = stringResource(R.string.settings_show_get_more_title),
                subtitle = stringResource(R.string.settings_show_get_more_desc),
                containerColor = Color(0xFF67d4ff),
                iconColor = Color(0xFF004e5d),
                shape = getMoreGamesShape,
                checked = showGetMoreGames,
                onCheckedChange = {
                    showGetMoreGames = it
                    prefs.edit().putBoolean(PREF_SHOW_GET_MORE_GAMES, it).apply()
                }
            )

            if (isPixel) {
                Spacer(modifier = Modifier.height(2.dp))

                SettingsItemCard(
                    icon = Icons.Rounded.SportsEsports,
                    title = stringResource(R.string.settings_google_play_title),
                    subtitle = stringResource(R.string.settings_google_play_desc),
                    containerColor = Color(0xFF80da88),
                    iconColor = Color(0xFF00522c),
                    shape = RoundedCornerShape(4.dp),
                    onClick = {
                        try {
                            val intent = Intent()
                            intent.component = ComponentName("com.google.android.gms", "com.google.android.gms.gp.gameservice.SettingsActivity")
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Google Play Games not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            SettingsItemCard(
                icon = Icons.Rounded.Tune,
                title = stringResource(R.string.settings_advanced_title),
                subtitle = stringResource(R.string.settings_advanced_desc),
                containerColor = Color(0xFFC5C0FF),
                iconColor = Color(0xFF2D237A),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                onClick = {
                    val intent = Intent(context, AdvancedSettingsActivity::class.java)
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.settings_header_info),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            SettingsItemCard(
                icon = Icons.Rounded.Info,
                title = stringResource(R.string.settings_version_title),
                subtitle = appInfo,
                containerColor = Color(0xFFa1c9ff),
                iconColor = Color(0xFF0641a0),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )

            Spacer(modifier = Modifier.height(2.dp))

            SettingsItemCard(
                icon = Icons.Rounded.Code,
                title = stringResource(R.string.settings_developer_title),
                subtitle = stringResource(R.string.settings_developer_name),
                containerColor = Color(0xFFc7c7c7),
                iconColor = Color(0xFF474747),
                shape = RoundedCornerShape(4.dp),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/FeDeveloper95"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(2.dp))

            SettingsItemCard(
                icon = Icons.Rounded.BugReport,
                title = stringResource(R.string.settings_report_title),
                subtitle = stringResource(R.string.settings_report_desc),
                containerColor = Color(0xFFffb3ae),
                iconColor = Color(0xFF8a1a16),
                shape = RoundedCornerShape(4.dp),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/fedeveloper95"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(2.dp))

            SettingsItemCard(
                icon = R.drawable.ic_phone_update,
                title = stringResource(R.string.settings_check_updates_title),
                subtitle = stringResource(R.string.settings_check_updates_desc),
                containerColor = Color(0xFF67d4ff),
                iconColor = Color(0xFF004e5d),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                onClick = {
                    val intent = Intent(context, UpdaterActivity::class.java)
                    context.startActivity(intent)
                }
            )
        }
    }

    if (showNameDialog) {
        NamePopup(
            currentName = userName,
            onNameSaved = { newName ->
                userName = newName
                showNameDialog = false
            },
            onDismiss = { showNameDialog = false }
        )
    }

    if (showThemeDialog) {
        ThemePopup(
            currentTheme = currentTheme,
            onThemeSelected = { index ->
                currentTheme = index
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showStyleDialog) {
        CardStylePopup(
            currentStyle = currentCardStyle,
            onStyleSelected = { newStyle ->
                currentCardStyle = newStyle
                showStyleDialog = false
            },
            onDismiss = { showStyleDialog = false }
        )
    }

    if (showSortDialog) {
        GameOrderPopup(
            currentSort = currentSortType,
            onSortSelected = { newSort ->
                currentSortType = newSort
                showSortDialog = false
            },
            onDismiss = { showSortDialog = false }
        )
    }

    if (showIconDialog) {
        IconPopup(
            currentIcon = currentAppIcon,
            onIconSelected = { newIcon ->
                currentAppIcon = newIcon
                prefs.edit().putString(PREF_APP_ICON, newIcon).apply()

                val pm = context.packageManager
                val expressiveComponent = ComponentName(context, "com.fedeveloper95.games.ExpressiveIcon")
                val flatComponent = ComponentName(context, "com.fedeveloper95.games.FlatIcon")

                if (newIcon == "Expressive") {
                    pm.setComponentEnabledSetting(expressiveComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
                    pm.setComponentEnabledSetting(flatComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
                } else {
                    pm.setComponentEnabledSetting(flatComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
                    pm.setComponentEnabledSetting(expressiveComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
                }

                showIconDialog = false
            },
            onDismiss = { showIconDialog = false }
        )
    }
}

@Composable
fun SettingsItemCard(
    icon: Any,
    title: String,
    subtitle: String,
    containerColor: Color,
    iconColor: Color,
    shape: Shape,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "anim_shape"
    )

    val animatedShape = remember(shape, pressProgress) {
        if (shape is RoundedCornerShape) {
            object : Shape {
                override fun createOutline(
                    size: Size,
                    layoutDirection: LayoutDirection,
                    density: Density
                ): Outline {
                    val targetPx = with(density) { 20.dp.toPx() }
                    fun lerp(start: Float, stop: Float, fraction: Float) =
                        (1 - fraction) * start + fraction * stop

                    val ts = lerp(shape.topStart.toPx(size, density), targetPx, pressProgress)
                    val te = lerp(shape.topEnd.toPx(size, density), targetPx, pressProgress)
                    val bs = lerp(shape.bottomStart.toPx(size, density), targetPx, pressProgress)
                    val be = lerp(shape.bottomEnd.toPx(size, density), targetPx, pressProgress)

                    return Outline.Rounded(
                        RoundRect(
                            rect = Rect(
                                0f,
                                0f,
                                size.width,
                                size.height
                            ),
                            topLeft = CornerRadius(ts),
                            topRight = CornerRadius(te),
                            bottomRight = CornerRadius(be),
                            bottomLeft = CornerRadius(bs)
                        )
                    )
                }
            }
        } else shape
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(animatedShape),
        shape = animatedShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            supportingContent = {
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Normal,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon is ImageVector) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (icon is Int) {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick
                )
                .padding(vertical = 4.dp),
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
fun SettingsSwitchCard(
    icon: Any,
    title: String,
    subtitle: String,
    containerColor: Color,
    iconColor: Color,
    shape: Shape,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "anim_shape"
    )

    val animatedShape = remember(shape, pressProgress) {
        if (shape is RoundedCornerShape) {
            object : Shape {
                override fun createOutline(
                    size: Size,
                    layoutDirection: LayoutDirection,
                    density: Density
                ): Outline {
                    val targetPx = with(density) { 20.dp.toPx() }
                    fun lerp(start: Float, stop: Float, fraction: Float) =
                        (1 - fraction) * start + fraction * stop

                    val ts = lerp(shape.topStart.toPx(size, density), targetPx, pressProgress)
                    val te = lerp(shape.topEnd.toPx(size, density), targetPx, pressProgress)
                    val bs = lerp(shape.bottomStart.toPx(size, density), targetPx, pressProgress)
                    val be = lerp(shape.bottomEnd.toPx(size, density), targetPx, pressProgress)

                    return Outline.Rounded(
                        RoundRect(
                            rect = Rect(
                                0f,
                                0f,
                                size.width,
                                size.height
                            ),
                            topLeft = CornerRadius(ts),
                            topRight = CornerRadius(te),
                            bottomRight = CornerRadius(be),
                            bottomLeft = CornerRadius(bs)
                        )
                    )
                }
            }
        } else shape
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(animatedShape),
        shape = animatedShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            supportingContent = {
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Normal,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon is ImageVector) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (icon is Int) {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    thumbContent = {
                        if (checked) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    }
                )
            },
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = { onCheckedChange(!checked) }
                )
                .padding(vertical = 4.dp),
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}