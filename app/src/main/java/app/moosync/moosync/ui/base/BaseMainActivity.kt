package app.moosync.moosync.ui.base

import android.os.Bundle
import app.moosync.moosync.providers.ProviderStore
import app.moosync.moosync.utils.MediaServiceRemote
import app.moosync.moosync.utils.services.interfaces.MediaControls

abstract class BaseMainActivity : BaseActivity() {
    private lateinit var mediaServiceRemote: MediaServiceRemote
    lateinit var providerStore: ProviderStore

    protected val TAG: String
        get() = javaClass.kotlin.simpleName ?: javaClass.kotlin.toString()

    fun getMediaRemote(): MediaServiceRemote {
        return mediaServiceRemote
    }

    fun getMediaControls(): MediaControls? {
        return this.getMediaRemote().controls
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        connectMediaRemote()
        initProviderStore()
        super.onCreate(savedInstanceState)
    }

    private fun connectMediaRemote() {
        mediaServiceRemote = MediaServiceRemote(this)
    }

    private fun initProviderStore() {
        providerStore = ProviderStore(this)
    }
}