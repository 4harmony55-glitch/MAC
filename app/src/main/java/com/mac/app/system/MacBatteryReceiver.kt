package com.mac.app.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MacBatteryReceiver:BroadcastReceiver(){ override fun onReceive(context:Context,intent:Intent){ if(intent.action==Intent.ACTION_BATTERY_CHANGED){ val level=intent.getIntExtra("level",-1); val scale=intent.getIntExtra("scale",100); val pct=if(level>=0&&scale>0) level*100/scale else -1; if(pct>=0) MacroRunner.runMatching(context,"battery",pct) } } }
