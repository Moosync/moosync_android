package app.moosync.moosync.ui.base

import android.os.Bundle
import app.moosync.moosync.utils.MediaServiceRemote
import app.moosync.moosync.utils.services.interfaces.MediaControls

abstract class BaseMainActivity : BaseActivity() {
    private lateinit var mediaServiceRemote: MediaServiceRemote

    fun getMediaRemote(): MediaServiceRemote {
        return mediaServiceRemote
    }

    fun getMediaControls(): MediaControls? {
        return this.getMediaRemote().controls
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        connectMediaRemote()
        super.onCreate(savedInstanceState)
    }

    private fun connectMediaRemote() {
        mediaServiceRemote = MediaServiceRemote(this)
    }
}