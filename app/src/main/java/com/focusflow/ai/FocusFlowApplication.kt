package com.focusflow.ai

import android.app.Application
import com.focusflow.ai.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FocusFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
