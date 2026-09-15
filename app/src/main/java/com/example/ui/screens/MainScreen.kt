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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.NotesViewModel

@Composable
fun MainScreen(viewModel: NotesViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    val navItems = listOf(
        NavigationItemData(AppScreen.WORKSPACES, "Воркспейсы", Icons.Default.Dashboard),
        NavigationItemData(AppScreen.FILES, "Файлы", Icons.Default.Folder),
        NavigationItemData(AppScreen.EDITOR, "Редактор", Icons.Default.Edit),
        NavigationItemData(AppScreen.CALENDAR, "Календарь", Icons.Default.CalendarMonth),
        NavigationItemData(AppScreen.TAGS, "Теги", Icons.Default.Label),
        NavigationItemData(AppScreen.SETTINGS, "Настройки", Icons.Default.Settings)
    )

    if (isWideScreen) {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail {
                navItems.forEach { item ->
                    NavigationRailItem(
                        selected = currentScreen == item.screen,
                        onClick = { viewModel.navigateTo(item.screen) },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontSize = 11.sp) }
                    )
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                ScreenContent(currentScreen = currentScreen, viewModel = viewModel)
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    tonalElevation = 6.dp
                ) {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentScreen == item.screen,
                            onClick = { viewModel.navigateTo(item.screen) },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title, fontSize = 10.sp, maxLines = 1) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
