package com.technogenis.alarammaster

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (intent.action) {
            "SILENT_MODE" -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            }
            "NORMAL_MODE" -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
        }
    }
}
