package com.freeaudiobook.app
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
/**
 * 主界面：超大字号极简版
 * 功能：导入小说 / 书库 / 朗读 / 上一章下一章 / 语速 / 定时关闭
 */
class MainActivity : AppCompatActivity() {
    private lateinit var tvBookInfo: TextView
    private lateinit var tvContent: TextView
    private lateinit var btnPlay: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnImport: Button
    private lateinit var btnFontBigger: Button
    private lateinit var btnFontSmaller: Button
    private lateinit var btnLibrary: Button
    private lateinit var btnTimer: Button
    private lateinit var etTimer: EditText
    private lateinit var seekRate: SeekBar
    private lateinit var tts: TtsManager
    private var chapters: List<BookParser.Chapter> = emptyList()
    private var curChapter = 0
    private var bookTitle = ""
    private var fontSize = 30f
    private var countDown: CountDownTimer? = null
    private val pickFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val text = BookParser.readText(this, uri)
                if (text.isBlank()) {
                    toast("读取失败或文件为空")
                    return@let
                }
                // 复制到书库目录
                val name = uri.lastPathSegment?.substringAfterLast('/') ?: "book.txt"
                try {
                    val dst = File(filesDir, name)
                    contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(dst).use { out -> input.copyTo(out) }
                    }
                } catch (_: Exception) { }
                loadBook(text, name)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvBookInfo = findViewById(R.id.tvBookInfo)
        tvContent = findViewById(R.id.tvContent)
        btnPlay = findViewById(R.id.btnPlay)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        btnImport = findViewById(R.id.btnImport)
        btnFontBigger = findViewById(R.id.btnFontBigger)
        btnFontSmaller = findViewById(R.id.btnFontSmaller)
        btnLibrary = findViewById(R.id.btnLibrary)
        btnTimer = findViewById(R.id.btnTimer)
        etTimer = findViewById(R.id.etTimer)
        seekRate = findViewById(R.id.seekRate)
        tts = TtsManager(this)
        tts.onSentenceStart = { idx -> runOnUiThread { highlightSentence(idx) } }
        tts.onChapterFinished = { runOnUiThread { nextChapter(auto = true) } }
        btnImport.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            pickFile.launch(intent)
        }
        btnPlay.setOnClickListener { togglePlay() }
        btnPrev.setOnClickListener { prevChapter() }
        btnNext.setOnClickListener { nextChapter(auto = false) }
        btnFontBigger.setOnClickListener { changeFont(4f) }
        btnFontSmaller.setOnClickListener { changeFont(-4f) }
        btnLibrary.setOnClickListener { openLibrary() }
        btnTimer.setOnClickListener { setTimer() }
        seekRate.max = 100
        seekRate.progress = 50
        seekRate.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                // 0.5x ~ 2.0x
                val rate = 0.5f + progress / 100f * 1.5f
                tts.setRate(rate)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }
    private fun loadBook(text: String, name: String) {
        tts.stop()
        chapters = BookParser.splitChapters(text)
        bookTitle = name
        curChapter = 0
        showChapter()
        toast("已打开《$name》，共 ${chapters.size} 章")
    }
    private fun showChapter() {
        if (chapters.isEmpty()) return
        val ch = chapters[curChapter]
        tvBookInfo.text = "当前书籍：$bookTitle  |  ${ch.title}  (${curChapter + 1}/${chapters.size})"
        tvContent.text = ch.title + "\n\n" + ch.content
        tts.loadChapter(ch.content)
    }
    private fun togglePlay() {
        if (chapters.isEmpty()) {
            toast("请先点击「导入小说」添加一本书")
            return
        }
        if (tts.isPlaying()) {
            tts.pause()
            btnPlay.text = "▶ 开始朗读"
        } else {
            tts.start()
            btnPlay.text = "⏸ 暂停朗读"
        }
    }
    private fun prevChapter() {
        if (chapters.isEmpty()) return
        tts.stop()
        btnPlay.text = "▶ 开始朗读"
        if (curChapter > 0) { curChapter--; showChapter() }
    }
    private fun nextChapter(auto: Boolean) {
        if (chapters.isEmpty()) return
        tts.stop()
        if (curChapter < chapters.size - 1) {
            curChapter++
            showChapter()
            if (auto) { tts.start(); btnPlay.text = "⏸ 暂停朗读" }
        } else {
            btnPlay.text = "▶ 开始朗读"
            toast("已读完最后一章 🎉")
        }
    }
    private fun changeFont(delta: Float) {
        fontSize = (fontSize + delta).coerceIn(20f, 60f)
        tvContent.textSize = fontSize
        tvBookInfo.textSize = fontSize * 0.75f
    }
    private fun highlightSentence(idx: Int) {
        // 简单提示当前朗读句序号（可扩展为高亮）
        val ch = chapters.getOrNull(curChapter) ?: return
        val sentences = ch.content.split(Regex("(?<=[。！？!?；;\\n])")).filter { it.trim().length >= 2 }
        if (idx < sentences.size) {
            // 滚动到大致位置
            tvContent.post {
                val ratio = idx.toFloat() / sentences.size.coerceAtLeast(1)
                val scroll = (tvContent.layout?.height ?: 0) * ratio
                tvContent.scrollTo(0, scroll.toInt())
            }
        }
    }
    private fun setTimer() {
        val minutes = etTimer.text.toString().toIntOrNull() ?: 0
        countDown?.cancel()
        if (minutes <= 0) { toast("已取消定时关闭"); return }
        countDown = object : CountDownTimer(minutes * 60_000L, 1000L) {
            override fun onTick(ms: Long) { }
            override fun onFinish() {
                tts.stop()
                btnPlay.text = "▶ 开始朗读"
                toast("⏰ 定时时间到，已停止朗读")
            }
        }.start()
        toast("已设定 $minutes 分钟后自动停止朗读")
    }
    private fun openLibrary() {
        val files = File(filesDir).listFiles { f -> f.name.endsWith(".txt") || f.name.endsWith(".epub") }
        if (files.isNullOrEmpty()) { toast("书库为空，请先导入小说"); return }
        val names = files.map { it.name }.toTypedArray()
        android.app.AlertDialog.Builder(this)
            .setTitle("我的书库")
            .setItems(names) { _, which ->
                val f = files[which]
                val text = try { f.readText() } catch (_: Exception) { "" }
                if (text.isBlank()) toast("读取失败") else loadBook(text, f.name)
            }
            .show()
    }
    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() {
        super.onDestroy()
        countDown?.cancel()
        tts.shutdown()
    }
}