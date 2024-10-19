package com.technogenis.alarammaster.services
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder
import android.util.Log

class SilentModeService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val mode = intent?.getStringExtra("mode")
        setAudioMode(mode)
        stopSelf() // Stop the service after setting the mode
        return START_NOT_STICKY
    }

    private fun setAudioMode(mode: String?) {
        val audioManager = getSystemService(AudioManager::class.java)
        if (mode == "silent") {
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            Log.d("SilentModeService", "Phone is set to silent mode")
        } else if (mode == "normal") {
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            Log.d("SilentModeService", "Phone is set to normal mode")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}