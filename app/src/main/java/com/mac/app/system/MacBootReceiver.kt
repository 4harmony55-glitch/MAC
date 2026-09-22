package com.mac.app.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MacBootReceiver:BroadcastReceiver(){ override fun onReceive(context:Context,intent:Intent){ if(intent.action==Intent.ACTION_BOOT_COMPLETED) MacroRunner.runMatching(context,"boot") } }
