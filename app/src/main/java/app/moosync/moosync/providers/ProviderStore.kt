package app.moosync.moosync.providers

import android.content.Context

class ProviderStore(private val context: Context) {
    private val providers: ArrayList<GenericProvider> = ArrayList()

    init {
        initProviders()
    }

    private fun initProviders() {
        providers.add(YoutubeProvider(context))
        providers.add(SpotifyProvider(context))
    }

    fun getAllProviders(): ArrayList<GenericProvider> {
        return providers
    }
}