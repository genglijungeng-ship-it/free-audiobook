package com.freeaudiobook.app
import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream
/**
 * 书籍解析：支持 txt（多编码）与 epub
 * 章节切分：识别「第X章/回/节/卷」等常见标题
 */
object BookParser {
    data class Chapter(val title: String, val content: String)
    private val chapterRegex = Regex(
        "^\\s*(第[零一二三四五六七八九十百千0-9]+[章回节卷][^\\n]{0,40}|Chapter\\s+\\d+[^\\n]{0,40})\\s*$",
        RegexOption.MULTILINE
    )
    fun readText(context: Context, uri: Uri): String {
        val name = uri.lastPathSegment?.lowercase() ?: ""
        return if (name.endsWith(".epub")) readEpub(context, uri)
        else readTxt(context, uri)
    }
    private fun readTxt(context: Context, uri: Uri): String {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return ""
        for (enc in listOf("UTF-8", "GBK", "GB18030", "UTF-16")) {
            try {
                val s = String(bytes, charset(enc))
                if (!s.contains('\uFFFD')) return s
            } catch (_: Exception) { }
        }
        return String(bytes, Charsets.UTF_8)
    }
    private fun readEpub(context: Context, uri: Uri): String {
        val sb = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val n = entry.name.lowercase()
                    if (n.endsWith(".xhtml") || n.endsWith(".html") || n.endsWith(".htm")) {
                        val html = BufferedReader(InputStreamReader(zip, Charsets.UTF_8)).readText()
                        sb.append(stripHtml(html)).append("\n\n")
                    }
                    entry = zip.nextEntry
                }
            }
        }
        return sb.toString()
    }
    private fun stripHtml(html: String): String =
        html.replace(Regex("<script[\\s\\S]*?</script>"), "")
            .replace(Regex("<style[\\s\\S]*?</style>"), "")
            .replace(Regex("<[^>]+>"), "")
            .replace("&nbsp;", " ")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .trim()
    fun splitChapters(text: String): List<Chapter> {
        val matches = chapterRegex.findAll(text).toList()
        if (matches.isEmpty()) {
            return text.chunked(3000).mapIndexed { i, c -> Chapter("第${i + 1}段", c) }
        }
        val chapters = mutableListOf<Chapter>()
        for (i in matches.indices) {
            val title = matches[i].value.trim()
            val start = matches[i].range.last + 1
            val end = if (i + 1 < matches.size) matches[i + 1].range.first else text.length
            val content = text.substring(start.coerceAtMost(text.length), end.coerceAtMost(text.length)).trim()
            if (content.isNotEmpty()) chapters.add(Chapter(title, content))
        }
        return chapters
    }
}