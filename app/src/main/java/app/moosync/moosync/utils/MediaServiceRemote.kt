package app.moosync.moosync.utils

import android.app.Activity
import android.content.*
import android.os.IBinder
import app.moosync.moosync.utils.Constants.ACTION_FROM_MAIN_ACTIVITY
import app.moosync.moosync.utils.models.Song
import app.moosync.moosync.utils.services.MediaPlayerService
import app.moosync.moosync.utils.services.interfaces.MediaPlayerCallbacks
import app.moosync.moosync.utils.services.interfaces.MediaServiceWrapper

class MediaServiceRemote private constructor(activity: Activity) {

    private var mediaService: MediaServiceWrapper? = null
    private val mContextWrapper: ContextWrapper = ContextWrapper(activity)
    private val serviceConnection: ServiceConnection

    private val methodQueue: MutableList<() -> Unit> = mutableListOf()

    init {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                val binder = p1 as MediaPlayerService.MediaPlayerBinder?
                mediaService = binder?.service
                runFromMethodQueue()
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                mediaService = null
            }
        }

        bindService()
    }

    private fun runFromMethodQueue() {
        for (method in methodQueue) {
            method.invoke()
        }
    }

    private fun runOrAddToQueue(method: () -> Unit) {
        if (mediaService == null) {
            methodQueue.add {
                method.invoke()
            }
            return
        }

        method.invoke()
    }

    private fun bindService() {
        if (mediaService == null) {
            val intent = Intent(mContextWrapper, MediaPlayerService::class.java)
            intent.putExtra(ACTION_FROM_MAIN_ACTIVITY, true)

            // Start service as foreground
            mContextWrapper.startService(intent)

            mContextWrapper.bindService(
                Intent().setClass(
                    mContextWrapper,
                    MediaPlayerService::class.java
                ), serviceConnection, Context.BIND_AUTO_CREATE
            )
        }
    }

    fun getCurrentSong(): Song? {
        return mediaService?.currentSong
    }

    fun playSong(song: Song) {
        runOrAddToQueue {
            mediaService!!.controls.playSong(song)
        }
    }

    fun addToQueue(song: Song) {
        runOrAddToQueue {
            mediaService!!.controls.addToQueue(song)
        }
    }

    fun addMediaCallbacks(callbacks: MediaPlayerCallbacks) {
        runOrAddToQueue {
            mediaService!!.addMediaPlayerCallbacks(callbacks)
        }
    }

    fun stopPlayback() {
        runOrAddToQueue {
            mediaService!!.controls.stop()
        }
    }

    fun release() {
        mediaService?.decideQuit()
        if (mediaService != null) {
            mediaService?.setMainActivityStatus(false)
            mContextWrapper.unbindService(serviceConnection)
            mediaService = null
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