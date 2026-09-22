package com.mac.app.domain

import android.content.Context
import org.json.JSONObject

data class InventoryEntry(val id:String,val name:String,val kind:NodeType)

class CapabilityInventory(private val context:Context){
    val entries:List<InventoryEntry> by lazy { load() }
    private fun load():List<InventoryEntry>{
        val text=context.assets.open("capability_catalog.json").bufferedReader().use{it.readText()}
        val root=JSONObject(text)
        val result=ArrayList<InventoryEntry>()
        fun read(key:String,type:NodeType){
            val a=root.getJSONArray(key)
            for(i in 0 until a.length()){
                val o=a.getJSONObject(i)
                result += InventoryEntry(o.getString("id"),o.getString("name"),type)
            }
        }
        read("triggers",NodeType.TRIGGER);read("constraints",NodeType.CONSTRAINT);read("actions",NodeType.ACTION)
        return result
    }
}
