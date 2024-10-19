package com.technogenis.alarammaster.services

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class AppLifecycleObserver : LifecycleObserver {

    // Called when the app moves to the foreground
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        Log.d("AppLifecycleObserver", "App in Foreground")
    }

    // Called when the app moves to the background
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        Log.d("AppLifecycleObserver", "App in Background")
    }
}