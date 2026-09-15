package com.example.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromBlockType(value: BlockType): String {
        return value.name
    }

    @TypeConverter
    fun toBlockType(value: String): BlockType {
        return try {
            BlockType.valueOf(value)
        } catch (e: Exception) {
            BlockType.PARAGRAPH
        }
    }
}
