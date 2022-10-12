package app.moosync.moosync.utils.services

import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import app.moosync.moosync.R
import app.moosync.moosync.utils.Constants.ACTION_FROM_MAIN_ACTIVITY
import app.moosync.moosync.utils.Constants.NOTIFICATION_ID
import app.moosync.moosync.utils.PlaybackState
import app.moosync.moosync.utils.models.Song
import app.moosync.moosync.utils.services.interfaces.MediaControls
import app.moosync.moosync.utils.services.interfaces.MediaPlayerCallbacks
import app.moosync.moosync.utils.services.interfaces.MediaServiceWrapper

class MediaPlayerService : MediaBrowserServiceCompat() {
    // Manages everything related to music playback
    private lateinit var mediaController: MediaController

    // Binder used to connect to activity
    private val binder: IBinder = MediaPlayerBinder()

    private var isForegroundService = false
    private var isMainActivityRunning = false

    override fun onCreate() {
        super.onCreate()

        mediaController = MediaController(this, object : MediaController.ForegroundServiceCallbacks {
            override fun shouldStartForeground() {
                handleStartForeground()
            }

            override fun shouldStopForeground() {
                if (isMainActivityRunning) {
                    handleStopForeground()
                } else {
                    quit()
                }
            }
        })
        sessionToken = mediaController.sessionToken
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val fromMainActivity = intent?.extras?.getBoolean(ACTION_FROM_MAIN_ACTIVITY) ?: false
        if (fromMainActivity) {
            isMainActivityRunning = true
        }
        return START_NOT_STICKY
    }

    private fun handleStopForeground() {
        if (isForegroundService) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            isForegroundService = false
        }
    }

    private fun handleStartForeground() {
        if (!isForegroundService) {
            Log.d("TAG", "handleStartForeground: starting foreground")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    mediaController.notification!!,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, mediaController.notification)
            }

            isForegroundService = true
        }
    }

    private fun quit() {
        handleStopForeground()
        mediaController.release()
        stopSelf()
    }

    override fun onDestroy() {
        mediaController.release()
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
        if(mediaController.decideQuit()) {
            quit()
        }
    }

    inner class MediaPlayerBinder : Binder() {
        val service = object: MediaServiceWrapper {
            override val controls: MediaControls
                get() = this@MediaPlayerService.mediaController.controls

            override val currentSong: Song
                get() = this@MediaPlayerService.mediaController.queue.currentSong

            override val playbackState: PlaybackState
                get() = this@MediaPlayerService.mediaController.playerState

            override fun decideQuit() {
                this@MediaPlayerService.decideQuit()
            }

            override fun setMainActivityStatus(isRunning: Boolean) {
                isMainActivityRunning = isRunning
            }

            override fun addMediaPlayerCallbacks(callbacks: MediaPlayerCallbacks) {
                this@MediaPlayerService.mediaController.addPlayerCallbacks(callbacks)
            }
        }
    }
}