package com.example.taxipilot.feature.driver

// Moteur vocal — encapsule SpeechRecognizer (voix → texte) et TextToSpeech (texte → voix).
//
// Architecture :
//   - SpeechRecognizer : API Android, nécessite le thread principal → créé dans DriverVoiceTab
//   - TextToSpeech : initialisé avec fr-MA (français Maroc), fallback sur Locale.FRENCH
//   - listenState : IDLE → LISTENING → PROCESSING → IDLE (suivi de l'état micro)
//   - partialText : texte reconnu en temps réel (mise à jour pendant que l'utilisateur parle)
//
// Langues configurées :
//   - Primaire : fr-MA (français Maroc)
//   - Secondaire : ar-MA (arabe Maroc) — accepté mais non parsé par VoiceParser
//
// IMPORTANT : appeler destroy() dans DisposableEffect pour libérer les ressources Android.

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

class VoiceEngine(private val context: Context) {

    // États possibles de l'écoute micro
    enum class ListenState { IDLE, LISTENING, PROCESSING }

    private val _listenState = MutableStateFlow(ListenState.IDLE)
    val listenState: StateFlow<ListenState> = _listenState.asStateFlow()

    // Texte partiellement reconnu (mis à jour en temps réel pendant l'écoute)
    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    // true si la reconnaissance vocale est disponible sur cet appareil
    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    init {
        // Initialise le moteur TTS avec fr-MA, fallback sur français générique
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val frMA = Locale("fr", "MA")
                val result = tts?.setLanguage(frMA)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.FRENCH // fallback si fr-MA indisponible
                }
                ttsReady = true
            }
        }
    }

    // ── Reconnaissance vocale ─────────────────────────────────────────────────

    // Démarre l'écoute micro et appelle onResult avec le texte reconnu, ou onError si échec
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
                    _listenState.value = ListenState.PROCESSING // traitement en cours
                }
                override fun onPartialResults(results: Bundle) {
                    // Met à jour le texte affiché en temps réel pendant l'écoute
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
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-MA")             // langue principale
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fr-MA")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("ar-MA")) // langue secondaire
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) // résultats partiels en temps réel
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)        // jusqu'à 5 hypothèses de reconnaissance
        }
        recognizer?.startListening(intent)
    }

    // Arrête l'écoute manuellement (l'utilisateur a appuyé sur stop)
    fun stopListening() {
        recognizer?.stopListening()
        _listenState.value = ListenState.IDLE
    }

    // ── Synthèse vocale (Text-to-Speech) ─────────────────────────────────────

    // Lit le texte à voix haute et appelle onDone quand terminé
    fun speak(text: String, onDone: () -> Unit = {}) {
        if (!ttsReady) { onDone(); return }
        val id = "utt_${System.currentTimeMillis()}"
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {}
            override fun onDone(utteranceId: String) = onDone()
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String) = onDone()
        })
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, id) // interrompt le discours en cours
    }

    // Arrête la synthèse vocale immédiatement
    fun stopSpeaking() = tts?.stop()

    // ── Cycle de vie ──────────────────────────────────────────────────────────

    // Libère les ressources Android — DOIT être appelé dans DisposableEffect.onDispose()
    fun destroy() {
        recognizer?.destroy()
        recognizer = null
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    // ── Messages d'erreur traduits ────────────────────────────────────────────

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
