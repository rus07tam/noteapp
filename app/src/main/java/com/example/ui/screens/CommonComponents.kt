package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

val APP_COLOR_PALETTE = listOf(
    "#3B82F6" to "Синий",
    "#10B981" to "Изумрудный",
    "#F59E0B" to "Янтарный",
    "#EF4444" to "Красный",
    "#8B5CF6" to "Фиолетовый",
    "#EC4899" to "Розовый",
    "#06B6D4" to "Морской",
    "#14B8A6" to "Бирюзовый",
    "#F97316" to "Оранжевый",
    "#64748B" to "Графитовый"
)

val APP_ICONS_LIST = listOf(
    "📝", "📄", "📁", "📂", "💡", "🎯", "🚀", "📚",
    "💼", "☕", "⚡", "📌", "🔬", "🏷️", "📅", "🎨",
    "⭐", "🔥", "📋", "🛠️", "🧩", "🌟", "✨", "📊"
)

fun parseColorHex(hex: String, default: Color = Color(0xFF3B82F6)): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = if (cleanHex.length == 6) {
            android.graphics.Color.parseColor("#FF$cleanHex")
        } else {
            android.graphics.Color.parseColor("#$cleanHex")
        }
        Color(colorInt)
    } catch (e: Exception) {
        default
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    currentColorHex: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите цвет маркера") },
        text = {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                APP_COLOR_PALETTE.forEach { (hex, name) ->
                    val color = parseColorHex(hex)
                    val isSelected = hex.equals(currentColorHex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                onColorSelected(hex)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Выбран",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IconPickerDialog(
    currentIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите иконку") },
        text = {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                APP_ICONS_LIST.forEach { iconEmoji ->
                    val isSelected = iconEmoji == currentIcon
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onIconSelected(iconEmoji)
                                onDismiss()
                            },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = iconEmoji, fontSize = 22.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun SimpleInputDialog(
    title: String,
    initialValue: String = "",
    label: String = "Название",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(label) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onConfirm(text.trim())
                        onDismiss()
                    }
                },
                enabled = text.isNotBlank()
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
}

fun showDatePicker(
    context: android.content.Context,
    initialMillis: Long?,
    onDateSelected: (Long) -> Unit
) {
    val cal = Calendar.getInstance()
    if (initialMillis != null && initialMillis > 0) {
        cal.timeInMillis = initialMillis
    }
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selected = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onDateSelected(selected.timeInMillis)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}
