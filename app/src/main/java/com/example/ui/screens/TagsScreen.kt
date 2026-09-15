package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagsScreen(viewModel: NotesViewModel) {
    val activeWorkspace by viewModel.activeWorkspace.collectAsStateWithLifecycle()
    val documents by viewModel.documentsForActiveWorkspace.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    // Aggregate tags with counts
    val allTagsWithCount = remember(documents) {
        val map = mutableMapOf<String, Int>()
        documents.forEach { doc ->
            val tags = doc.tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            tags.forEach { tag ->
                map[tag] = (map[tag] ?: 0) + 1
            }
        }
        map.toList().sortedByDescending { it.second }
    }

    val filteredTags = remember(allTagsWithCount, searchQuery) {
        if (searchQuery.isBlank()) allTagsWithCount
        else allTagsWithCount.filter { it.first.contains(searchQuery, ignoreCase = true) }
    }

    // Filter documents matching selected tag
    val matchingDocs = remember(documents, selectedTag) {
        if (selectedTag == null) emptyList()
        else {
            documents.filter { doc ->
                val tags = doc.tagsCsv.split(",").map { it.trim() }
                tags.contains(selectedTag)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Теги", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "Поиск файлов по тегам воркспейса '${activeWorkspace?.name ?: ""}'",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск среди тегов...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Очистить")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            if (filteredTags.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏷️", fontSize = 44.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Теги не найдены",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Добавьте теги в метаданных заметок, чтобы группировать файлы.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        "Все теги (${filteredTags.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredTags.forEach { (tag, count) ->
                            val isSelected = tag == selectedTag
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) viewModel.setSelectedTag(null)
                                    else viewModel.setSelectedTag(tag)
                                },
                                label = {
                                    Text("#$tag ($count)")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Label, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            )
                        }
                    }
                }

                // Matching Documents Section
                if (selectedTag != null) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Файлы с тегом '#$selectedTag' (${matchingDocs.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { viewModel.setSelectedTag(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Сбросить фильтр", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    items(matchingDocs, key = { it.id }) { doc ->
                        val markerColor = parseColorHex(doc.colorHex)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.openDocumentInEditor(doc.id)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, markerColor.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(doc.icon, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = doc.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        if (doc.statusTag.isNotBlank()) {
                                            Text(
                                                text = doc.statusTag,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = markerColor
                                            )
                                        }
                                        Text(
                                            text = viewModel.formatDate(doc.updatedAt),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(markerColor)
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
}
