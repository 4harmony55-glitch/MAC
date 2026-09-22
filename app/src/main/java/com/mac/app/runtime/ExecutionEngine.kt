package com.mac.app.runtime

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mac.app.R
import com.mac.app.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.coroutines.resume

class ExecutionEngine(private val context: Context) {
    private val variables = mutableMapOf<String, String>()

    suspend fun run(macro: Macro, emit: (ExecutionEvent) -> Unit) {
        emit(ExecutionEvent(level=ExecutionEvent.Level.INFO, message="Starting ${macro.name}"))
        if (!macro.enabled) { emit(ExecutionEvent(level=ExecutionEvent.Level.WARNING, message="Macro is disabled")); return }
        macro.constraints.forEach { emit(ExecutionEvent(level=ExecutionEvent.Level.INFO, message="Constraint queued: ${it.title}")) }
        macro.actions.forEachIndexed { index, action ->
            emit(ExecutionEvent(level=ExecutionEvent.Level.INFO, message="Action ${index + 1}: ${action.title}"))
            try { execute(action); emit(ExecutionEvent(level=ExecutionEvent.Level.SUCCESS, message="Completed: ${action.title}")) }
            catch (t: Throwable) { emit(ExecutionEvent(level=ExecutionEvent.Level.ERROR, message="Failed: ${action.title}: ${t.message ?: t::class.simpleName}")) }
        }
        emit(ExecutionEvent(level=ExecutionEvent.Level.SUCCESS, message="Finished ${macro.name}"))
    }

    private suspend fun execute(action: NodeSpec) {
        fun p(name: String, fallback: String = "") = action.parameters[name] ?: fallback
        when (action.key) {
            "wait" -> delay((p("seconds", "1").toLongOrNull() ?: 1).coerceIn(0, 3600) * 1000)
            "notify" -> notify(p("title", "MAC"), p("text", "Automation executed"))
            "variable" -> variables[p("name", "value")] = p("value")
            "clipboard" -> { val cm=context.getSystemService(ClipboardManager::class.java); cm.setPrimaryClip(ClipData.newPlainText("MAC", p("text"))) }
            "vibrate" -> { val v=context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator; val ms=(p("milliseconds","150").toLongOrNull() ?: 150).coerceIn(1,5000); if(Build.VERSION.SDK_INT>=26) v.vibrate(VibrationEffect.createOneShot(ms,VibrationEffect.DEFAULT_AMPLITUDE)) else v.vibrate(ms) }
            "speak" -> speak(p("text","MAC"))
            "volume" -> { val audio=context.getSystemService(Context.AUDIO_SERVICE) as AudioManager; val max=audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC); val pct=(p("percent","50").toIntOrNull() ?: 50).coerceIn(0,100); audio.setStreamVolume(AudioManager.STREAM_MUSIC,max*pct/100,0) }
            "launch_app" -> { val pkg=p("package").trim(); require(pkg.isNotEmpty()); val i=context.packageManager.getLaunchIntentForPackage(pkg) ?: error("Application not found: $pkg"); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); context.startActivity(i) }
            "http" -> request(p("url"), p("method","GET"))
            else -> error("Adapter not implemented yet for ${action.key}")
        }
    }

    private fun notify(title:String,text:String) {
        require(NotificationManagerCompat.from(context).areNotificationsEnabled()) { "Notifications are disabled for MAC" }
        val nm=context.getSystemService(NotificationManager::class.java)
        if(Build.VERSION.SDK_INT>=26) nm.createNotificationChannel(NotificationChannel(CHANNEL,"MAC automation",NotificationManager.IMPORTANCE_DEFAULT))
        nm.notify((System.currentTimeMillis() and 0x7fffffff).toInt(), NotificationCompat.Builder(context,CHANNEL).setSmallIcon(R.drawable.ic_stat_mac).setContentTitle(title).setContentText(text).setAutoCancel(true).build())
    }

    private suspend fun request(url:String, method:String) = withContext(Dispatchers.IO) {
        require(url.startsWith("https://")) { "Only HTTPS is supported by this adapter" }
        val c=URL(url).openConnection() as HttpURLConnection
        try { c.requestMethod=method.uppercase(Locale.US); c.connectTimeout=10_000; c.readTimeout=10_000; if(c.responseCode !in 200..299) error("HTTP ${c.responseCode}"); c.inputStream.use { it.readBytes() } } finally { c.disconnect() }
    }

    private suspend fun speak(text:String)=suspendCancellableCoroutine<Unit> { cont ->
        var tts:TextToSpeech?=null
        tts=TextToSpeech(context.applicationContext) { status ->
            if(status!=TextToSpeech.SUCCESS) {
                if(cont.isActive) cont.resume(Unit)
                tts?.shutdown()
                return@TextToSpeech
            }
            tts?.setOnUtteranceProgressListener(object:UtteranceProgressListener(){
                override fun onStart(utteranceId:String?){ }
                override fun onDone(utteranceId:String?){ if(cont.isActive) cont.resume(Unit); tts?.shutdown() }
                override fun onError(utteranceId:String?){ if(cont.isActive) cont.resume(Unit); tts?.shutdown() }
            })
            tts?.language=Locale.getDefault()
            tts?.speak(text,TextToSpeech.QUEUE_FLUSH,Bundle(),"mac-${System.nanoTime()}")
        }
        cont.invokeOnCancellation { tts?.stop(); tts?.shutdown() }
    }

    companion object { const val CHANNEL="mac_automation" }
}
