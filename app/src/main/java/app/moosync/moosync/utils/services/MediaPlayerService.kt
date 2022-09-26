package app.moosync.moosync.utils.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import app.moosync.moosync.R
import app.moosync.moosync.utils.Constants.NOTIFICATION_ID
import app.moosync.moosync.utils.services.Actions.ACTION_QUIT

class MediaPlayerService : MediaBrowserServiceCompat() {
    // Manages everything related to music playback
    lateinit var mediaQueueManager: MediaQueueManager

    // Binder used to connect to activity
    private val binder: IBinder = MusicBinder()

    override fun onCreate() {
        super.onCreate()

        Log.d("TAG", "onCreate: creating service")

        mediaQueueManager = MediaQueueManager(this)
        sessionToken = mediaQueueManager.sessionToken

        registerReceiver(receiver, IntentFilter(ACTION_QUIT))
        startForeground(NOTIFICATION_ID, mediaQueueManager.notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TAG", "onStartCommand: $intent")
        when (intent?.action) {
            ACTION_QUIT -> quit()
        }
        return START_NOT_STICKY
    }

    private fun quit() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        mediaQueueManager.release()
        stopSelf()
    }

    override fun onDestroy() {
        Log.d("TAG", "onDestroy: destroying service")
        unregisterReceiver(receiver)
        mediaQueueManager.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return if ("android.media.browse.MediaBrowserService" == intent?.action) {
            super.onBind(intent)
        } else binder
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(getString(R.string.app_name), null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(null)
    }

    fun decideQuit() {
        if(mediaQueueManager.decideQuit()) {
            quit()
        }
    }

    inner class MusicBinder : Binder() {
        val service: MediaPlayerService
            get() = this@MediaPlayerService
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            quit()
        }
    }
}