package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.OpenInNew
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.DocumentEntity
import com.example.data.local.FolderEntity
import com.example.ui.util.AppIconView
import com.example.ui.viewmodel.NotesViewModel

data class CreateTarget(val folderId: Long?)

sealed class MoveTarget {
    data class MoveDoc(val doc: DocumentEntity) : MoveTarget()
    data class MoveFld(val folder: FolderEntity) : MoveTarget()
}

fun isDescendantOf(sourceFolderId: Long, targetFolderId: Long, allFolders: List<FolderEntity>): Boolean {
    if (sourceFolderId == targetFolderId) return true
    var current: FolderEntity? = allFolders.find { it.id == targetFolderId }
    while (current != null) {
        if (current.parentId == sourceFolderId) return true
        val nextParentId = current.parentId ?: return false
        current = allFolders.find { it.id == nextParentId }
    }
    return false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesTreeScreen(viewModel: NotesViewModel) {
    val activeWorkspace by viewModel.activeWorkspace.collectAsStateWithLifecycle()
    val folders by viewModel.foldersInWorkspace.collectAsStateWithLifecycle()
    val documents by viewModel.documentsInWorkspace.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Dialog states - explicitly using CreateTarget to distinguish root vs folder
    var docCreateTarget by remember { mutableStateOf<CreateTarget?>(null) }
    var folderCreateTarget by remember { mutableStateOf<CreateTarget?>(null) }
    var folderToEdit by remember { mutableStateOf<FolderEntity?>(null) }
    var folderToDelete by remember { mutableStateOf<FolderEntity?>(null) }
    var docToDelete by remember { mutableStateOf<DocumentEntity?>(null) }
    var itemToMove by remember { mutableStateOf<MoveTarget?>(null) }
    var draggingTarget by remember { mutableStateOf<MoveTarget?>(null) }

    val expandedFolders = remember { mutableMapOf<Long, Boolean>() }

    val handleDropOnRoot: () -> Unit = {
        when (val t = draggingTarget) {
            is MoveTarget.MoveDoc -> viewModel.moveDocument(t.doc.id, null)
            is MoveTarget.MoveFld -> viewModel.moveFolder(t.folder.id, null)
            null -> {}
        }
        draggingTarget = null
    }

    val handleDropOnFolder: (Long) -> Unit = { targetFolderId ->
        when (val t = draggingTarget) {
            is MoveTarget.MoveDoc -> viewModel.moveDocument(t.doc.id, targetFolderId)
            is MoveTarget.MoveFld -> viewModel.moveFolder(t.folder.id, targetFolderId)
            null -> {}
        }
        draggingTarget = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Column {
                        Text(
                            stringResource(R.string.files_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        activeWorkspace?.let { ws ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppIconView(icon = ws.icon, size = 14.dp, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    ws.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(
                            if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = stringResource(R.string.search_files_placeholder)
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
                    placeholder = { Text(stringResource(R.string.search_files_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    singleLine = true
                )
            }

            // Quick Create Bar - WORKING BUTTONS FOR ROOT NOTE AND FOLDER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { docCreateTarget = CreateTarget(folderId = null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.new_note))
                }

                OutlinedButton(
                    onClick = { folderCreateTarget = CreateTarget(folderId = null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.new_folder))
                }
            }

            // Empty state
            if (folders.isEmpty() && documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📁", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.files_empty_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.files_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Drag and drop in-progress banner
                AnimatedVisibility(
                    visible = draggingTarget != null,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DriveFileMove, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val itemName = when (val t = draggingTarget) {
                                    is MoveTarget.MoveDoc -> t.doc.title
                                    is MoveTarget.MoveFld -> t.folder.name
                                    null -> ""
                                }
                                Text(
                                    stringResource(R.string.moving_item, itemName),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    stringResource(R.string.drag_to_move),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            IconButton(onClick = { draggingTarget = null }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel_drag), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {
                    // Root drop zone when dragging
                    if (draggingTarget != null) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { handleDropOnRoot() },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        stringResource(R.string.drop_to_root),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Render Top-level Folders (parentId == null)
                    val rootFolders = folders.filter { it.parentId == null }
                    items(rootFolders.size, key = { "folder_${rootFolders[it].id}" }) { idx ->
                        val folder = rootFolders[idx]
                        FolderTreeNode(
                            folder = folder,
                            allFolders = folders,
                            allDocuments = documents,
                            depth = 0,
                            expandedFolders = expandedFolders,
                            searchQuery = searchQuery,
                            viewModel = viewModel,
                            draggingTarget = draggingTarget,
                            onStartDrag = { draggingTarget = it },
                            onDropOnFolder = handleDropOnFolder,
                            onToggleExpand = { fId ->
                                val current = expandedFolders[fId] ?: true
                                expandedFolders[fId] = !current
                            },
                            onAddDocInFolder = { fId -> docCreateTarget = CreateTarget(folderId = fId) },
                            onAddSubfolder = { fId -> folderCreateTarget = CreateTarget(folderId = fId) },
                            onEditFolder = { f -> folderToEdit = f },
                            onDeleteFolder = { f -> folderToDelete = f },
                            onDeleteDoc = { d -> docToDelete = d },
                            onMoveItem = { target -> itemToMove = target }
                        )
                    }

                    // Render Root Documents (folderId == null)
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
                                stringResource(R.string.root_files),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(rootDocuments.size, key = { "doc_${rootDocuments[it].id}" }) { idx ->
                            val doc = rootDocuments[idx]
                            val isDocDragged = draggingTarget is MoveTarget.MoveDoc && (draggingTarget as MoveTarget.MoveDoc).doc.id == doc.id
                            DocumentTreeItem(
                                document = doc,
                                depth = 0,
                                viewModel = viewModel,
                                isBeingDragged = isDocDragged,
                                onStartDrag = { draggingTarget = MoveTarget.MoveDoc(doc) },
                                onDelete = { docToDelete = doc },
                                onMove = { itemToMove = MoveTarget.MoveDoc(doc) }
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
    docCreateTarget?.let { target ->
        CreateDocumentDialog(
            folderId = target.folderId,
            onConfirm = { title, icon, colorHex ->
                viewModel.createDocument(target.folderId, title, icon, colorHex)
                docCreateTarget = null
            },
            onDismiss = { docCreateTarget = null }
        )
    }

    // Dialog: Create Folder
    folderCreateTarget?.let { target ->
        CreateFolderDialog(
            parentId = target.folderId,
            onConfirm = { name, icon, colorHex ->
                viewModel.createFolder(target.folderId, name, icon, colorHex)
                folderCreateTarget = null
            },
            onDismiss = { folderCreateTarget = null }
        )
    }

    // Dialog: Move Item (Drag'n'drop / Move to folder)
    itemToMove?.let { target ->
        MoveToFolderDialog(
            target = target,
            allFolders = folders,
            onConfirm = { destFolderId ->
                when (target) {
                    is MoveTarget.MoveDoc -> viewModel.moveDocument(target.doc.id, destFolderId)
                    is MoveTarget.MoveFld -> viewModel.moveFolder(target.folder.id, destFolderId)
                }
                itemToMove = null
            },
            onDismiss = { itemToMove = null }
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
            title = stringResource(R.string.delete_folder),
            message = stringResource(R.string.delete_folder_confirm, f.name),
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
            title = stringResource(R.string.delete_doc),
            message = stringResource(R.string.delete_doc_confirm, d.title),
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
    draggingTarget: MoveTarget? = null,
    onStartDrag: (MoveTarget) -> Unit = {},
    onDropOnFolder: (Long) -> Unit = {},
    onToggleExpand: (Long) -> Unit,
    onAddDocInFolder: (Long) -> Unit,
    onAddSubfolder: (Long) -> Unit,
    onEditFolder: (FolderEntity) -> Unit,
    onDeleteFolder: (FolderEntity) -> Unit,
    onDeleteDoc: (DocumentEntity) -> Unit,
    onMoveItem: (MoveTarget) -> Unit
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

    val isBeingDragged = draggingTarget is MoveTarget.MoveFld && draggingTarget.folder.id == folder.id
    val canDropHere = draggingTarget != null && when (draggingTarget) {
        is MoveTarget.MoveDoc -> draggingTarget.doc.folderId != folder.id
        is MoveTarget.MoveFld -> draggingTarget.folder.id != folder.id && !isDescendantOf(draggingTarget.folder.id, folder.id, allFolders)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (depth * 16).dp, top = 2.dp, bottom = 2.dp)
                .clickable { onToggleExpand(folder.id) },
            colors = CardDefaults.cardColors(
                containerColor = if (isBeingDragged) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = if (isBeingDragged) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
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
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Folder Icon (Support Emoji + Material)
                AppIconView(icon = folder.icon, size = 20.dp, fontSize = 20.sp, tint = markerColor)

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

                // Drag handle
                IconButton(
                    onClick = { onStartDrag(MoveTarget.MoveFld(folder)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.DragIndicator,
                        contentDescription = stringResource(R.string.drag_to_move),
                        tint = if (isBeingDragged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Context menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(18.dp))
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.new_note_in_folder)) },
                            onClick = {
                                menuExpanded = false
                                onAddDocInFolder(folder.id)
                            },
                            leadingIcon = { Icon(Icons.Default.NoteAdd, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.new_subfolder)) },
                            onClick = {
                                menuExpanded = false
                                onAddSubfolder(folder.id)
                            },
                            leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.move_item)) },
                            onClick = {
                                menuExpanded = false
                                onMoveItem(MoveTarget.MoveFld(folder))
                            },
                            leadingIcon = { Icon(Icons.Default.DriveFileMove, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit_folder)) },
                            onClick = {
                                menuExpanded = false
                                onEditFolder(folder)
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete_folder), color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDeleteFolder(folder)
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
        }

        // Drop affordance for this folder when dragging
        if (canDropHere) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = (depth * 16 + 12).dp, top = 2.dp, bottom = 4.dp)
                    .clickable { onDropOnFolder(folder.id) },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DriveFileMove, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.drop_into_folder, folder.name),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
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
                        draggingTarget = draggingTarget,
                        onStartDrag = onStartDrag,
                        onDropOnFolder = onDropOnFolder,
                        onToggleExpand = onToggleExpand,
                        onAddDocInFolder = onAddDocInFolder,
                        onAddSubfolder = onAddSubfolder,
                        onEditFolder = onEditFolder,
                        onDeleteFolder = onDeleteFolder,
                        onDeleteDoc = onDeleteDoc,
                        onMoveItem = onMoveItem
                    )
                }

                // Documents in folder
                childDocs.forEach { doc ->
                    val isDocDragged = draggingTarget is MoveTarget.MoveDoc && (draggingTarget as MoveTarget.MoveDoc).doc.id == doc.id
                    DocumentTreeItem(
                        document = doc,
                        depth = depth + 1,
                        viewModel = viewModel,
                        isBeingDragged = isDocDragged,
                        onStartDrag = { onStartDrag(MoveTarget.MoveDoc(doc)) },
                        onDelete = { onDeleteDoc(doc) },
                        onMove = { onMoveItem(MoveTarget.MoveDoc(doc)) }
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
    isBeingDragged: Boolean = false,
    onStartDrag: () -> Unit = {},
    onDelete: () -> Unit,
    onMove: () -> Unit
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
            containerColor = if (isBeingDragged) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isBeingDragged) 1.5.dp else 1.dp,
            if (isBeingDragged) MaterialTheme.colorScheme.primary else markerColor.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document Icon (Support Emoji + Material)
            AppIconView(icon = document.icon, size = 20.dp, fontSize = 20.sp, tint = markerColor)

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
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    // Status Tag Badge
                    if (document.statusTag.isNotBlank()) {
                        Text(
                            text = document.statusTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Calendar date badge if present
                    document.calendarDate?.let { cDate ->
                        Text(
                            text = "📅 ${viewModel.formatDate(cDate)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Tags
                    val tags = document.tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    if (tags.isNotEmpty()) {
                        Text(
                            text = tags.take(2).joinToString(" ") { "#$it" } + if (tags.size > 2) "…" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Drag handle
            IconButton(
                onClick = onStartDrag,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.DragIndicator,
                    contentDescription = stringResource(R.string.drag_to_move),
                    tint = if (isBeingDragged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Context menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(18.dp))
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.open_in_editor)) },
                        onClick = {
                            menuExpanded = false
                            viewModel.openDocumentInEditor(document.id)
                        },
                        leadingIcon = { Icon(Icons.Default.OpenInNew, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.move_item)) },
                        onClick = {
                            menuExpanded = false
                            onMove()
                        },
                        leadingIcon = { Icon(Icons.Default.DriveFileMove, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete_doc), color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
    }
}

@Composable
fun MoveToFolderDialog(
    target: MoveTarget,
    allFolders: List<FolderEntity>,
    onConfirm: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val itemName = when (target) {
        is MoveTarget.MoveDoc -> target.doc.title
        is MoveTarget.MoveFld -> target.folder.name
    }

    // If moving a folder, exclude itself and its descendants
    val validFolders = if (target is MoveTarget.MoveFld) {
        val selfAndDescendants = mutableSetOf(target.folder.id)
        var addedMore = true
        while (addedMore) {
            addedMore = false
            for (f in allFolders) {
                if (f.parentId != null && f.parentId in selfAndDescendants && f.id !in selfAndDescendants) {
                    selfAndDescendants.add(f.id)
                    addedMore = true
                }
            }
        }
        allFolders.filter { it.id !in selfAndDescendants }
    } else {
        allFolders
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.move_item)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = itemName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Option: Root
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onConfirm(null) },
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.move_to_root), fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                    }
                }

                // List of folders
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(validFolders.size) { idx ->
                        val f = validFolders[idx]
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onConfirm(f.id) },
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppIconView(icon = f.icon, size = 18.dp, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(f.name, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun CreateDocumentDialog(
    folderId: Long?,
    onConfirm: (title: String, icon: String, colorHex: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("📝") }
    var colorHex by remember { mutableStateOf("#3B82F6") }

    var showIconPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_note)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.note_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

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
                        Text(stringResource(R.string.icon))
                        Spacer(modifier = Modifier.width(6.dp))
                        AppIconView(icon = icon, size = 24.dp, fontSize = 24.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showColorPicker = true }
                            .padding(8.dp)
                    ) {
                        Text(stringResource(R.string.color))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(parseColorHex(colorHex))
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), icon, colorHex)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

    if (showIconPicker) {
        IconPickerDialog(
            currentIcon = icon,
            onIconSelected = { icon = it },
            onDismiss = { showIconPicker = false }
        )
    }

    if (showColorPicker) {
        ColorPickerDialog(
            currentColorHex = colorHex,
            onColorSelected = { colorHex = it },
            onDismiss = { showColorPicker = false }
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
    var icon by remember { mutableStateOf("📁") }
    var colorHex by remember { mutableStateOf("#3B82F6") }

    var showIconPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (parentId == null) stringResource(R.string.new_folder) else stringResource(R.string.new_subfolder)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.folder_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

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
                        Text(stringResource(R.string.icon))
                        Spacer(modifier = Modifier.width(6.dp))
                        AppIconView(icon = icon, size = 24.dp, fontSize = 24.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showColorPicker = true }
                            .padding(8.dp)
                    ) {
                        Text(stringResource(R.string.color))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(parseColorHex(colorHex))
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), icon, colorHex)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

    if (showIconPicker) {
        IconPickerDialog(
            currentIcon = icon,
            onIconSelected = { icon = it },
            onDismiss = { showIconPicker = false }
        )
    }

    if (showColorPicker) {
        ColorPickerDialog(
            currentColorHex = colorHex,
            onColorSelected = { colorHex = it },
            onDismiss = { showColorPicker = false }
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
    var icon by remember { mutableStateOf(folder.icon) }
    var colorHex by remember { mutableStateOf(folder.colorHex) }

    var showIconPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_folder)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.folder_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

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
                        Text(stringResource(R.string.icon))
                        Spacer(modifier = Modifier.width(6.dp))
                        AppIconView(icon = icon, size = 24.dp, fontSize = 24.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showColorPicker = true }
                            .padding(8.dp)
                    ) {
                        Text(stringResource(R.string.color))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(parseColorHex(colorHex))
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(folder.copy(name = name.trim(), icon = icon, colorHex = colorHex, updatedAt = System.currentTimeMillis()))
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

    if (showIconPicker) {
        IconPickerDialog(
            currentIcon = icon,
            onIconSelected = { icon = it },
            onDismiss = { showIconPicker = false }
        )
    }

    if (showColorPicker) {
        ColorPickerDialog(
            currentColorHex = colorHex,
            onColorSelected = { colorHex = it },
            onDismiss = { showColorPicker = false }
        )
    }
}
