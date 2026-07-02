package com.delivce.kikapu.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.delivce.kikapu.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

private const val CHANNEL_ID = "kikapu_trip_reminders"

@HiltWorker
class TripReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val tripId = inputData.getString(KEY_TRIP_ID) ?: return Result.failure()
        val tripName = inputData.getString(KEY_TRIP_NAME) ?: ""
        val userName = inputData.getString(KEY_USER_NAME) ?: ""

        showRetroNotification(tripId, tripName, userName)
        return Result.success()
    }

    private fun showRetroNotification(tripId: String, tripName: String, userName: String) {
        createNotificationChannel()

        val yesIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("kikapu://trip/$tripId")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val yesPendingIntent = PendingIntent.getActivity(
            applicationContext,
            tripId.hashCode(),
            yesIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val noIntent = Intent(applicationContext, TripReminderReceiver::class.java).apply {
            action = TripReminderReceiver.ACTION_DISMISS_REMINDER
            putExtra(KEY_TRIP_ID, tripId)
        }
        val noPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            tripId.hashCode(),
            noIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val remoteViews = RemoteViews(applicationContext.packageName, R.layout.notification_retro).apply {
            setTextViewText(R.id.notification_title, "► TRIP REMINDER")
            setTextViewText(
                R.id.notification_message,
                "$userName, ready to shop for \"$tripName\"?"
            )
            setOnClickPendingIntent(R.id.btn_yes, yesPendingIntent)
            setOnClickPendingIntent(R.id.btn_no, noPendingIntent)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shopping_cart)
            .setCustomContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(tripId.hashCode(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Trip Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val KEY_TRIP_ID = "tripId"
        const val KEY_TRIP_NAME = "tripName"
        const val KEY_USER_NAME = "userName"

        fun schedule(
            context: Context,
            tripId: String,
            tripName: String,
            userName: String,
            triggerAtMs: Long
        ) {
            val delay = (triggerAtMs - System.currentTimeMillis()).coerceAtLeast(0)
            val request = OneTimeWorkRequestBuilder<TripReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(
                        KEY_TRIP_ID to tripId,
                        KEY_TRIP_NAME to tripName,
                        KEY_USER_NAME to userName
                    )
                )
                .addTag("reminder_$tripId")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork("reminder_$tripId", ExistingWorkPolicy.REPLACE, request)
        }

        fun cancel(context: Context, tripId: String) {
            WorkManager.getInstance(context).cancelUniqueWork("reminder_$tripId")
        }
    }
}
