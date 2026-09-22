package com.mac.app.system

import android.content.Context
import com.mac.app.data.MacroRepository
import com.mac.app.runtime.ExecutionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object MacroRunner {
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Default)
    fun runMatching(context:Context,key:String,batteryPercent:Int?=null) {
        val app=context.applicationContext
        MacroRepository(app).load().filter { it.enabled && it.triggers.any { trigger -> trigger.key==key && (key!="battery" || batteryMatches(trigger,batteryPercent ?: -1)) } }.forEach { macro ->
            scope.launch { ExecutionEngine(app).run(macro) { } }
        }
    }
    private fun batteryMatches(trigger:com.mac.app.domain.NodeSpec,pct:Int):Boolean {
        val threshold=trigger.parameters["threshold"]?.toIntOrNull() ?: 50
        return when(trigger.parameters["direction"]){ "at_or_above" -> pct>=threshold; "at_or_below" -> pct<=threshold; else -> pct==threshold }
    }
}
