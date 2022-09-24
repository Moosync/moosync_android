package app.moosync.moosync

import android.content.ComponentName
import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
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

class MainActivity : AppCompatActivity() {

    private lateinit var mMediaBrowser: MediaBrowserCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repo = SongRepository(this)

        PermissionManager(this).requestPermission {
            val songs = AudioScanner().readDirectory(this)
            repo.insert(*songs.map { it.toDatabaseEntity() }.toTypedArray())

            Log.d("TAG", "onCreate: added songs")
        }

        setContentView(R.layout.activity_main)

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

    override fun onDestroy() {
        mMediaBrowser.disconnect()
        super.onDestroy()
    }
}
