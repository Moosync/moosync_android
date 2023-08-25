package app.moosync.moosync.utils.responses.spotify

data class SpotifyUserPlaylistResponse(
    val href: String,
    val items: List<Item>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: String,
    val total: Int
) {
    data class Item(
        val collaborative: Boolean,
        val description: String,
        val external_urls: ExternalUrls,
        val href: String,
        val id: String,
        val images: List<Image>,
        val name: String,
        val owner: Owner,
        val `public`: Boolean,
        val snapshot_id: String,
        val tracks: Tracks,
        val type: String,
        val uri: String
    )

    data class ExternalUrls(
        val spotify: String
    )

    data class Image(
        val height: Int,
        val url: String,
        val width: Int
    )

    data class Owner(
        val display_name: String,
        val external_urls: ExternalUrls,
        val followers: Followers,
        val href: String,
        val id: String,
        val type: String,
        val uri: String
    )

    data class Tracks(
        val href: String,
        val total: Int
    )

    data class Followers(
        val href: String,
        val total: Int
    )
}