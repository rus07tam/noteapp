package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.DocumentEntity
import com.example.data.local.FolderEntity
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesTreeScreen(viewModel: NotesViewModel) {
    val activeWorkspace by viewModel.activeWorkspace.collectAsStateWithLifecycle()
    val folders by viewModel.foldersForActiveWorkspace.collectAsStateWithLifecycle()
    val documents by viewModel.documentsForActiveWorkspace.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Map to keep track of expanded folders (default expanded)
    val expandedFolders = remember { mutableStateMapOf<Long, Boolean>() }

    // Dialogs state
    var showCreateDocDialog by remember { mutableStateOf<Long?>(null) } // folderId or null for root
    var showCreateFolderDialog by remember { mutableStateOf<Long?>(null) } // parentId or null for root
    var folderToEdit by remember { mutableStateOf<FolderEntity?>(null) }
    var folderToDelete by remember { mutableStateOf<FolderEntity?>(null) }
    var docToDelete by remember { mutableStateOf<DocumentEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = activeWorkspace?.icon ?: "📁",
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = activeWorkspace?.name ?: "Воркспейс",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Древо файлов и папок",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(
                            if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Поиск"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search field if active
            if (isSearchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск файлов и папок...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Очистить")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    singleLine = true
                )
            }

            // Quick Create Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showCreateDocDialog = null },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Заметка")
                }

                OutlinedButton(
                    onClick = { showCreateFolderDialog = null },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Папка")
                }
            }

            // Empty state
            if (folders.isEmpty() && documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🗂️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "В этом воркспейсе пока пусто",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Создайте папку или добавьте первую заметку",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Render Root Folders and their tree
                    val rootFolders = folders.filter { it.parentId == null }
                        .let { list ->
                            if (searchQuery.isNotBlank()) {
                                list.filter { it.name.contains(searchQuery, ignoreCase = true) }
                            } else list
                        }

                    item {
                        rootFolders.forEach { folder ->
                            FolderTreeNode(
                                folder = folder,
                                allFolders = folders,
                                allDocuments = documents,
                                depth = 0,
                                expandedFolders = expandedFolders,
                                searchQuery = searchQuery,
                                viewModel = viewModel,
                                onToggleExpand = { fId ->
                                    val current = expandedFolders[fId] ?: true
                                    expandedFolders[fId] = !current
                                },
                                onAddDocInFolder = { fId -> showCreateDocDialog = fId },
                                onAddSubfolder = { fId -> showCreateFolderDialog = fId },
                                onEditFolder = { f -> folderToEdit = f },
                                onDeleteFolder = { f -> folderToDelete = f },
                                onDeleteDoc = { d -> docToDelete = d }
                            )
                        }
                    }

                    // Render Root Documents (no folder)
                    val rootDocuments = documents.filter { it.folderId == null }
                        .let { list ->
                            if (searchQuery.isNotBlank()) {
                                list.filter {
                                    it.title.contains(searchQuery, ignoreCase = true) ||
                                            it.tagsCsv.contains(searchQuery, ignoreCase = true) ||
                                            it.statusTag.contains(searchQuery, ignoreCase = true)
                                }
                            } else list
                        }

                    if (rootDocuments.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Файлы без папки",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(rootDocuments.size, key = { "doc_${rootDocuments[it].id}" }) { idx ->
                            val doc = rootDocuments[idx]
                            DocumentTreeItem(
                                document = doc,
                                depth = 0,
                                viewModel = viewModel,
                                onDelete = { docToDelete = doc }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Dialog: Create Document
    showCreateDocDialog?.let { folderId ->
        CreateDocumentDialog(
            folderId = folderId,
            onConfirm = { title, icon, colorHex ->
                viewModel.createDocument(folderId, title, icon, colorHex)
                showCreateDocDialog = null
            },
            onDismiss = { showCreateDocDialog = null }
        )
    }
    // Dialog: Create Doc at root
    if (showCreateDocDialog == null && showCreateDocDialog != null) {
        // Handled above
    } else if (showCreateDocDialog != null && showCreateDocDialog == -1L) {
        // Special marker for root doc
        CreateDocumentDialog(
            folderId = null,
            onConfirm = { title, icon, colorHex ->
                viewModel.createDocument(null, title, icon, colorHex)
                showCreateDocDialog = null
            },
            onDismiss = { showCreateDocDialog = null }
        )
    }

    // Dialog: Create Folder
    showCreateFolderDialog?.let { parentId ->
        CreateFolderDialog(
            parentId = parentId,
            onConfirm = { name, icon, colorHex ->
                viewModel.createFolder(parentId, name, icon, colorHex)
                showCreateFolderDialog = null
            },
            onDismiss = { showCreateFolderDialog = null }
        )
    }

    // Dialog: Edit Folder
    folderToEdit?.let { f ->
        EditFolderDialog(
            folder = f,
            onConfirm = { updated ->
                viewModel.updateFolder(updated)
                folderToEdit = null
            },
            onDismiss = { folderToEdit = null }
        )
    }

    // Dialog: Delete Folder
    folderToDelete?.let { f ->
        ConfirmDeleteDialog(
            title = "Удалить папку?",
            message = "Вы уверены, что хотите удалить папку '${f.name}'? Все вложенные файлы и подпапки будут удалены.",
            onConfirm = {
                viewModel.deleteFolder(f)
                folderToDelete = null
            },
            onDismiss = { folderToDelete = null }
        )
    }

    // Dialog: Delete Document
    docToDelete?.let { d ->
        ConfirmDeleteDialog(
            title = "Удалить документ?",
            message = "Вы уверены, что хотите удалить '${d.title}'?",
            onConfirm = {
                viewModel.deleteDocument(d)
                docToDelete = null
            },
            onDismiss = { docToDelete = null }
        )
    }
}

@Composable
fun FolderTreeNode(
    folder: FolderEntity,
    allFolders: List<FolderEntity>,
    allDocuments: List<DocumentEntity>,
    depth: Int,
    expandedFolders: Map<Long, Boolean>,
    searchQuery: String,
    viewModel: NotesViewModel,
    onToggleExpand: (Long) -> Unit,
    onAddDocInFolder: (Long) -> Unit,
    onAddSubfolder: (Long) -> Unit,
    onEditFolder: (FolderEntity) -> Unit,
    onDeleteFolder: (FolderEntity) -> Unit,
    onDeleteDoc: (DocumentEntity) -> Unit
) {
    val isExpanded = expandedFolders[folder.id] ?: true
    val childFolders = allFolders.filter { it.parentId == folder.id }
    val childDocs = allDocuments.filter { it.folderId == folder.id }
        .let { list ->
            if (searchQuery.isNotBlank()) {
                list.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                            it.tagsCsv.contains(searchQuery, ignoreCase = true) ||
                            it.statusTag.contains(searchQuery, ignoreCase = true)
                }
            } else list
        }

    val markerColor = parseColorHex(folder.colorHex)
    var menuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (depth * 16).dp, top = 2.dp, bottom = 2.dp)
                .clickable { onToggleExpand(folder.id) },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expand chevron
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Folder Icon
                Text(text = folder.icon, fontSize = 20.sp)

                Spacer(modifier = Modifier.width(8.dp))

                // Folder Name and color marker dot
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(markerColor)
                    )
                }

                // Child count badge
                val totalItems = childFolders.size + childDocs.size
                if (totalItems > 0) {
                    Text(
                        text = "$totalItems",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // More Menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Действия", modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Новая заметка") },
                            leadingIcon = { Icon(Icons.Default.NoteAdd, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onAddDocInFolder(folder.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Новая подпапка") },
                            leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onAddSubfolder(folder.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Изменить папку") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEditFolder(folder)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Удалить папку", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDeleteFolder(folder)
                            }
                        )
                    }
                }
            }
        }

        // Subtree
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Subfolders
                childFolders.forEach { subFolder ->
                    FolderTreeNode(
                        folder = subFolder,
                        allFolders = allFolders,
                        allDocuments = allDocuments,
                        depth = depth + 1,
                        expandedFolders = expandedFolders,
                        searchQuery = searchQuery,
                        viewModel = viewModel,
                        onToggleExpand = onToggleExpand,
                        onAddDocInFolder = onAddDocInFolder,
                        onAddSubfolder = onAddSubfolder,
                        onEditFolder = onEditFolder,
                        onDeleteFolder = onDeleteFolder,
                        onDeleteDoc = onDeleteDoc
                    )
                }

                // Documents in folder
                childDocs.forEach { doc ->
                    DocumentTreeItem(
                        document = doc,
                        depth = depth + 1,
                        viewModel = viewModel,
                        onDelete = { onDeleteDoc(doc) }
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentTreeItem(
    document: DocumentEntity,
    depth: Int,
    viewModel: NotesViewModel,
    onDelete: () -> Unit
) {
    val markerColor = parseColorHex(document.colorHex)
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16 + 8).dp, top = 2.dp, bottom = 2.dp)
            .clickable {
                viewModel.openDocumentInEditor(document.id)
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            markerColor.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document Icon
            Text(text = document.icon, fontSize = 20.sp)

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(markerColor)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    // Status tag
                    if (document.statusTag.isNotBlank()) {
                        Text(
                            text = document.statusTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }

                    // Calendar badge
                    if (document.calendarDate != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = viewModel.formatDate(document.calendarDate),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Date updated
                    Text(
                        text = viewModel.formatDate(document.updatedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // More Menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Действия", modifier = Modifier.size(18.dp))
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Открыть в редакторе") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            viewModel.openDocumentInEditor(document.id)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CreateDocumentDialog(
    folderId: Long?,
    onConfirm: (title: String, icon: String, colorHex: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("📝") }
    var selectedColorHex by remember { mutableStateOf("#3B82F6") }
    var showColorPicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая заметка") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название заметки") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), selectedIcon, selectedColorHex)
                    }
                },
                enabled = title.isNotBlank()
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

@Composable
fun CreateFolderDialog(
    parentId: Long?,
    onConfirm: (name: String, icon: String, colorHex: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("📁") }
    var selectedColorHex by remember { mutableStateOf("#3B82F6") }
    var showColorPicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (parentId == null) "Новая папка" else "Новая вложенная папка") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название папки") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

@Composable
fun EditFolderDialog(
    folder: FolderEntity,
    onConfirm: (FolderEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(folder.name) }
    var selectedIcon by remember { mutableStateOf(folder.icon) }
    var selectedColorHex by remember { mutableStateOf(folder.colorHex) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать папку") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название папки") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                        onConfirm(folder.copy(name = name.trim(), icon = selectedIcon, colorHex = selectedColorHex))
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Сохранить")
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
