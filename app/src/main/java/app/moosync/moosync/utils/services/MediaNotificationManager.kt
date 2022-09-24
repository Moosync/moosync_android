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
import app.moosync.moosync.utils.services.Actions.ACTION_QUIT
import app.moosync.moosync.utils.services.Actions.ACTION_REWIND
import app.moosync.moosync.utils.services.Actions.ACTION_SKIP
import app.moosync.moosync.utils.services.Actions.ACTION_TOGGLE_PAUSE
import app.moosync.moosync.utils.services.Actions.NOTIFICATION_CHANNEL_ID
import app.moosync.moosync.utils.services.Actions.NOTIFICATION_ID

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

        val playPauseAction = buildPlayAction(true)
        val previousAction = NotificationCompat.Action(
            R.drawable.ic_baseline_skip_previous_48,
            "Skip previous",
            retrievePlaybackAction(ACTION_REWIND)
        )
        val nextAction = NotificationCompat.Action(
            R.drawable.ic_baseline_skip_next_48,
            "Skip next",
            retrievePlaybackAction(ACTION_SKIP)
        )
        val dismissAction = NotificationCompat.Action(
            R.drawable.ic_baseline_close_48,
            "Close",
            retrievePlaybackAction(ACTION_QUIT)
        )
        notificationBuilder
            .setStyle(mediaStyle)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setShowWhen(false)
            .addAction(previousAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .addAction(dismissAction)
            .build()

        notification = notificationBuilder.build()
        updateMetadata()
    }

    private fun buildPlayAction(isPlaying: Boolean): NotificationCompat.Action {
        val playButtonResId =
            if (isPlaying) R.drawable.ic_baseline_pause_48 else R.drawable.ic_baseline_play_arrow_48
        return NotificationCompat.Action.Builder(
            playButtonResId,
            "Play pause",
            retrievePlaybackAction(ACTION_TOGGLE_PAUSE)
        ).build()
    }


    private fun retrievePlaybackAction(action: String): PendingIntent {
        val intent = Intent(action)
        val serviceName = ComponentName(mContext, MediaPlayerService::class.java)
        intent.component = serviceName
        return PendingIntent.getService(
            mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun updateMetadata() {
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}