package com.sippy.app.ui

import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sippy.app.R
import com.sippy.app.ui.screens.AboutScreen
import com.sippy.app.ui.screens.CoachScreen
import com.sippy.app.ui.screens.DevOptionsScreen
import com.sippy.app.ui.screens.FriendsScreen
import com.sippy.app.ui.screens.HistoryScreen
import com.sippy.app.ui.screens.HomeScreen
import com.sippy.app.ui.screens.OnboardingScreen
import com.sippy.app.ui.screens.SettingsScreen
import com.sippy.app.ui.theme.AppBackdrop
import com.sippy.app.ui.theme.SippyTheme

private data class Tab(val route: String, val label: String, val icon: Int)

private val tabs = listOf(
    Tab("home", "Home", R.drawable.ic_home),
    Tab("friends", "Friends", R.drawable.ic_friends),
    Tab("history", "History", R.drawable.ic_history),
    Tab("coach", "Coach", R.drawable.ic_coach),
    Tab("settings", "Settings", R.drawable.ic_settings)
)

@Composable
fun SippyNavigation(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val systemReduceMotion = remember {
        try {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        } catch (e: Exception) { false }
    }
    val reduceMotion = systemReduceMotion || (state.settings?.reduceMotion ?: false)
    CompositionLocalProvider(LocalAnimationsEnabled provides !reduceMotion) {
        SippyTheme(state.settings?.themeMode ?: "system", state.settings?.accent ?: "aqua") {
            val nav = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }
            val cfg = state.settings
            LaunchedEffect(state.feedback.toast) {
                state.feedback.toast?.let {
                    snackbarHostState.showSnackbar(it)
                    viewModel.act(UiAction.ClearToast)
                }
            }
            if (cfg == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@SippyTheme
            }
            val start = if (cfg.onboardingDone) "home" else "onboarding"
            LaunchedEffect(cfg.onboardingDone) {
                if (!cfg.onboardingDone && nav.currentDestination?.route != "onboarding") {
                    nav.navigate("onboarding") {
                        popUpTo(nav.graph.startDestinationId) { inclusive = true }
                    }
                }
            }
            val backStack by nav.currentBackStackEntryAsState()
            val route = backStack?.destination?.route
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (route in tabs.map { it.route }) {
                        NavigationBar {
                            tabs.forEach { tab ->
                                NavigationBarItem(
                                    selected = route == tab.route,
                                    onClick = { nav.navigate(tab.route) { launchSingleTop = true } },
                                    icon = { Icon(painterResource(tab.icon), contentDescription = tab.label) },
                                    label = { Text(tab.label) }
                                )
                            }
                        }
                    }
                }
            ) { padding ->
                Box(Modifier.padding(padding).fillMaxSize()) {
                    AppBackdrop(animationsEnabled = !reduceMotion)
                    NavHost(nav, startDestination = start, modifier = Modifier.fillMaxSize()) {
                    composable("onboarding") { OnboardingScreen(viewModel::act) }
                    composable("home") { HomeScreen(state, viewModel::act) }
                    composable("friends") { FriendsScreen(state, viewModel::act) }
                    composable("history") { HistoryScreen(state, viewModel::act) }
                    composable("coach") { CoachScreen(state, viewModel::act) { nav.navigate("settings") } }
                    composable("settings") { SettingsScreen(state, viewModel::act, nav::navigate) }
                    composable("about") { AboutScreen(viewModel::act) { nav.popBackStack() } }
                    composable("dev") { DevOptionsScreen(state, viewModel::act) { nav.popBackStack() } }
                }
                }
            }
        }
    }
}
