package app.moosync.moosync.utils

import android.app.Activity
import android.content.*
import android.os.IBinder
import android.util.Log
import app.moosync.moosync.MainActivity
import app.moosync.moosync.utils.Constants.ACTION_FROM_MAIN_ACTIVITY
import app.moosync.moosync.utils.models.Song
import app.moosync.moosync.utils.services.MediaPlayerService
import app.moosync.moosync.utils.services.interfaces.MediaControls
import app.moosync.moosync.utils.services.interfaces.MediaPlayerCallbacks
import app.moosync.moosync.utils.services.interfaces.MediaServiceWrapper
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.lang.reflect.UndeclaredThrowableException

data class MethodRequirements(val scope: CoroutineScope?, val channel: Channel<Any?>?, val method: (mediaService: MediaServiceWrapper) -> Any?) {
    constructor(method: (mediaService: MediaServiceWrapper) -> Any?) : this(null, null, method)
}

class MediaServiceRemote private constructor(activity: Activity) {

    private var mediaService: MediaServiceWrapper? = null
    private val mContextWrapper: ContextWrapper = ContextWrapper(activity)
    private val serviceConnection: ServiceConnection

    // methodQueue containing methods which are to be executed after connection to service is created
    private val methodQueue: MutableList<MethodRequirements> = mutableListOf()

    // Return an instance of proxies media controls
    private var _controls: MediaControls? = null
    val controls: MediaControls?
        get() {
            if (_controls != null) {
                return _controls
            }

            val controls = mediaService?.controls
            if (controls != null) {
                _controls = Proxy.newProxyInstance(
                    controls.javaClass.classLoader,
                    controls.javaClass.interfaces,
                    TransportControlInvocationHandler(::runOrAddToQueue)
                ) as MediaControls
                return _controls
            }

            return null
        }

    init {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                val binder = p1 as MediaPlayerService.MediaPlayerBinder?
                mediaService = binder?.service
                mediaService?.let { runFromMethodQueue(it) }
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                mediaService = null
            }
        }

        bindService()
    }

    private fun runFromMethodQueue(mediaService: MediaServiceWrapper) {
        for (method in methodQueue) {
            val retValue = method.method.invoke(mediaService)
            val channel = method.channel
            val scope = method.scope

            if (channel != null && scope != null) {
                scope.launch {
                    channel.send(retValue)
                }
            }
        }
    }

    private fun runOrAddToQueue(method: (mediaService: MediaServiceWrapper) -> Unit) {
        if (mediaService == null) {
            methodQueue.add(MethodRequirements {
                method.invoke(it)
            })
            return
        }

        method.invoke(mediaService!!)
    }

    /**
     * This returns a deferred with method result
     * The result is returned after mediaService is created
     */
    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> runOrAddToQueueAsync(
        scope: CoroutineScope = CoroutineScope(
            Dispatchers.Default
        ), crossinline method: (mediaService: MediaServiceWrapper) -> T
    ): Deferred<T> = scope.async {
        if (mediaService == null) {

            val channel: Channel<T?> = Channel()

            methodQueue.add(MethodRequirements(scope, channel as Channel<Any?>) {
                return@MethodRequirements method.invoke(it)
            })

            channel.consume {
                val value = this.receive()
                channel.close()
                return@async value as T
            }

        } else {
            return@async method.invoke(mediaService!!)
        }
    }

    // Binds to the media service
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

    fun playSong(song: Song) {
        CoroutineScope(Dispatchers.Main).launch {
            val provider = (mContextWrapper.baseContext as MainActivity).providerStore.getProviderForId(song._id)
            val transformedSong = provider?.prePlaybackTransformation(song)?.await()
            Log.d("TAG", "playSong: got transformed song $transformedSong")
            if (transformedSong != null) {
                controls?.playSong(transformedSong)
            }
        }
    }

    fun getCurrentSongAsync(scope: CoroutineScope): Deferred<Song?> {
        return runOrAddToQueueAsync(scope) {
            return@runOrAddToQueueAsync it.currentSong
        }
    }

    fun getRepeatAsync(scope: CoroutineScope): Deferred<Boolean> {
        return runOrAddToQueueAsync(scope) {
            return@runOrAddToQueueAsync it.repeat
        }
    }

    fun addMediaCallbacks(callbacks: MediaPlayerCallbacks) {
        return runOrAddToQueue {
            it.addMediaPlayerCallbacks(callbacks)
        }
    }

    private fun getValidSong(queue: ArrayList<Song>, index: Int, isNext: Boolean): Song? {
        if (index < queue.size && index >= 0) {
            return queue[index]
        }

        return if (queue.size <= 1 || (queue.size == 2 && isNext)) {
            null
        } else {
            if (isNext) queue[0] else queue[queue.size - 1]
        }
    }

    fun getCircularSongsAsync(): Deferred<Array<Song?>> {
        return runOrAddToQueueAsync {
            val nextIndex = it.currentIndex + 1
            val prevIndex = it.currentIndex - 1

            val currentSong = it.currentSong
            val nextSong = getValidSong(it.queue, nextIndex, true)
            val prevSong = getValidSong(it.queue, prevIndex, false)

            return@runOrAddToQueueAsync arrayOf(prevSong, currentSong, nextSong)
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
        private var isInitialized = false
        operator fun invoke(activity: Activity): MediaServiceRemote {
            if (!isInitialized) {
                return MediaServiceRemote(activity)
            }
            throw Error("Remote is already initialized")
        }
    }

    // Proxy media controls to queue all method calls
    private class TransportControlInvocationHandler(private val addToQueueHandler: (method: (mediaService: MediaServiceWrapper) -> Unit) -> Unit) :
        InvocationHandler {
        override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?) {
            try {
                addToQueueHandler {
                    if (args != null) {
                        method?.invoke(it.controls, *args)
                    } else {
                        method?.invoke(it.controls)
                    }
                }
            } catch (e: UndeclaredThrowableException) {
                Log.e("TransportControlInvocationHandler", "invoke: Failed to run method ${method?.name} $e", )
            }
        }
    }
}