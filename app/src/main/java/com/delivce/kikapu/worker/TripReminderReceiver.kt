package com.delivce.kikapu.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class TripReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_DISMISS_REMINDER) {
            val tripId = intent.getStringExtra(TripReminderWorker.KEY_TRIP_ID) ?: return
            NotificationManagerCompat.from(context).cancel(tripId.hashCode())
        }
    }

    companion object {
        const val ACTION_DISMISS_REMINDER = "com.delivce.kikapu.ACTION_DISMISS_REMINDER"
    }
}
