package com.example.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import java.util.regex.Pattern

object RichTextHelper {

    /**
     * Parses markdown-like rich text markup into Compose AnnotatedString:
     * - **bold**
     * - *italic*
     * - ~~strikethrough~~
     * - <u>underline</u>
     * - `code`
     */
    fun parseRichText(raw: String, primaryColor: Color): AnnotatedString {
        if (raw.isEmpty()) return AnnotatedString("")

        // Tokenize and build styled spans
        val regex = Pattern.compile(
            "(\\*\\*(.+?)\\*\\*)|" +        // 1: **bold** (group 2)
            "(\\*(.+?)\\*)|" +              // 3: *italic* (group 4)
            "(~~(.+?)~~)|" +                // 5: ~~strike~~ (group 6)
            "(<u>(.+?)</u>)|" +             // 7: <u>underline</u> (group 8)
            "(`(.+?)`)"                     // 9: `code` (group 10)
        )

        val matcher = regex.matcher(raw)
        val builder = AnnotatedString.Builder()
        var lastIndex = 0

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()

            // Append plain text before match
            if (start > lastIndex) {
                builder.append(raw.substring(lastIndex, start))
            }

            when {
                // Bold: **text**
                matcher.group(2) != null -> {
                    val content = matcher.group(2)!!
                    val s = builder.length
                    builder.append(content)
                    builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), s, builder.length)
                }
                // Italic: *text*
                matcher.group(4) != null -> {
                    val content = matcher.group(4)!!
                    val s = builder.length
                    builder.append(content)
                    builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), s, builder.length)
                }
                // Strikethrough: ~~text~~
                matcher.group(6) != null -> {
                    val content = matcher.group(6)!!
                    val s = builder.length
                    builder.append(content)
                    builder.addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), s, builder.length)
                }
                // Underline: <u>text</u>
                matcher.group(8) != null -> {
                    val content = matcher.group(8)!!
                    val s = builder.length
                    builder.append(content)
                    builder.addStyle(SpanStyle(textDecoration = TextDecoration.Underline), s, builder.length)
                }
                // Code: `text`
                matcher.group(10) != null -> {
                    val content = matcher.group(10)!!
                    val s = builder.length
                    builder.append(content)
                    builder.addStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = primaryColor.copy(alpha = 0.12f),
                            color = primaryColor
                        ),
                        s,
                        builder.length
                    )
                }
            }
            lastIndex = end
        }

        if (lastIndex < raw.length) {
            builder.append(raw.substring(lastIndex))
        }

        return builder.toAnnotatedString()
    }

    /**
     * Applies or removes a formatting tag on the selected text of a TextFieldValue.
     */
    fun toggleTag(
        tfv: TextFieldValue,
        prefix: String,
        suffix: String = prefix
    ): TextFieldValue {
        val text = tfv.text
        val sel = tfv.selection

        val start = sel.min
        val end = sel.max

        if (start == end) {
            // No selection: insert tags with cursor in the middle
            val newText = text.substring(0, start) + prefix + suffix + text.substring(end)
            val newCursor = start + prefix.length
            return TextFieldValue(newText, TextRange(newCursor))
        }

        val selected = text.substring(start, end)
        val hasPrefix = selected.startsWith(prefix)
        val hasSuffix = selected.endsWith(suffix)

        return if (hasPrefix && hasSuffix && selected.length >= prefix.length + suffix.length) {
            // Unwrap
            val unwrapped = selected.substring(prefix.length, selected.length - suffix.length)
            val newText = text.substring(0, start) + unwrapped + text.substring(end)
            TextFieldValue(newText, TextRange(start, start + unwrapped.length))
        } else {
            // Wrap
            val wrapped = "$prefix$selected$suffix"
            val newText = text.substring(0, start) + wrapped + text.substring(end)
            TextFieldValue(newText, TextRange(start, start + wrapped.length))
        }
    }

    fun toggleUnderline(tfv: TextFieldValue): TextFieldValue = toggleTag(tfv, "<u>", "</u>")
}
