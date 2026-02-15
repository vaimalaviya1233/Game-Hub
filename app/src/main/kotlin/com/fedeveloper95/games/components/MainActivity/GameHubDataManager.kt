package com.fedeveloper95.games.components.mainactivity

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.roundToInt

data class GameApp(
    val name: String,
    val packageName: String,
    val launchIntent: Intent?,
    val launchCount: Int = 0,
    val totalPlayTime: Long = 0
)

class GameViewModel : ViewModel() {
    private val _games = MutableStateFlow<List<GameApp>>(emptyList())
    val games: StateFlow<List<GameApp>> = _games

    private val _allApps = MutableStateFlow<List<GameApp>>(emptyList())
    val allApps: StateFlow<List<GameApp>> = _allApps

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val HIDDEN_GAMES_PREF = "hidden_games"
    private val MANUAL_GAMES_PREF = "manual_games"
    private val GAME_ORDER_PREF = "game_order"
    private val PREF_PREFIX_COUNT = "play_count_"
    private val PREF_SORT_TYPE = "pref_sort_type"
    private val PREF_STATS_INTERVAL = "pref_stats_interval"

    private var loadJob: Job? = null

    fun loadGames(context: Context) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            val packageManager = context.packageManager
            val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
            val settings = context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE)

            val loadedData = withContext(Dispatchers.IO) {
                try {
                    val hiddenGames = getPrefsSet(context, HIDDEN_GAMES_PREF)
                    val manualGames = getPrefsSet(context, MANUAL_GAMES_PREF)
                    val savedOrder = getSavedOrder(context)
                    val sortType = settings.getString(PREF_SORT_TYPE, "Alphabetical") ?: "Alphabetical"
                    val statsInterval = settings.getFloat(PREF_STATS_INTERVAL, 3f).roundToInt()

                    val intent = Intent(Intent.ACTION_MAIN, null)
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    val resolveInfos = packageManager.queryIntentActivities(intent, 0)

                    val gamesList = mutableListOf<GameApp>()
                    val allAppsList = mutableListOf<GameApp>()

                    val usageStats = getUsageStats(context, statsInterval)

                    for (resolveInfo in resolveInfos) {
                        val packageName = resolveInfo.activityInfo.packageName ?: continue
                        val name = resolveInfo.loadLabel(packageManager).toString()
                        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: continue

                        val appInfo = try {
                            packageManager.getApplicationInfo(packageName, 0)
                        } catch (e: PackageManager.NameNotFoundException) { continue }

                        val isDeclaredGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            appInfo.category == ApplicationInfo.CATEGORY_GAME
                        } else {
                            (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
                        }

                        val isManualGame = manualGames.contains(packageName)
                        val isHidden = hiddenGames.contains(packageName)
                        val count = prefs.getInt(PREF_PREFIX_COUNT + packageName, 0)
                        val time = usageStats[packageName] ?: 0L

                        val app = GameApp(name, packageName, launchIntent, count, time)

                        if (!isDeclaredGame || isHidden) {
                            if (!isManualGame) allAppsList.add(app)
                        }

                        if ((isDeclaredGame || isManualGame) && !isHidden) {
                            gamesList.add(app)
                        }
                    }

                    val sortedGames = when (sortType) {
                        "Time" -> gamesList.sortedByDescending { it.totalPlayTime }
                        "Custom" -> {
                            if (savedOrder.isNotEmpty()) {
                                gamesList.sortedBy { game ->
                                    val index = savedOrder.indexOf(game.packageName)
                                    if (index != -1) index else Int.MAX_VALUE
                                }
                            } else {
                                gamesList.sortedBy { it.name }
                            }
                        }
                        else -> gamesList.sortedBy { it.name }
                    }

                    Pair(sortedGames, allAppsList.sortedBy { it.name })
                } catch (e: Exception) {
                    Pair(emptyList(), emptyList())
                }
            }

            _games.value = loadedData.first
            _allApps.value = loadedData.second
            _isLoading.value = false
        }
    }

    private fun getUsageStats(context: Context, intervalIndex: Int): Map<String, Long> {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }

        if (mode != AppOpsManager.MODE_ALLOWED) return emptyMap()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis

        when (intervalIndex) {
            0 -> calendar.add(Calendar.DAY_OF_YEAR, -1)
            1 -> calendar.add(Calendar.WEEK_OF_YEAR, -1)
            2 -> calendar.add(Calendar.MONTH, -1)
            3 -> calendar.add(Calendar.YEAR, -1)
        }
        val startTime = calendar.timeInMillis

        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        val usageMap = mutableMapOf<String, Long>()

        if (stats != null) {
            for (usageStat in stats) {
                val current = usageMap[usageStat.packageName] ?: 0L
                usageMap[usageStat.packageName] = current + usageStat.totalTimeInForeground
            }
        }
        return usageMap
    }

    fun incrementLaunchCount(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        val currentCount = prefs.getInt(PREF_PREFIX_COUNT + packageName, 0)
        prefs.edit().putInt(PREF_PREFIX_COUNT + packageName, currentCount + 1).apply()

        val updatedGames = _games.value.map { game ->
            if (game.packageName == packageName) {
                game.copy(launchCount = currentCount + 1)
            } else {
                game
            }
        }
        _games.value = updatedGames
    }

    fun updateGamesOrder(newOrder: List<GameApp>) {
        _games.value = newOrder
    }

    fun saveOrder(context: Context) {
        val orderList = _games.value.map { it.packageName }
        val orderString = orderList.joinToString(",")
        val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(GAME_ORDER_PREF, orderString).apply()
    }

    fun switchToCustomSort(context: Context) {
        saveOrder(context)
        val prefs = context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_SORT_TYPE, "Custom").apply()
    }

    private fun getSavedOrder(context: Context): List<String> {
        val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        val orderString = prefs.getString(GAME_ORDER_PREF, "") ?: ""
        return if (orderString.isNotEmpty()) orderString.split(",") else emptyList()
    }

    fun hideGame(context: Context, packageName: String) {
        removeFromPrefs(context, MANUAL_GAMES_PREF, packageName)
        addToPrefs(context, HIDDEN_GAMES_PREF, packageName)
        loadGames(context)
    }

    fun addManualGame(context: Context, packageName: String) {
        removeFromPrefs(context, HIDDEN_GAMES_PREF, packageName)
        addToPrefs(context, MANUAL_GAMES_PREF, packageName)
        loadGames(context)
    }

    private fun getPrefsSet(context: Context, key: String): Set<String> {
        val prefs: SharedPreferences = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        return prefs.getStringSet(key, emptySet()) ?: emptySet()
    }

    private fun addToPrefs(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        val set = prefs.getStringSet(key, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.add(value)
        prefs.edit().putStringSet(key, set).apply()
    }

    private fun removeFromPrefs(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        val set = prefs.getStringSet(key, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        if (set.contains(value)) {
            set.remove(value)
            prefs.edit().putStringSet(key, set).apply()
        }
    }
}