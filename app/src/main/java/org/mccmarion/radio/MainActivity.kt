package org.mccmarion.radio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.mccmarion.radio.ui.screens.AboutScreen
import org.mccmarion.radio.ui.screens.PlayerScreen
import org.mccmarion.radio.ui.screens.ScheduleScreen
import org.mccmarion.radio.ui.theme.MCCRadioTheme
import org.mccmarion.radio.util.NetworkMonitor

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Player : Screen("player", Config.STATION_NAME, Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle)
    data object Schedule : Screen("schedule", "Schedule", Icons.Filled.Schedule, Icons.Outlined.Schedule)
    data object About : Screen("about", "About", Icons.Filled.Info, Icons.Outlined.Info)
}

class MainActivity : ComponentActivity() {

    private val screens = listOf(Screen.Player, Screen.Schedule, Screen.About)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MCCRadioApplication
        val networkMonitor = NetworkMonitor(this)

        setContent {
            MCCRadioTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val context = LocalContext.current

                // Collect states
                val playbackState by app.audioManager.playbackState.collectAsStateWithLifecycle()
                val currentQuality by app.audioManager.currentQuality.collectAsStateWithLifecycle()
                val currentTrack by app.metadataService.currentTrack.collectAsStateWithLifecycle()
                val albumArtUrl by app.metadataService.albumArtUrl.collectAsStateWithLifecycle()
                val listeners by app.metadataService.listeners.collectAsStateWithLifecycle()
                val isLive by app.metadataService.isLive.collectAsStateWithLifecycle()
                val streamerName by app.metadataService.streamerName.collectAsStateWithLifecycle()
                val schedule by app.scheduleService.schedule.collectAsStateWithLifecycle()
                val scheduleLoading by app.scheduleService.isLoading.collectAsStateWithLifecycle()
                val scheduleError by app.scheduleService.error.collectAsStateWithLifecycle()
                val isConnected by networkMonitor.isConnected.collectAsStateWithLifecycle(initialValue = true)

                val coroutineScope = rememberCoroutineScope()

                // Get current screen for title
                val currentScreen = screens.find { screen ->
                    currentDestination?.hierarchy?.any { it.route == screen.route } == true
                } ?: Screen.Player

                // Start metadata polling
                LaunchedEffect(Unit) {
                    app.metadataService.startPolling()
                }

                // Update metadata in audio manager when track changes
                LaunchedEffect(currentTrack) {
                    currentTrack?.let { app.audioManager.updateMetadata(it) }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text(currentScreen.title) },
                            actions = {
                                // Show share button only on Player screen
                                if (currentScreen == Screen.Player) {
                                    IconButton(onClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, app.metadataService.shareText())
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share"))
                                    }) {
                                        Icon(Icons.Default.Share, contentDescription = "Share")
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            screens.forEach { screen ->
                                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            if (selected) screen.selectedIcon else screen.unselectedIcon,
                                            contentDescription = screen.title
                                        )
                                    },
                                    label = { Text(if (screen == Screen.Player) "Player" else screen.title) },
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Player.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Player.route) {
                            PlayerScreen(
                                playbackState = playbackState,
                                currentTrack = currentTrack,
                                albumArtUrl = albumArtUrl,
                                currentQuality = currentQuality,
                                listeners = listeners,
                                isLive = isLive,
                                streamerName = streamerName,
                                isConnected = isConnected,
                                onPlayPause = { app.audioManager.togglePlayPause() },
                                onReconnect = { app.audioManager.reconnect() },
                                onQualityChange = { app.audioManager.setQuality(it) }
                            )
                        }

                        composable(Screen.Schedule.route) {
                            // Load schedule when navigating to this screen
                            LaunchedEffect(Unit) {
                                app.scheduleService.loadSchedule()
                            }

                            ScheduleScreen(
                                schedule = schedule,
                                isLoading = scheduleLoading,
                                error = scheduleError,
                                currentDayIndex = app.scheduleService.currentDayIndex(),
                                onRefresh = {
                                    coroutineScope.launch {
                                        app.scheduleService.refresh()
                                    }
                                }
                            )
                        }

                        composable(Screen.About.route) {
                            AboutScreen()
                        }
                    }
                }
            }
        }
    }
}
