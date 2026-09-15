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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.DocumentEntity
import com.example.ui.viewmodel.NotesViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: NotesViewModel) {
    val activeWorkspace by viewModel.activeWorkspace.collectAsStateWithLifecycle()
    val documents by viewModel.documentsForActiveWorkspace.collectAsStateWithLifecycle()

    var currentCalendarMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDayMillis by remember {
        mutableStateOf(
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        )
    }

    var showQuickCreateDialog by remember { mutableStateOf(false) }

    // Month & Year header string
    val monthYearFormat = remember { SimpleDateFormat("LLLL yyyy", Locale("ru")) }
    val currentMonthTitle = remember(currentCalendarMonth.timeInMillis) {
        monthYearFormat.format(currentCalendarMonth.time).replaceFirstChar { it.uppercase() }
    }

    // Days grid calculation
    val daysInGrid = remember(currentCalendarMonth.timeInMillis) {
        calculateCalendarDays(currentCalendarMonth)
    }

    // Find documents linked to the selected day
    val docsForSelectedDay = remember(selectedDayMillis, documents) {
        documents.filter { doc ->
            doc.calendarDate != null && isSameDay(doc.calendarDate, selectedDayMillis)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Календарь", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "Воркспейс: ${activeWorkspace?.name ?: "—"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            currentCalendarMonth = Calendar.getInstance()
                            selectedDayMillis = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Сегодня", fontSize = 12.sp)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Month navigation header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = {
                            val newCal = currentCalendarMonth.clone() as Calendar
                            newCal.add(Calendar.MONTH, -1)
                            currentCalendarMonth = newCal
                        }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Предыдущий месяц")
                        }

                        Text(
                            text = currentMonthTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(onClick = {
                            val newCal = currentCalendarMonth.clone() as Calendar
                            newCal.add(Calendar.MONTH, 1)
                            currentCalendarMonth = newCal
                        }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Следующий месяц")
                        }
                    }
                }
            }

            // Calendar Grid Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        // Days of Week Header
                        val weekdays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                        Row(modifier = Modifier.fillMaxWidth()) {
                            weekdays.forEach { dayName ->
                                Text(
                                    text = dayName,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Weeks
                        val weeks = daysInGrid.chunked(7)
                        val today = Calendar.getInstance()

                        weeks.forEach { week ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                week.forEach { dayInfo ->
                                    val isSelected = isSameDay(dayInfo.calendar.timeInMillis, selectedDayMillis)
                                    val isToday = isSameDay(dayInfo.calendar.timeInMillis, today.timeInMillis)

                                    // Linked documents for this cell
                                    val cellDocs = documents.filter { doc ->
                                        doc.calendarDate != null && isSameDay(doc.calendarDate, dayInfo.calendar.timeInMillis)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                                    isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else if (isToday) 1.dp else 0.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else if (isToday) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                selectedDayMillis = dayInfo.calendar.timeInMillis
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "${dayInfo.calendar.get(Calendar.DAY_OF_MONTH)}",
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    !dayInfo.isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    isToday -> MaterialTheme.colorScheme.primary
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )

                                            // Document indicators (color marker dots)
                                            if (cellDocs.isNotEmpty()) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    cellDocs.take(3).forEach { doc ->
                                                        val color = parseColorHex(doc.colorHex)
                                                        Box(
                                                            modifier = Modifier
                                                                .size(5.dp)
                                                                .clip(CircleShape)
                                                                .background(color)
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
                }
            }

            // LINKED DOCUMENTS DROPDOWN / POPUP CARD
            item {
                val selectedDateFormatted = viewModel.formatDate(selectedDayMillis)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Заметки на $selectedDateFormatted",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (docsForSelectedDay.isNotEmpty()) {
                                        "Связано файлов: ${docsForSelectedDay.size}"
                                    } else {
                                        "Нет связанных заметок"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { showQuickCreateDialog = true },
                                modifier = Modifier.height(34.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Создать", fontSize = 12.sp)
                            }
                        }

                        if (docsForSelectedDay.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                docsForSelectedDay.forEach { doc ->
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
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, markerColor.copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(doc.icon, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = doc.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (doc.statusTag.isNotBlank()) {
                                                    Text(
                                                        text = doc.statusTag,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = markerColor
                                                    )
                                                }
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(markerColor)
                                            )
                                        }
                                    }
                                }
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

    if (showQuickCreateDialog) {
        CreateDocumentDialog(
            folderId = null,
            onConfirm = { title, icon, colorHex ->
                viewModel.createDocument(
                    folderId = null,
                    title = title,
                    icon = icon,
                    colorHex = colorHex,
                    calendarDate = selectedDayMillis
                )
                showQuickCreateDialog = false
            },
            onDismiss = { showQuickCreateDialog = false }
        )
    }
}

data class CalendarDayInfo(
    val calendar: Calendar,
    val isCurrentMonth: Boolean
)

fun calculateCalendarDays(monthCal: Calendar): List<CalendarDayInfo> {
    val days = mutableListOf<CalendarDayInfo>()
    val cal = monthCal.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)

    // Monday is 1st day of week
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val prefixDays = (firstDayOfWeek - Calendar.MONDAY + 7) % 7

    cal.add(Calendar.DAY_OF_MONTH, -prefixDays)

    val currentMonth = monthCal.get(Calendar.MONTH)
    // 42 days (6 weeks)
    for (i in 0 until 42) {
        days.add(
            CalendarDayInfo(
                calendar = cal.clone() as Calendar,
                isCurrentMonth = cal.get(Calendar.MONTH) == currentMonth
            )
        )
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return days
}

fun isSameDay(millis1: Long, millis2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
