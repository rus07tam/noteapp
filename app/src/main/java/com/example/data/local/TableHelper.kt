package com.example.data.local

import org.json.JSONArray
import org.json.JSONObject

object TableHelper {
    fun parseTable(jsonStr: String): List<List<String>> {
        if (jsonStr.isBlank()) {
            return listOf(
                listOf("Колонка 1", "Колонка 2"),
                listOf("Значение 1", "Значение 2")
            )
        }
        return try {
            val jsonArray = JSONArray(jsonStr)
            val result = mutableListOf<List<String>>()
            for (i in 0 until jsonArray.length()) {
                val rowArray = jsonArray.getJSONArray(i)
                val row = mutableListOf<String>()
                for (j in 0 until rowArray.length()) {
                    row.add(rowArray.optString(j, ""))
                }
                result.add(row)
            }
            if (result.isEmpty()) {
                listOf(listOf("Колонка 1", "Колонка 2"), listOf("", ""))
            } else {
                result
            }
        } catch (e: Exception) {
            listOf(listOf("Колонка 1", "Колонка 2"), listOf("Ошибка данных", ""))
        }
    }

    fun serializeTable(table: List<List<String>>): String {
        val jsonArray = JSONArray()
        for (row in table) {
            val rowArray = JSONArray()
            for (cell in row) {
                rowArray.put(cell)
            }
            jsonArray.put(rowArray)
        }
        return jsonArray.toString()
    }
}
