package com.technogenis.alarammaster.appTracker

import android.app.Activity
import android.app.Application
import android.os.Bundle

class MyApplication : Application() {

    private var isInForeground = false

    override fun onCreate() {
        super.onCreate()

        // Register activity lifecycle callbacks
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                isInForeground = true
            }

            override fun onActivityStopped(activity: Activity) {
                isInForeground = false
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    fun isAppInForeground(): Boolean {
        return isInForeground
    }
}
