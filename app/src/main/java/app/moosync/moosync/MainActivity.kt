package app.moosync.moosync

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import app.moosync.moosync.utils.db.repository.SongRepository
import app.moosync.moosync.utils.helpers.AudioScanner
import app.moosync.moosync.utils.helpers.PermissionManager
import app.moosync.moosync.utils.services.MediaPlayerService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var mMediaBrowser: MediaBrowserCompat

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionManager(this).requestPermission {
            // TODO: Decide which scope would be better
            GlobalScope.launch(Dispatchers.IO) {
                val songs = AudioScanner().readDirectory(this@MainActivity)
                val repo = SongRepository(this@MainActivity)
                repo.insert(*songs.map { it.toDatabaseEntity() }.toTypedArray())

                Log.d("TAG", "onCreate: added songs")
            }
        }

        mMediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlayerService::class.java),
            object : MediaBrowserCompat.ConnectionCallback() {
                override fun onConnected() {
                    registerMediaController(mMediaBrowser.sessionToken)
                    Log.d("TAG", "onConnected: connected")
                    super.onConnected()
                }

                override fun onConnectionFailed() {
                    Log.d("TAG", "onConnectionFailed: connection failed")
                    super.onConnectionFailed()
                }

                override fun onConnectionSuspended() {
                    Log.d("TAG", "onConnectionFailed: connection suspended")
                    super.onConnectionSuspended()
                }
            },
            null
        )

        mMediaBrowser.connect()
    }

    private fun registerMediaController(token: MediaSessionCompat.Token) {
        val mediaController = MediaControllerCompat(this, token)
        MediaControllerCompat.setMediaController(this, mediaController)
        mediaController.registerCallback(object : MediaControllerCompat.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                super.onMetadataChanged(metadata)
                Log.d(
                    "TAG", "metadata changed to ${
                        metadata?.getString(
                            MediaMetadataCompat.METADATA_KEY_MEDIA_URI
                        )
                    }"
                )
            }

            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                super.onPlaybackStateChanged(state)
                Log.d("TAG", "onPlaybackStateChanged: state changed to $state")
            }
        })

    }

    fun getMediaBrowser(): MediaBrowserCompat {
        if (!mMediaBrowser.isConnected) {
            mMediaBrowser.connect()
        }

        return mMediaBrowser
    }

    override fun onDestroy() {
        mMediaBrowser.disconnect()
        super.onDestroy()
    }
}
