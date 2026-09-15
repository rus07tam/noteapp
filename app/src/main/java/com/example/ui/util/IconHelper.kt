package com.example.ui.util

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object IconHelper {

    val MATERIAL_ICONS = listOf(
        "material:folder" to Icons.Default.Folder,
        "material:description" to Icons.Default.Description,
        "material:article" to Icons.Default.Article,
        "material:star" to Icons.Default.Star,
        "material:bookmark" to Icons.Default.Bookmark,
        "material:work" to Icons.Default.Work,
        "material:home" to Icons.Default.Home,
        "material:code" to Icons.Default.Code,
        "material:edit" to Icons.Default.Edit,
        "material:calendar" to Icons.Default.CalendarMonth,
        "material:label" to Icons.Default.Label,
        "material:school" to Icons.Default.School,
        "material:lightbulb" to Icons.Default.Lightbulb,
        "material:favorite" to Icons.Default.Favorite,
        "material:checkbox" to Icons.Default.CheckBox,
        "material:task" to Icons.Default.TaskAlt,
        "material:build" to Icons.Default.Build,
        "material:flight" to Icons.Default.Flight,
        "material:pets" to Icons.Default.Pets,
        "material:palette" to Icons.Default.Palette,
        "material:cart" to Icons.Default.ShoppingCart,
        "material:flag" to Icons.Default.Flag,
        "material:lock" to Icons.Default.Lock,
        "material:chat" to Icons.Default.Chat,
        "material:image" to Icons.Default.Image,
        "material:music" to Icons.Default.MusicNote,
        "material:science" to Icons.Default.Science,
        "material:dashboard" to Icons.Default.Dashboard
    )

    fun getMaterialVector(key: String): ImageVector? {
        return MATERIAL_ICONS.find { it.first == key }?.second
    }
}

@Composable
fun AppIconView(
    icon: String,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    fontSize: TextUnit = 20.sp
) {
    if (icon.startsWith("material:")) {
        val vector = IconHelper.getMaterialVector(icon) ?: Icons.Default.Folder
        Icon(
            imageVector = vector,
            contentDescription = null,
            tint = tint,
            modifier = modifier.size(size)
        )
    } else {
        Text(
            text = icon.ifBlank { "📝" },
            fontSize = fontSize,
            modifier = modifier
        )
    }
}
