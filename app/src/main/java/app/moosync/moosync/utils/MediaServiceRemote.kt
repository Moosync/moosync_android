package app.moosync.moosync.utils

import android.app.Activity
import android.content.*
import android.os.IBinder
import android.util.Log
import app.moosync.moosync.utils.models.Song
import app.moosync.moosync.utils.services.MediaPlayerService

class MediaServiceRemote private constructor(activity: Activity) {

    private var mediaPlayerService: MediaPlayerService? = null
    private val mContextWrapper: ContextWrapper = ContextWrapper(activity)
    private val serviceConnection: ServiceConnection

    init {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                val binder = p1 as MediaPlayerService.MusicBinder?
                mediaPlayerService = binder?.service
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                mediaPlayerService = null
            }
        }

        val contextWrapper = ContextWrapper(activity.applicationContext)
        val intent = Intent(contextWrapper, MediaPlayerService::class.java)

        // Start service as foreground
        contextWrapper.startForegroundService(intent)

        mContextWrapper.bindService(
            Intent().setClass(
                mContextWrapper,
                MediaPlayerService::class.java
            ), serviceConnection, Context.BIND_AUTO_CREATE
        )
    }

    fun playSong(song: Song) {
        mediaPlayerService?.mediaQueueManager?.playSong(song)
    }

    fun release() {
        Log.d("TAG", "release: releasing")
        mediaPlayerService?.decideQuit()
        if (mediaPlayerService != null) {
            mContextWrapper.unbindService(serviceConnection)
            mediaPlayerService = null
        }
    }

    // Maintain only single connection to service
    companion object {
        var isInitialized = false
        operator fun invoke(activity: Activity): MediaServiceRemote {
            if (!isInitialized) {
                return MediaServiceRemote(activity)
            }
            throw Error("Remote is already initialized")
        }
    }
}