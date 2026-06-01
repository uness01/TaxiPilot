package com.example.taxipilot.feature.driver

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Wraps SpeechRecognizer + TextToSpeech.
 * Must be created on the main thread. Call [destroy] when done (DisposableEffect).
 */
class VoiceEngine(private val context: Context) {

    enum class ListenState { IDLE, LISTENING, PROCESSING }

    private val _listenState = MutableStateFlow(ListenState.IDLE)
    val listenState: StateFlow<ListenState> = _listenState.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Prefer fr-MA; fall back to generic French if the locale is unavailable
                val frMA = Locale("fr", "MA")
                val result = tts?.setLanguage(frMA)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.FRENCH
                }
                ttsReady = true
            }
        }
    }

    // ── Speech recognition ────────────────────────────────────────────────────

    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isAvailable) { onError("Reconnaissance vocale non disponible."); return }

        _partialText.value = ""
        _listenState.value = ListenState.LISTENING

        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle) {
                    _listenState.value = ListenState.LISTENING
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _listenState.value = ListenState.PROCESSING
                }
                override fun onPartialResults(results: Bundle) {
                    val partial = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull() ?: ""
                    _partialText.value = partial
                }
                override fun onResults(results: Bundle) {
                    _listenState.value = ListenState.IDLE
                    _partialText.value = ""
                    val text = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()?.lowercase(Locale.FRENCH)?.trim() ?: ""
                    if (text.isNotBlank()) onResult(text) else onError("Rien n'a été détecté.")
                }
                override fun onError(error: Int) {
                    _listenState.value = ListenState.IDLE
                    onError(recognitionErrorMessage(error))
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            // Primary: French (Morocco); ar-MA accepted as secondary
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-MA")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fr-MA")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            // Offer Arabic-Morocco as additional language (supported on Google Speech)
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("ar-MA"))
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }
        recognizer?.startListening(intent)
    }

    fun stopListening() {
        recognizer?.stopListening()
        _listenState.value = ListenState.IDLE
    }

    // ── Text-to-speech ────────────────────────────────────────────────────────

    fun speak(text: String, onDone: () -> Unit = {}) {
        if (!ttsReady) { onDone(); return }
        val id = "utt_${System.currentTimeMillis()}"
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {}
            override fun onDone(utteranceId: String) = onDone()
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String) = onDone()
        })
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
    }

    fun stopSpeaking() = tts?.stop()

    // ── Lifecycle ────────────────────────────────────────────────────────────

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun recognitionErrorMessage(code: Int) = when (code) {
        SpeechRecognizer.ERROR_AUDIO             -> "Erreur audio."
        SpeechRecognizer.ERROR_CLIENT            -> "Erreur client."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission microphone requise."
        SpeechRecognizer.ERROR_NETWORK           -> "Erreur réseau."
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT   -> "Délai réseau dépassé."
        SpeechRecognizer.ERROR_NO_MATCH          -> "Commande non reconnue. Réessayez."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY   -> "Reconnaissance en cours."
        SpeechRecognizer.ERROR_SERVER            -> "Erreur serveur."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT    -> "Aucune parole détectée."
        else                                     -> "Erreur inconnue ($code)."
    }
}
