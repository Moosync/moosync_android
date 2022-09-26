package app.moosync.moosync.utils.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import app.moosync.moosync.R
import app.moosync.moosync.utils.Constants.NOTIFICATION_CHANNEL_ID
import app.moosync.moosync.utils.Constants.NOTIFICATION_ID
import app.moosync.moosync.utils.services.Actions.ACTION_QUIT
import app.moosync.moosync.utils.services.Actions.ACTION_REWIND
import app.moosync.moosync.utils.services.Actions.ACTION_SKIP
import app.moosync.moosync.utils.services.Actions.ACTION_TOGGLE_PAUSE

class MediaNotificationManager(
    private val mContext: Context,
    private val token: MediaSessionCompat.Token
) {
    private val notificationManager: NotificationManager =
        mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var notificationBuilder: NotificationCompat.Builder =
        NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID)
    private var notification: Notification? = null

    init {
        // Cancel all notifications
        notificationManager.cancelAll()
        createNotificationChannel()
        createNotification()
    }

    private fun createNotificationChannel() {
        val existingChannel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
        if (existingChannel == null) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Now playing",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.enableLights(false)
            channel.enableVibration(false)
            channel.setShowBadge(false)

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() {
        val mediaStyle = MediaStyle().setMediaSession(token).setShowActionsInCompactView(0, 1, 2)

        notificationBuilder
            .setStyle(mediaStyle)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setShowWhen(false)
            .build()

        notification = notificationBuilder.build()
        updateMetadata()
    }

    fun updateMetadata() {
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}