package app.moosync.moosync

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import app.moosync.moosync.utils.db.repository.SongRepository
import app.moosync.moosync.utils.helpers.AudioScanner
import app.moosync.moosync.utils.helpers.PermissionManager

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
    }

    override fun onDestroy() {
        mMediaBrowser?.disconnect()
        super.onDestroy()
    }
}
