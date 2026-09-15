package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BlockType
import com.example.data.local.TableHelper
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Заметки", appName)
    }

    @Test
    fun `test table serialization and deserialization`() {
        val original = listOf(
            listOf("Заголовок 1", "Заголовок 2"),
            listOf("Значение 1", "Значение 2")
        )
        val json = TableHelper.serializeTable(original)
        val parsed = TableHelper.parseTable(json)
        assertEquals(2, parsed.size)
        assertEquals("Заголовок 1", parsed[0][0])
        assertEquals("Значение 2", parsed[1][1])
    }

    @Test
    fun `test block types`() {
        val types = BlockType.values()
        assertEquals(9, types.size)
    }
}
