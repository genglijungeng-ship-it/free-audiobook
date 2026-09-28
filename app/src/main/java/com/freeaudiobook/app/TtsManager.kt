package com.freeaudiobook.app
import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
/**
 * 离线TTS朗读管理器（安卓原生引擎，完全免费无广告）
 * 支持：语速调节、逐句朗读、暂停/继续、自动下一句回调
 */
class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var sentences: List<String> = emptyList()
    private var currentIndex = 0
    private var playing = false
    private var rate = 1.0f
    var onSentenceStart: ((Int) -> Unit)? = null
    var onChapterFinished: (() -> Unit)? = null
    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.CHINA
                tts?.setSpeechRate(rate)
                ready = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        val idx = utteranceId?.toIntOrNull() ?: return
                        onSentenceStart?.invoke(idx)
                    }
                    override fun onDone(utteranceId: String?) {
                        if (!playing) return
                        currentIndex++
                        speakNext()
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        if (!playing) return
                        currentIndex++
                        speakNext()
                    }
                })
            }
        }
    }
    fun setRate(r: Float) {
        rate = r
        tts?.setSpeechRate(r)
    }
    /** 载入一章内容，按标点切句 */
    fun loadChapter(content: String) {
        sentences = content
            .split(Regex("(?<=[。！？!?；;\\n])"))
            .map { it.trim() }
            .filter { it.length >= 2 }
        currentIndex = 0
    }
    fun start() {
        if (!ready) return
        playing = true
        speakNext()
    }
    fun pause() {
        playing = false
        tts?.stop()
    }
    fun stop() {
        playing = false
        tts?.stop()
    }
    fun isPlaying(): Boolean = playing
    private fun speakNext() {
        if (!playing) return
        if (currentIndex >= sentences.size) {
            playing = false
            onChapterFinished?.invoke()
            return
        }
        val text = sentences[currentIndex]
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, currentIndex.toString())
    }
    fun shutdown() {
        playing = false
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}