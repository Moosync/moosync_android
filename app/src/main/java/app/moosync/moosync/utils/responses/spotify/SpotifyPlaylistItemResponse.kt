package app.moosync.moosync.utils.responses.spotify
data class SpotifyPlaylistItemResponse(
    val href: String,
    val items: List<Item>?,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: Any,
    val total: Int
) {
    data class VideoThumbnail(
        val url: Any
    )

    data class ExternalUrls(
        val spotify: String
    )

    data class Item(
        val added_at: String,
        val added_by: AddedBy,
        val is_local: Boolean,
        val primary_color: Any,
        val track: SpotifySearchResponse.SpotifySong,
        val video_thumbnail: VideoThumbnail
    )

    data class AddedBy(
        val external_urls: ExternalUrls,
        val href: String,
        val id: String,
        val type: String,
        val uri: String
    )
}