package app.moosync.moosync.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.moosync.moosync.utils.MediaServiceRemote

abstract class BaseMainActivity : AppCompatActivity() {
    private var mediaServiceRemote: MediaServiceRemote? = null

    fun getMediaRemote(): MediaServiceRemote? {
        return mediaServiceRemote
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectMediaRemote()
    }

    private fun connectMediaRemote() {
        mediaServiceRemote = MediaServiceRemote(this)
    }
}