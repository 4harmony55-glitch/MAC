package com.mac.app.data

import android.content.Context
import com.mac.app.domain.*
import org.json.JSONArray
import org.json.JSONObject

class MacroRepository(context: Context) {
    private val prefs = context.getSharedPreferences("mac_storage", Context.MODE_PRIVATE)
    fun load(): List<Macro> = runCatching {
        val root = JSONArray(prefs.getString(KEY, "[]") ?: "[]")
        buildList { for (i in 0 until root.length()) add(fromJson(root.getJSONObject(i))) }
    }.getOrDefault(emptyList())
    fun save(macros: List<Macro>) { prefs.edit().putString(KEY, JSONArray().apply { macros.forEach { put(toJson(it)) } }.toString()).apply() }
    fun upsert(macro: Macro) = save((load().filterNot { it.id == macro.id } + macro).sortedBy { it.name.lowercase() })

    private fun toJson(m: Macro) = JSONObject().apply {
        put("id", m.id); put("name", m.name); put("enabled", m.enabled); put("createdAt", m.createdAt); put("updatedAt", m.updatedAt)
        put("triggers", nodes(m.triggers)); put("constraints", nodes(m.constraints)); put("actions", nodes(m.actions))
    }
    private fun nodes(values: List<NodeSpec>) = JSONArray().apply { values.forEach { n -> put(JSONObject().apply {
        put("id", n.id); put("type", n.type.name); put("key", n.key); put("title", n.title); put("description", n.description); put("parameters", JSONObject(n.parameters)); put("capability", n.capability.name)
    }) } }
    private fun fromJson(o: JSONObject): Macro {
        fun parse(key: String) = buildList {
            val a = o.optJSONArray(key) ?: JSONArray()
            for (i in 0 until a.length()) {
                val n = a.getJSONObject(i); val po = n.optJSONObject("parameters") ?: JSONObject()
                val params = buildMap { po.keys().forEach { k -> put(k, po.optString(k)) } }
                add(NodeSpec(id=n.optString("id"), type=runCatching { NodeType.valueOf(n.optString("type")) }.getOrDefault(NodeType.ACTION), key=n.optString("key"), title=n.optString("title"), description=n.optString("description"), parameters=params, capability=runCatching { Capability.valueOf(n.optString("capability")) }.getOrDefault(Capability.NONE)))
            }
        }
        return Macro(id=o.optString("id"), name=o.optString("name", "Untitled macro"), enabled=o.optBoolean("enabled", true), triggers=parse("triggers"), constraints=parse("constraints"), actions=parse("actions"), createdAt=o.optLong("createdAt", System.currentTimeMillis()), updatedAt=o.optLong("updatedAt", System.currentTimeMillis()))
    }
    private companion object { const val KEY = "macros_v2" }
}
