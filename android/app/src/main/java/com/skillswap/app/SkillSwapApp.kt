package com.skillswap.app

import android.app.Application
import android.util.Log

/**
 * Application entry point. Kept deliberately small - just enough to confirm
 * process start in logs, which is useful when demonstrating the app running
 * on a device for the video walkthrough.
 */
class SkillSwapApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "SkillSwap application started")
    }

    companion object {
        const val TAG = "SkillSwapApp"
    }
}
