package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.NotesViewModel

@Composable
fun MainScreen(viewModel: NotesViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val hideNavLabels by viewModel.hideNavLabels.collectAsStateWithLifecycle()
    val renderUnderCutout by viewModel.renderUnderCutout.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    val navItems = listOf(
        NavigationItemData(AppScreen.WORKSPACES, R.string.nav_workspaces, Icons.Default.Dashboard),
        NavigationItemData(AppScreen.FILES, R.string.nav_files, Icons.Default.Folder),
        NavigationItemData(AppScreen.EDITOR, R.string.nav_editor, Icons.Default.Edit),
        NavigationItemData(AppScreen.CALENDAR, R.string.nav_calendar, Icons.Default.CalendarMonth),
        NavigationItemData(AppScreen.TAGS, R.string.nav_tags, Icons.Default.Label),
        NavigationItemData(AppScreen.SETTINGS, R.string.nav_settings, Icons.Default.Settings)
    )

    if (isWideScreen) {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail {
                navItems.forEach { item ->
                    val title = stringResource(item.titleRes)
                    NavigationRailItem(
                        selected = currentScreen == item.screen,
                        onClick = { viewModel.navigateTo(item.screen) },
                        icon = { Icon(item.icon, contentDescription = title) },
                        label = if (!hideNavLabels) {
                            { Text(title, fontSize = 11.sp) }
                        } else null,
                        alwaysShowLabel = !hideNavLabels
                    )
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                ScreenContent(currentScreen = currentScreen, viewModel = viewModel)
            }
        }
    } else {
        Scaffold(
            contentWindowInsets = if (renderUnderCutout) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
            bottomBar = {
                NavigationBar(
                    tonalElevation = 6.dp
                ) {
                    navItems.forEach { item ->
                        val title = stringResource(item.titleRes)
                        NavigationBarItem(
                            selected = currentScreen == item.screen,
                            onClick = { viewModel.navigateTo(item.screen) },
                            icon = { Icon(item.icon, contentDescription = title) },
                            label = if (!hideNavLabels) {
                                { Text(title, fontSize = 10.sp, maxLines = 1) }
                            } else null,
                            alwaysShowLabel = !hideNavLabels
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                ScreenContent(currentScreen = currentScreen, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ScreenContent(currentScreen: AppScreen, viewModel: NotesViewModel) {
    Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
        when (screen) {
            AppScreen.WORKSPACES -> WorkspacesScreen(viewModel = viewModel)
            AppScreen.FILES -> FilesTreeScreen(viewModel = viewModel)
            AppScreen.EDITOR -> EditorScreen(viewModel = viewModel)
            AppScreen.CALENDAR -> CalendarScreen(viewModel = viewModel)
            AppScreen.TAGS -> TagsScreen(viewModel = viewModel)
            AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
        }
    }
}

data class NavigationItemData(
    val screen: AppScreen,
    val titleRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
