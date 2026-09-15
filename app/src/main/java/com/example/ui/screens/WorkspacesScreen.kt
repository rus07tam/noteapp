package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.WorkspaceEntity
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspacesScreen(viewModel: NotesViewModel) {
    val workspaces by viewModel.allWorkspaces.collectAsStateWithLifecycle()
    val activeWorkspaceId by viewModel.activeWorkspaceId.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var workspaceToRename by remember { mutableStateOf<WorkspaceEntity?>(null) }
    var workspaceToClone by remember { mutableStateOf<WorkspaceEntity?>(null) }
    var workspaceToDelete by remember { mutableStateOf<WorkspaceEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Воркспейсы", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Управление рабочими пространствами",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Создать воркспейс")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Файлы, календарь и теги отображаются в контексте выбранного активного воркспейса.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(workspaces, key = { it.id }) { ws ->
                val isActive = ws.id == activeWorkspaceId
                val markerColor = parseColorHex(ws.colorHex)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.setActiveWorkspace(ws.id)
                            viewModel.navigateTo(AppScreen.FILES)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = if (isActive) {
                        androidx.compose.foundation.BorderStroke(2.dp, markerColor)
                    } else {
                        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon with colored badge
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(markerColor.copy(alpha = 0.15f))
                                .border(1.5.dp, markerColor, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = ws.icon, fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = ws.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isActive) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Активный",
                                        tint = markerColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Обновлено: ${viewModel.formatDate(ws.updatedAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // More Actions Menu
                        var menuExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Действия")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                if (!isActive) {
                                    DropdownMenuItem(
                                        text = { Text("Выбрать как активный") },
                                        leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                                        onClick = {
                                            menuExpanded = false
                                            viewModel.setActiveWorkspace(ws.id)
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Переименовать") },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        workspaceToRename = ws
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Клонировать") },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        workspaceToClone = ws
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        menuExpanded = false
                                        workspaceToDelete = ws
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Dialog: Create Workspace
    if (showCreateDialog) {
        CreateWorkspaceDialog(
            onConfirm = { name, icon, colorHex ->
                viewModel.createWorkspace(name, icon, colorHex)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    // Dialog: Rename Workspace
    workspaceToRename?.let { ws ->
        SimpleInputDialog(
            title = "Переименовать воркспейс",
            initialValue = ws.name,
            label = "Новое название",
            onConfirm = { newName ->
                viewModel.renameWorkspace(ws, newName)
                workspaceToRename = null
            },
            onDismiss = { workspaceToRename = null }
        )
    }

    // Dialog: Clone Workspace
    workspaceToClone?.let { ws ->
        SimpleInputDialog(
            title = "Клонировать воркспейс",
            initialValue = "${ws.name} (Копия)",
            label = "Название копии",
            onConfirm = { cloneName ->
                viewModel.cloneWorkspace(ws.id, cloneName)
                workspaceToClone = null
            },
            onDismiss = { workspaceToClone = null }
        )
    }

    // Dialog: Delete Workspace
    workspaceToDelete?.let { ws ->
        val isOnlyOne = workspaces.size <= 1
        if (isOnlyOne) {
            AlertDialog(
                onDismissRequest = { workspaceToDelete = null },
                title = { Text("Нельзя удалить") },
                text = { Text("В приложении должен оставаться хотя бы один воркспейс.") },
                confirmButton = {
                    TextButton(onClick = { workspaceToDelete = null }) {
                        Text("Понятно")
                    }
                }
            )
        } else {
            ConfirmDeleteDialog(
                title = "Удалить воркспейс?",
                message = "Вы действительно хотите удалить '${ws.name}'? Все вложенные папки, документы и блоки будут безвозвратно удалены.",
                onConfirm = {
                    viewModel.deleteWorkspace(ws)
                    workspaceToDelete = null
                },
                onDismiss = { workspaceToDelete = null }
            )
        }
    }
}

@Composable
fun CreateWorkspaceDialog(
    onConfirm: (name: String, icon: String, colorHex: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("💼") }
    var selectedColorHex by remember { mutableStateOf("#3B82F6") }
    var showColorPicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый воркспейс") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название воркспейса") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon Picker trigger
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showIconPicker = true }
                            .padding(8.dp)
                    ) {
                        Text("Иконка: ", style = MaterialTheme.typography.bodyMedium)
                        Text(selectedIcon, fontSize = 24.sp)
                    }

                    // Color Picker trigger
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showColorPicker = true }
                            .padding(8.dp)
                    ) {
                        Text("Цвет: ", style = MaterialTheme.typography.bodyMedium)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(parseColorHex(selectedColorHex))
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), selectedIcon, selectedColorHex)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )

    if (showColorPicker) {
        ColorPickerDialog(
            currentColorHex = selectedColorHex,
            onColorSelected = { selectedColorHex = it },
            onDismiss = { showColorPicker = false }
        )
    }

    if (showIconPicker) {
        IconPickerDialog(
            currentIcon = selectedIcon,
            onIconSelected = { selectedIcon = it },
            onDismiss = { showIconPicker = false }
        )
    }
}
