package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.BlockEntity
import com.example.data.local.BlockType
import com.example.data.local.DocumentEntity
import com.example.data.local.TableHelper
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.NotesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(viewModel: NotesViewModel) {
    val context = LocalContext.current
    val activeDoc by viewModel.activeDocument.collectAsStateWithLifecycle()
    val blocks by viewModel.activeDocumentBlocks.collectAsStateWithLifecycle()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val isSearchVisible by viewModel.isSearchVisible.collectAsStateWithLifecycle()
    val searchQuery by viewModel.editorSearchQuery.collectAsStateWithLifecycle()

    var showColorPicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }
    var showEditTitleDialog by remember { mutableStateOf(false) }
    var showEditStatusDialog by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var showAddBlockSheet by remember { mutableStateOf(false) }
    var contextMenuBlock by remember { mutableStateOf<BlockEntity?>(null) }
    var exportMenuExpanded by remember { mutableStateOf(false) }

    if (activeDoc == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Редактор") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.FILES) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📝", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Документ не выбран", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.navigateTo(AppScreen.FILES) }) {
                        Text("Перейти к файлам")
                    }
                }
            }
        }
        return
    }

    val doc = activeDoc!!
    val markerColor = parseColorHex(doc.colorHex)

    Scaffold(
        topBar = {
            Column {
                // Visual marker color top strip accent
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(markerColor)
                )

                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = markerColor.copy(alpha = 0.08f)
                    ),
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.FILES) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "К файлам")
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                if (isEditMode) showEditTitleDialog = true
                            }
                        ) {
                            // Clickable icon to change emoji
                            Text(
                                text = doc.icon,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(enabled = isEditMode) { showIconPicker = true }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = doc.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            // Clickable color circle for changing color marker
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(markerColor)
                                    .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), CircleShape)
                                    .clickable { showColorPicker = true }
                            )
                        }
                    },
                    actions = {
                        // Toggle Read / Write mode
                        FilterChip(
                            selected = isEditMode,
                            onClick = { viewModel.toggleEditMode() },
                            label = { Text(if (isEditMode) "Правка" else "Чтение") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        // Find button
                        IconButton(onClick = { viewModel.toggleSearchVisible() }) {
                            Icon(
                                imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Поиск в заметке"
                            )
                        }

                        // Export dropdown
                        Box {
                            IconButton(onClick = { exportMenuExpanded = true }) {
                                Icon(Icons.Default.FileDownload, contentDescription = "Экспорт")
                            }
                            DropdownMenu(
                                expanded = exportMenuExpanded,
                                onDismissRequest = { exportMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Экспорт в Markdown (.md)") },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                    onClick = {
                                        exportMenuExpanded = false
                                        val md = viewModel.getMarkdownExport()
                                        copyAndShare(context, "${doc.title}.md", md)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Экспорт в Текст (.txt)") },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                    onClick = {
                                        exportMenuExpanded = false
                                        val txt = viewModel.getPlainTextExport()
                                        copyAndShare(context, "${doc.title}.txt", txt)
                                    }
                                )
                            }
                        }
                    }
                )

                // Search Bar in Editor
                AnimatedVisibility(visible = isSearchVisible) {
                    val matchCount = if (searchQuery.isBlank()) 0 else blocks.count {
                        it.content.contains(searchQuery, ignoreCase = true) ||
                                it.tableDataJson.contains(searchQuery, ignoreCase = true)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setEditorSearchQuery(it) },
                            placeholder = { Text("Поиск в документе...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    Text(
                                        text = "$matchCount найдено",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (isEditMode) {
                FloatingActionButton(
                    onClick = { showAddBlockSheet = true },
                    containerColor = markerColor,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить блок")
                }
            }
        }
    ) { padding ->
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                // METADATA TABLE
                DocumentMetadataTable(
                    document = doc,
                    formatDate = viewModel::formatDate,
                    isEditMode = isEditMode,
                    markerColor = markerColor,
                    onPickCalendarDate = {
                        showDatePicker(context, doc.calendarDate) { selectedMillis ->
                            viewModel.updateDocumentCalendarDate(selectedMillis)
                        }
                    },
                    onClearCalendarDate = {
                        viewModel.updateDocumentCalendarDate(null)
                    },
                    onEditStatus = { showEditStatusDialog = true },
                    onAddTag = { showAddTagDialog = true },
                    onRemoveTag = { tag -> viewModel.removeTagFromDocument(tag) }
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = markerColor.copy(alpha = 0.3f), thickness = 1.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // BLOCKS LIST
            items(blocks, key = { it.id }) { block ->
                val isMatch = searchQuery.isNotBlank() && (
                        block.content.contains(searchQuery, ignoreCase = true) ||
                                block.tableDataJson.contains(searchQuery, ignoreCase = true)
                        )

                BlockItemView(
                    block = block,
                    isEditMode = isEditMode,
                    isSearchMatch = isMatch,
                    onUpdateBlock = { updated -> viewModel.updateBlock(updated) },
                    onOpenContextMenu = { contextMenuBlock = block },
                    onMoveUp = { viewModel.moveBlockUp(block) },
                    onMoveDown = { viewModel.moveBlockDown(block) }
                )
            }

            item {
                if (blocks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Документ пуст. Нажмите '+' чтобы добавить первый блок.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }

    // Context Menu Dialog on Block Long Press
    contextMenuBlock?.let { block ->
        BlockContextMenuDialog(
            block = block,
            onDuplicate = {
                viewModel.duplicateBlock(block)
                contextMenuBlock = null
            },
            onMoveUp = {
                viewModel.moveBlockUp(block)
                contextMenuBlock = null
            },
            onMoveDown = {
                viewModel.moveBlockDown(block)
                contextMenuBlock = null
            },
            onDelete = {
                viewModel.deleteBlock(block)
                contextMenuBlock = null
            },
            onDismiss = { contextMenuBlock = null }
        )
    }

    // Add Block Dialog / Bottom Sheet
    if (showAddBlockSheet) {
        AddBlockDialog(
            onAdd = { type ->
                viewModel.addBlock(type)
                showAddBlockSheet = false
            },
            onDismiss = { showAddBlockSheet = false }
        )
    }

    // Dialog: Edit Title
    if (showEditTitleDialog) {
        SimpleInputDialog(
            title = "Изменить название заметки",
            initialValue = doc.title,
            label = "Название",
            onConfirm = { newTitle ->
                viewModel.updateDocument(doc.copy(title = newTitle))
                showEditTitleDialog = false
            },
            onDismiss = { showEditTitleDialog = false }
        )
    }

    // Dialog: Edit Status
    if (showEditStatusDialog) {
        SimpleInputDialog(
            title = "Тег статуса",
            initialValue = doc.statusTag,
            label = "Статус (например, 'В работе', 'Готово', 'Черновик')",
            onConfirm = { newStatus ->
                viewModel.updateDocumentStatusTag(newStatus)
                showEditStatusDialog = false
            },
            onDismiss = { showEditStatusDialog = false }
        )
    }

    // Dialog: Add Tag
    if (showAddTagDialog) {
        SimpleInputDialog(
            title = "Добавить тег",
            initialValue = "",
            label = "Название тега",
            onConfirm = { newTag ->
                viewModel.addTagToDocument(newTag)
                showAddTagDialog = false
            },
            onDismiss = { showAddTagDialog = false }
        )
    }

    // Dialog: Color Picker
    if (showColorPicker) {
        ColorPickerDialog(
            currentColorHex = doc.colorHex,
            onColorSelected = { hex ->
                viewModel.updateDocumentColor(hex)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    // Dialog: Icon Picker
    if (showIconPicker) {
        IconPickerDialog(
            currentIcon = doc.icon,
            onIconSelected = { icon ->
                viewModel.updateDocumentIcon(icon)
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }
}

// METADATA DISPLAY IN TABLE FORMAT
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DocumentMetadataTable(
    document: DocumentEntity,
    formatDate: (Long?) -> String,
    isEditMode: Boolean,
    markerColor: Color,
    onPickCalendarDate: () -> Unit,
    onClearCalendarDate: () -> Unit,
    onEditStatus: () -> Unit,
    onAddTag: () -> Unit,
    onRemoveTag: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Метаданные",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            // Table Grid Rows
            // Row 1: Created & Updated
            Row(modifier = Modifier.fillMaxWidth()) {
                MetadataCell(label = "Создано", value = formatDate(document.createdAt), modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                MetadataCell(label = "Изменено", value = formatDate(document.updatedAt), modifier = Modifier.weight(1f))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Row 2: Calendar Date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onPickCalendarDate() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = markerColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Дата в календаре:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (document.calendarDate != null) formatDate(document.calendarDate) else "Не указана (нажмите для выбора)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (document.calendarDate != null) markerColor else MaterialTheme.colorScheme.primary
                )
                if (document.calendarDate != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onClearCalendarDate,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Очистить дату", modifier = Modifier.size(16.dp))
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Row 3: Status Tag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onEditStatus() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Статус:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = markerColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, markerColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (document.statusTag.isNotBlank()) document.statusTag else "Указать статус",
                        style = MaterialTheme.typography.labelSmall,
                        color = markerColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.Edit, contentDescription = "Изменить статус", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Row 4: General Tags
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Общие теги:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onAddTag, modifier = Modifier.height(28.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Добавить", fontSize = 11.sp)
                    }
                }

                val tags = document.tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                if (tags.isEmpty()) {
                    Text(
                        "Теги отсутствуют",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        tags.forEach { tag ->
                            AssistChip(
                                onClick = { onRemoveTag(tag) },
                                label = { Text("#$tag", fontSize = 12.sp) },
                                trailingIcon = {
                                    Icon(Icons.Default.Close, contentDescription = "Удалить тег", modifier = Modifier.size(12.dp))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetadataCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

// BLOCK ITEM VIEW (READ & WRITE MODES)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BlockItemView(
    block: BlockEntity,
    isEditMode: Boolean,
    isSearchMatch: Boolean,
    onUpdateBlock: (BlockEntity) -> Unit,
    onOpenContextMenu: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val highlightBorder = if (isSearchMatch) {
        androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* normal click */ },
                onLongClick = { onOpenContextMenu() }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSearchMatch) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            } else if (isEditMode) {
                MaterialTheme.colorScheme.surface
            } else {
                Color.Transparent
            }
        ),
        border = highlightBorder ?: if (isEditMode) {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = if (isEditMode) 8.dp else 2.dp)
        ) {
            // Edit Header Bar for this block (only in edit mode)
            if (isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DragHandle,
                            contentDescription = "Переместить",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getBlockTypeLabel(block.type),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        IconButton(onClick = onMoveUp, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Вверх", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onMoveDown, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Вниз", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onOpenContextMenu, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Меню блока", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Render block content by Type
            when (block.type) {
                BlockType.PARAGRAPH -> {
                    if (isEditMode) {
                        OutlinedTextField(
                            value = block.content,
                            onValueChange = { onUpdateBlock(block.copy(content = it)) },
                            placeholder = { Text("Введите текст параграфа...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = block.content.ifBlank { " " },
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp
                        )
                    }
                }

                BlockType.HEADING_1 -> {
                    if (isEditMode) {
                        OutlinedTextField(
                            value = block.content,
                            onValueChange = { onUpdateBlock(block.copy(content = it)) },
                            placeholder = { Text("Заголовок H1...") },
                            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = block.content,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                }

                BlockType.HEADING_2 -> {
                    if (isEditMode) {
                        OutlinedTextField(
                            value = block.content,
                            onValueChange = { onUpdateBlock(block.copy(content = it)) },
                            placeholder = { Text("Заголовок H2...") },
                            textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = block.content,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                        )
                    }
                }

                BlockType.HEADING_3 -> {
                    if (isEditMode) {
                        OutlinedTextField(
                            value = block.content,
                            onValueChange = { onUpdateBlock(block.copy(content = it)) },
                            placeholder = { Text("Заголовок H3...") },
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = block.content,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                BlockType.DIVIDER -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                BlockType.BULLETED_LIST -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• ", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        if (isEditMode) {
                            OutlinedTextField(
                                value = block.content,
                                onValueChange = { onUpdateBlock(block.copy(content = it)) },
                                placeholder = { Text("Элемент списка...") },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Text(text = block.content, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                BlockType.NUMBERED_LIST -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${block.orderIndex + 1}. ", fontWeight = FontWeight.Bold)
                        if (isEditMode) {
                            OutlinedTextField(
                                value = block.content,
                                onValueChange = { onUpdateBlock(block.copy(content = it)) },
                                placeholder = { Text("Элемент списка...") },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Text(text = block.content, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                BlockType.TODO -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = block.isChecked,
                            onCheckedChange = { checked ->
                                onUpdateBlock(block.copy(isChecked = checked))
                            }
                        )
                        if (isEditMode) {
                            OutlinedTextField(
                                value = block.content,
                                onValueChange = { onUpdateBlock(block.copy(content = it)) },
                                placeholder = { Text("Текст задачи...") },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Text(
                                text = block.content,
                                style = MaterialTheme.typography.bodyMedium,
                                textDecoration = if (block.isChecked) TextDecoration.LineThrough else null,
                                color = if (block.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                BlockType.TABLE -> {
                    TableBlockView(
                        tableJson = block.tableDataJson,
                        isEditMode = isEditMode,
                        onTableUpdated = { newTableJson ->
                            onUpdateBlock(block.copy(tableDataJson = newTableJson))
                        }
                    )
                }
            }
        }
    }
}

fun getBlockTypeLabel(type: BlockType): String {
    return when (type) {
        BlockType.PARAGRAPH -> "Параграф"
        BlockType.HEADING_1 -> "Заголовок 1"
        BlockType.HEADING_2 -> "Заголовок 2"
        BlockType.HEADING_3 -> "Заголовок 3"
        BlockType.DIVIDER -> "Разделитель"
        BlockType.BULLETED_LIST -> "Маркированный список"
        BlockType.NUMBERED_LIST -> "Нумерованный список"
        BlockType.TODO -> "Задача (Todo)"
        BlockType.TABLE -> "Таблица"
    }
}

// TABLE BLOCK VIEW & EDITOR
@Composable
fun TableBlockView(
    tableJson: String,
    isEditMode: Boolean,
    onTableUpdated: (String) -> Unit
) {
    val table = remember(tableJson) { TableHelper.parseTable(tableJson) }
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Table Action Buttons (when in edit mode)
        if (isEditMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val numCols = if (table.isEmpty()) 2 else table.first().size
                        val newRow = List(numCols) { "" }
                        val updated = table.toMutableList().apply { add(newRow) }
                        onTableUpdated(TableHelper.serializeTable(updated))
                    },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("+ Строка", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        if (table.size > 1) {
                            val updated = table.dropLast(1)
                            onTableUpdated(TableHelper.serializeTable(updated))
                        }
                    },
                    enabled = table.size > 1,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("- Строка", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        val updated = table.map { row -> row + "" }
                        onTableUpdated(TableHelper.serializeTable(updated))
                    },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("+ Колонка", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        val cols = table.firstOrNull()?.size ?: 0
                        if (cols > 1) {
                            val updated = table.map { row -> row.dropLast(1) }
                            onTableUpdated(TableHelper.serializeTable(updated))
                        }
                    },
                    enabled = (table.firstOrNull()?.size ?: 0) > 1,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("- Колонка", fontSize = 11.sp)
                }
            }
        }

        // Table Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
        ) {
            Column {
                table.forEachIndexed { rowIndex, row ->
                    val isHeader = rowIndex == 0
                    Row(
                        modifier = Modifier
                            .background(
                                if (isHeader) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
                            )
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        row.forEachIndexed { colIndex, cellValue ->
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 120.dp)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                    .padding(6.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (isEditMode) {
                                    OutlinedTextField(
                                        value = cellValue,
                                        onValueChange = { newVal ->
                                            val updated = table.mapIndexed { r, rList ->
                                                if (r == rowIndex) {
                                                    rList.mapIndexed { c, cVal ->
                                                        if (c == colIndex) newVal else cVal
                                                    }
                                                } else rList
                                            }
                                            onTableUpdated(TableHelper.serializeTable(updated))
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                } else {
                                    Text(
                                        text = cellValue.ifBlank { "—" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// CONTEXT MENU FOR BLOCK LONG PRESS
@Composable
fun BlockContextMenuDialog(
    block: BlockEntity,
    onDuplicate: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Действия с блоком") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Тип: ${getBlockTypeLabel(block.type)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                DropdownMenuItem(
                    text = { Text("Дублировать") },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                    onClick = onDuplicate
                )
                DropdownMenuItem(
                    text = { Text("Переместить вверх") },
                    leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null) },
                    onClick = onMoveUp
                )
                DropdownMenuItem(
                    text = { Text("Переместить вниз") },
                    leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null) },
                    onClick = onMoveDown
                )
                DropdownMenuItem(
                    text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    onClick = onDelete
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

// ADD BLOCK DIALOG
@Composable
fun AddBlockDialog(
    onAdd: (BlockType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить блок") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                item {
                    AddBlockOption(
                        icon = Icons.Default.Edit,
                        title = "Параграф",
                        subtitle = "Обычный текст",
                        onClick = { onAdd(BlockType.PARAGRAPH) }
                    )
                }
                item {
                    AddBlockOption(
                        icon = Icons.Default.Title,
                        title = "Заголовок 1 (H1)",
                        subtitle = "Крупный заголовок раздела",
                        onClick = { onAdd(BlockType.HEADING_1) }
                    )
                }
                item {
                    AddBlockOption(
                        icon = Icons.Default.Title,
                        title = "Заголовок 2 (H2)",
                        subtitle = "Средний подзаголовок",
                        onClick = { onAdd(BlockType.HEADING_2) }
                    )
                }
                item {
                    AddBlockOption(
                        icon = Icons.Default.Title,
                        title = "Заголовок 3 (H3)",
                        subtitle = "Малый заголовок",
                        onClick = { onAdd(BlockType.HEADING_3) }
                    )
                }
                item {
                    AddBlockOption(
                        icon = Icons.Default.FormatListBulleted,
                        title = "Маркированный список",
                        subtitle = "Список с точками",
                        onClick = { onAdd(BlockType.BULLETED_LIST) }
                    )
                }
                item {
                    AddBlockOption(
                        icon = Icons.Default.FormatListNumbered,
                        title = "Нумерованный список",
                        subtitle = "Список с номерами 1, 2, 3...",
                        onClick = { onAdd(BlockType.NUMBERED_LIST) }
                    )
                }
                item {
                    AddBlockOption(
                        icon = Icons.Default.Check,
                        title = "Задача (Todo)",
                        subtitle = "Список задач с чекбоксами",
                        onClick = { onAdd(BlockType.TODO) }
                    )
                }
                item {
                    AddBlockOption(
                        icon = Icons.Default.TableChart,
                        title = "Таблица",
                        subtitle = "Табличные строки и столбцы",
                        onClick = { onAdd(BlockType.TABLE) }
                    )
                }
                item {
                    AddBlockOption(
                        icon = Icons.Default.HorizontalRule,
                        title = "Разделитель",
                        subtitle = "Горизонтальная линия",
                        onClick = { onAdd(BlockType.DIVIDER) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun AddBlockOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

fun copyAndShare(context: Context, fileName: String, content: String) {
    // Copy to clipboard
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(fileName, content)
    clipboard.setPrimaryClip(clip)

    Toast.makeText(context, "Скопировано в буфер обмена!", Toast.LENGTH_SHORT).show()

    // Trigger Android share sheet
    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_TITLE, fileName)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Экспортировать $fileName")
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        // Share sheet could fail in headless or unsupported sandbox, clipboard copy is already safe
    }
}
