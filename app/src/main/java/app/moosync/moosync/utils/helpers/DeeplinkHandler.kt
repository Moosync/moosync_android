package app.moosync.moosync.utils.helpers

import android.net.Uri

object DeeplinkHandler {
    private val activeListeners: HashMap<String, (url: Uri) -> Unit> = HashMap()

    fun addListener(host: String, callback: (url: Uri) -> Unit) {
        activeListeners[host] = callback
    }

    fun triggerCallback(url: Uri) {

        val registeredCallback = activeListeners[url.host]
        if (registeredCallback !== null) {
            registeredCallback(url)
            activeListeners.remove(url.host)
        }
    }
}