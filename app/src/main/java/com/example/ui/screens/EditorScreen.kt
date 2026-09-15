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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatIndentDecrease
import androidx.compose.material.icons.automirrored.filled.FormatIndentIncrease
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.BlockEntity
import com.example.data.local.BlockType
import com.example.data.local.DocumentEntity
import com.example.data.local.TableHelper
import com.example.ui.util.AppIconView
import com.example.ui.util.RichTextHelper
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.NotesViewModel

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
                    title = { Text(stringResource(R.string.editor_title)) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.FILES) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                    Text(
                        stringResource(R.string.select_note_prompt),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.navigateTo(AppScreen.FILES) }) {
                        Text(stringResource(R.string.go_to_files))
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
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.FILES) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                if (isEditMode) showEditTitleDialog = true
                            }
                        ) {
                            // Icon picker trigger
                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { if (isEditMode) showIconPicker = true },
                                color = markerColor.copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    AppIconView(icon = doc.icon, size = 20.dp, fontSize = 20.sp, tint = markerColor)
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = doc.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isEditMode) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = if (isEditMode) stringResource(R.string.editing_mode) else stringResource(R.string.reading_mode),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Color Marker Dot
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(markerColor)
                                    .clickable { if (isEditMode) showColorPicker = true }
                            )
                        }
                    },
                    actions = {
                        // Search in document
                        IconButton(onClick = { viewModel.toggleSearch() }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_in_document))
                        }

                        // Toggle Read/Edit mode
                        IconButton(onClick = { viewModel.toggleEditMode() }) {
                            Icon(
                                imageVector = if (isEditMode) Icons.Default.MenuBook else Icons.Default.Edit,
                                contentDescription = if (isEditMode) stringResource(R.string.reading_mode) else stringResource(R.string.editing_mode),
                                tint = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Export dropdown
                        Box {
                            IconButton(onClick = { exportMenuExpanded = true }) {
                                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.export_title))
                            }

                            DropdownMenu(
                                expanded = exportMenuExpanded,
                                onDismissRequest = { exportMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.export_markdown)) },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                    onClick = {
                                        exportMenuExpanded = false
                                        val md = viewModel.getMarkdownExport()
                                        copyAndShare(context, "${doc.title}.md", md)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.export_plain_text)) },
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
                            placeholder = { Text(stringResource(R.string.search_in_document)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    Text(
                                        text = "$matchCount",
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
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_block))
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
                    onRemoveTag = { tag -> viewModel.removeTagFromDocument(tag) },
                    onTagClicked = { tag ->
                        viewModel.setSelectedTag(tag)
                        viewModel.navigateTo(AppScreen.TAGS)
                    }
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
                    onMoveDown = { viewModel.moveBlockDown(block) },
                    onInsertAfter = { type, indent ->
                        viewModel.insertBlockAfter(block, type, "", indent)
                    },
                    onChangeIndent = { delta ->
                        viewModel.changeBlockIndent(block, delta)
                    }
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
                            stringResource(R.string.empty_document_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }

    // Context Menu Dialog on Block Long Press (ONLY ACCESSIBLE IN EDIT MODE)
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

    // Add Block Dialog
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
            title = stringResource(R.string.edit_note_title),
            initialValue = doc.title,
            label = stringResource(R.string.note_title),
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
            title = stringResource(R.string.status_tag),
            initialValue = doc.statusTag,
            label = stringResource(R.string.status_tag_hint),
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
            title = stringResource(R.string.add_tag_title),
            initialValue = "",
            label = stringResource(R.string.tag_name_label),
            onConfirm = { newTag ->
                viewModel.addTagToDocument(newTag)
                showAddTagDialog = false
            },
            onDismiss = { showAddTagDialog = false }
        )
    }

    // Color Picker Dialog
    if (showColorPicker) {
        ColorPickerDialog(
            currentColorHex = doc.colorHex,
            onColorSelected = { newHex ->
                viewModel.updateDocumentColor(newHex)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    // Icon Picker Dialog
    if (showIconPicker) {
        IconPickerDialog(
            currentIcon = doc.icon,
            onIconSelected = { newIcon ->
                viewModel.updateDocumentIcon(newIcon)
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }
}

// DOCUMENT METADATA TABLE COMPONENT
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
    onRemoveTag: (String) -> Unit,
    onTagClicked: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Creation & Modification dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetadataCell(
                    label = stringResource(R.string.created_label),
                    value = formatDate(document.createdAt),
                    modifier = Modifier.weight(1f)
                )
                MetadataCell(
                    label = stringResource(R.string.updated_label),
                    value = formatDate(document.updatedAt),
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Row 2: Calendar Date - CANNOT BE MODIFIED/CLEARED IN READ MODE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = isEditMode) { onPickCalendarDate() }
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
                    stringResource(R.string.calendar_date_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (document.calendarDate != null) formatDate(document.calendarDate) else if (isEditMode) stringResource(R.string.not_set_click_to_pick) else stringResource(R.string.not_set),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (document.calendarDate != null) markerColor else MaterialTheme.colorScheme.primary
                )

                // Date clearing button only shown in EDIT mode
                if (document.calendarDate != null && isEditMode) {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onClearCalendarDate,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_date), modifier = Modifier.size(16.dp))
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Row 3: Status Tag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = isEditMode) { onEditStatus() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.status_label),
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
                        text = if (document.statusTag.isNotBlank()) document.statusTag else if (isEditMode) stringResource(R.string.set_status) else "—",
                        style = MaterialTheme.typography.labelSmall,
                        color = markerColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                if (isEditMode) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_status),
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Row 4: General Tags
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.general_tags_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isEditMode) {
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = onAddTag, modifier = Modifier.height(28.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(stringResource(R.string.add), fontSize = 11.sp)
                        }
                    }
                }

                val tags = document.tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                if (tags.isEmpty()) {
                    Text(
                        stringResource(R.string.no_tags),
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
                                onClick = {
                                    if (isEditMode) {
                                        onRemoveTag(tag)
                                    } else {
                                        // Navigate to Tags screen with filter
                                        onTagClicked(tag)
                                    }
                                },
                                label = { Text("#$tag", fontSize = 12.sp) },
                                trailingIcon = if (isEditMode) {
                                    {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = stringResource(R.string.delete_tag),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                } else null
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

// STABLE TEXT FIELD WITH LOCAL SELECTION AND RICH TEXT FORMATTING
@Composable
fun RichBlockTextField(
    initialText: String,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    textStyle: TextStyle = LocalTextStyle.current,
    singleLine: Boolean = false,
    onEnterOrNext: (() -> Unit)? = null
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length)))
    }
    var isFocused by remember { mutableStateOf(false) }

    // Sync only if external text really changed from elsewhere
    LaunchedEffect(initialText) {
        if (initialText != textFieldValue.text) {
            val newSel = if (textFieldValue.selection.end <= initialText.length) {
                textFieldValue.selection
            } else {
                TextRange(initialText.length)
            }
            textFieldValue = textFieldValue.copy(text = initialText, selection = newSel)
        }
    }

    Column(modifier = modifier) {
        // Rich text formatting toolbar when focused
        if (isFocused) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val updated = RichTextHelper.toggleTag(textFieldValue, "**")
                        textFieldValue = updated
                        onTextChanged(updated.text)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.FormatBold, contentDescription = "Bold", modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = {
                        val updated = RichTextHelper.toggleTag(textFieldValue, "*")
                        textFieldValue = updated
                        onTextChanged(updated.text)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.FormatItalic, contentDescription = "Italic", modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = {
                        val updated = RichTextHelper.toggleTag(textFieldValue, "~~")
                        textFieldValue = updated
                        onTextChanged(updated.text)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.FormatStrikethrough, contentDescription = "Strike", modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = {
                        val updated = RichTextHelper.toggleUnderline(textFieldValue)
                        textFieldValue = updated
                        onTextChanged(updated.text)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.FormatUnderlined, contentDescription = "Underline", modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = {
                        val updated = RichTextHelper.toggleTag(textFieldValue, "`")
                        textFieldValue = updated
                        onTextChanged(updated.text)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Code, contentDescription = "Code", modifier = Modifier.size(16.dp))
                }
            }
        }

        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                onTextChanged(newValue.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            placeholder = { Text(placeholder) },
            textStyle = textStyle,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(
                imeAction = if (onEnterOrNext != null) ImeAction.Next else ImeAction.Default
            ),
            keyboardActions = KeyboardActions(
                onNext = { onEnterOrNext?.invoke() },
                onDone = { onEnterOrNext?.invoke() }
            )
        )
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
    onMoveDown: () -> Unit,
    onInsertAfter: (BlockType, Int) -> Unit,
    onChangeIndent: (Int) -> Unit
) {
    val highlightBorder = if (isSearchMatch) {
        androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else null

    // Card modifier: long click ONLY active in edit mode!
    val cardModifier = if (isEditMode) {
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* normal click */ },
                onLongClick = { onOpenContextMenu() }
            )
    } else {
        Modifier.fillMaxWidth()
    }

    val indentPadding = (block.indentLevel * 20).dp

    Card(
        modifier = cardModifier.padding(start = indentPadding),
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
                            contentDescription = stringResource(R.string.move_item),
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

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Indent controls for list blocks
                        if (block.type == BlockType.BULLETED_LIST || block.type == BlockType.NUMBERED_LIST || block.type == BlockType.TODO) {
                            IconButton(
                                onClick = { onChangeIndent(-1) },
                                enabled = block.indentLevel > 0,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.FormatIndentDecrease, contentDescription = stringResource(R.string.decrease_indent), modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { onChangeIndent(1) },
                                enabled = block.indentLevel < 3,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.FormatIndentIncrease, contentDescription = stringResource(R.string.increase_indent), modifier = Modifier.size(16.dp))
                            }
                        }

                        IconButton(onClick = onMoveUp, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.move_up), modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onMoveDown, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.move_down), modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onOpenContextMenu, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.block_actions), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Render block content by Type
            when (block.type) {
                BlockType.PARAGRAPH -> {
                    if (isEditMode) {
                        RichBlockTextField(
                            initialText = block.content,
                            onTextChanged = { onUpdateBlock(block.copy(content = it)) },
                            placeholder = stringResource(R.string.paragraph_hint)
                        )
                    } else {
                        Text(
                            text = RichTextHelper.parseRichText(block.content.ifBlank { " " }, MaterialTheme.colorScheme.primary),
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp
                        )
                    }
                }

                BlockType.HEADING_1 -> {
                    if (isEditMode) {
                        RichBlockTextField(
                            initialText = block.content,
                            onTextChanged = { onUpdateBlock(block.copy(content = it)) },
                            placeholder = stringResource(R.string.heading1_hint),
                            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            singleLine = true
                        )
                    } else {
                        Text(
                            text = RichTextHelper.parseRichText(block.content, MaterialTheme.colorScheme.primary),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                }

                BlockType.HEADING_2 -> {
                    if (isEditMode) {
                        RichBlockTextField(
                            initialText = block.content,
                            onTextChanged = { onUpdateBlock(block.copy(content = it)) },
                            placeholder = stringResource(R.string.heading2_hint),
                            textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            singleLine = true
                        )
                    } else {
                        Text(
                            text = RichTextHelper.parseRichText(block.content, MaterialTheme.colorScheme.primary),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                        )
                    }
                }

                BlockType.HEADING_3 -> {
                    if (isEditMode) {
                        RichBlockTextField(
                            initialText = block.content,
                            onTextChanged = { onUpdateBlock(block.copy(content = it)) },
                            placeholder = stringResource(R.string.heading3_hint),
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            singleLine = true
                        )
                    } else {
                        Text(
                            text = RichTextHelper.parseRichText(block.content, MaterialTheme.colorScheme.primary),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }
                }

                BlockType.DIVIDER -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp
                    )
                }

                BlockType.BULLETED_LIST -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "•",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (isEditMode) {
                                RichBlockTextField(
                                    initialText = block.content,
                                    onTextChanged = { onUpdateBlock(block.copy(content = it)) },
                                    placeholder = stringResource(R.string.list_item_hint),
                                    modifier = Modifier.weight(1f),
                                    onEnterOrNext = { onInsertAfter(BlockType.BULLETED_LIST, block.indentLevel) }
                                )
                            } else {
                                Text(
                                    text = RichTextHelper.parseRichText(block.content, MaterialTheme.colorScheme.primary),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        if (isEditMode) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 28.dp, top = 2.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                TextButton(
                                    onClick = { onInsertAfter(BlockType.BULLETED_LIST, block.indentLevel) },
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.next_item), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                BlockType.NUMBERED_LIST -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${block.orderIndex + 1}.",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 6.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (isEditMode) {
                                RichBlockTextField(
                                    initialText = block.content,
                                    onTextChanged = { onUpdateBlock(block.copy(content = it)) },
                                    placeholder = stringResource(R.string.list_item_hint),
                                    modifier = Modifier.weight(1f),
                                    onEnterOrNext = { onInsertAfter(BlockType.NUMBERED_LIST, block.indentLevel) }
                                )
                            } else {
                                Text(
                                    text = RichTextHelper.parseRichText(block.content, MaterialTheme.colorScheme.primary),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        if (isEditMode) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 28.dp, top = 2.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                TextButton(
                                    onClick = { onInsertAfter(BlockType.NUMBERED_LIST, block.indentLevel) },
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.next_item), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                BlockType.TODO -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
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
                                RichBlockTextField(
                                    initialText = block.content,
                                    onTextChanged = { onUpdateBlock(block.copy(content = it)) },
                                    placeholder = stringResource(R.string.todo_item_hint),
                                    modifier = Modifier.weight(1f),
                                    onEnterOrNext = { onInsertAfter(BlockType.TODO, block.indentLevel) }
                                )
                            } else {
                                Text(
                                    text = RichTextHelper.parseRichText(block.content, MaterialTheme.colorScheme.primary),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textDecoration = if (block.isChecked) TextDecoration.LineThrough else null,
                                    color = if (block.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (isEditMode) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 36.dp, top = 2.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                TextButton(
                                    onClick = { onInsertAfter(BlockType.TODO, block.indentLevel) },
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.next_todo), fontSize = 11.sp)
                                }
                            }
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

@Composable
fun getBlockTypeLabel(type: BlockType): String {
    return when (type) {
        BlockType.PARAGRAPH -> stringResource(R.string.block_paragraph)
        BlockType.HEADING_1 -> stringResource(R.string.block_heading1)
        BlockType.HEADING_2 -> stringResource(R.string.block_heading2)
        BlockType.HEADING_3 -> stringResource(R.string.block_heading3)
        BlockType.DIVIDER -> stringResource(R.string.block_divider)
        BlockType.BULLETED_LIST -> stringResource(R.string.block_bullet_list)
        BlockType.NUMBERED_LIST -> stringResource(R.string.block_number_list)
        BlockType.TODO -> stringResource(R.string.block_todo)
        BlockType.TABLE -> stringResource(R.string.block_table)
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
                    Text(stringResource(R.string.add_row), fontSize = 11.sp)
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
                    Text(stringResource(R.string.remove_row), fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        val updated = table.map { row -> row + "" }
                        onTableUpdated(TableHelper.serializeTable(updated))
                    },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(stringResource(R.string.add_col), fontSize = 11.sp)
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
                    Text(stringResource(R.string.remove_col), fontSize = 11.sp)
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
                                        text = RichTextHelper.parseRichText(cellValue.ifBlank { "—" }, MaterialTheme.colorScheme.primary),
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
        title = { Text(stringResource(R.string.block_actions)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${stringResource(R.string.block_type_prefix)} ${getBlockTypeLabel(block.type)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                DropdownMenuItem(
                    text = { Text(stringResource(R.string.duplicate)) },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                    onClick = onDuplicate
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.move_up)) },
                    leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null) },
                    onClick = onMoveUp
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.move_down)) },
                    leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null) },
                    onClick = onMoveDown
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    onClick = onDelete
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
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
        title = { Text(stringResource(R.string.add_block)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                item {
                    AddBlockOption(
                        icon = Icons.Default.Edit,
                        title = stringResource(R.string.block_paragraph),
                        subtitle = stringResource(R.string.block_paragraph_desc),
                        onClick = { onAdd(BlockType.PARAGRAPH) }
                    )
                }
                item {
                    AddBlockOption(
                        icon = Icons.Default.Title,
                        title = stringResource(R.string.block_heading1),
                        subtitle = stringResource(R.string.block_heading1_desc),
                        onClick = { onAdd(BlockType.HEADING_1) }
                    )
                }
                item {
                    AddBlockOption(
                        icon = Icons.Default.Title,
                        title = stringResource(R.string.block_heading2),
                        subtitle = stringResource(R.string.block_heading2_desc),
                        onClick = { onAdd(BlockType.HEADING_2) }
                    )
                }
                item {
                    AddBlockOption(
                        icon = Icons.Default.Title,
                        title = stringResource(R.string.block_heading3),
                        subtitle = stringResource(R.string.block_heading3_desc),
                        onClick = { onAdd(BlockType.HEADING_3) }
                    )
                }
                item {
                    AddBlockOption(
                        icon = Icons.Default.FormatListBulleted,
                        title = stringResource(R.string.block_bullet_list),
                        subtitle = stringResource(R.string.block_bullet_list_desc),
                        onClick = { onAdd(BlockType.BULLETED_LIST) }
                    )
                }
                item {
                    AddBlockOption(
                        icon = Icons.Default.FormatListNumbered,
                        title = stringResource(R.string.block_number_list),
                        subtitle = stringResource(R.string.block_number_list_desc),
                        onClick = { onAdd(BlockType.NUMBERED_LIST) }
                    )
                }
                item {
                    AddBlockOption(
                        icon = Icons.Default.Check,
                        title = stringResource(R.string.block_todo),
                        subtitle = stringResource(R.string.block_todo_desc),
                        onClick = { onAdd(BlockType.TODO) }
                    )
                }
                item {
                    AddBlockOption(
                        icon = Icons.Default.TableChart,
                        title = stringResource(R.string.block_table),
                        subtitle = stringResource(R.string.block_table_desc),
                        onClick = { onAdd(BlockType.TABLE) }
                    )
                }
                item {
                    AddBlockOption(
                        icon = Icons.Default.HorizontalRule,
                        title = stringResource(R.string.block_divider),
                        subtitle = stringResource(R.string.block_divider_desc),
                        onClick = { onAdd(BlockType.DIVIDER) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
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
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(fileName, content)
    clipboard.setPrimaryClip(clip)

    Toast.makeText(context, "Скопировано в буфер обмена!", Toast.LENGTH_SHORT).show()

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
        // clipboard copy is already safe
    }
}
