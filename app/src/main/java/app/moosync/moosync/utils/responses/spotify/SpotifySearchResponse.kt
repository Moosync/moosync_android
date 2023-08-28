package app.moosync.moosync.utils.responses.spotify

data class SpotifySearchResponse(
    val albums: Albums?,
    val artists: Artists?,
    val playlists: Playlists?,
    val tracks: TracksX?
) {
    data class Albums(
        val href: String,
        val items: List<SpotifyAlbum>?,
        val limit: Int,
        val next: String,
        val offset: Int,
        val previous: Any,
        val total: Int
    )

    data class Artists(
        val href: String,
        val items: List<SpotifyArtist>?,
        val limit: Int,
        val next: String,
        val offset: Int,
        val previous: Any,
        val total: Int
    )

    data class Playlists(
        val href: String,
        val items: List<SpotifyUserPlaylistResponse.Item>,
        val limit: Int,
        val next: String,
        val offset: Int,
        val previous: Any,
        val total: Int
    )

    data class TracksX(
        val href: String,
        val items: List<SpotifySong>,
        val limit: Int,
        val next: String,
        val offset: Int,
        val previous: Any,
        val total: Int
    )

    data class ExternalUrlsX(
        val spotify: String
    )

    data class Image(
        val height: Int,
        val url: String,
        val width: Int
    )

    data class SpotifyArtist(
        val external_urls: ExternalUrlsX,
        val followers: Followers,
        val genres: List<String>,
        val href: String,
        val id: String,
        val images: List<Image>,
        val name: String,
        val popularity: Int,
        val type: String,
        val uri: String
    )

    data class Followers(
        val href: Any,
        val total: Int
    )

    data class SpotifySong(
        val album: SpotifyAlbum,
        val artists: List<SpotifyArtist>,
        val available_markets: List<String>,
        val disc_number: Int,
        val duration_ms: Int,
        val explicit: Boolean,
        val external_ids: ExternalIds,
        val external_urls: ExternalUrlsX,
        val href: String,
        val id: String,
        val is_local: Boolean,
        val name: String,
        val popularity: Int,
        val preview_url: String,
        val track_number: Int,
        val type: String,
        val uri: String
    )

    data class SpotifyAlbum(
        val album_type: String,
        val artists: List<SpotifyArtist>,
        val available_markets: List<String>,
        val external_urls: ExternalUrlsX,
        val href: String,
        val id: String,
        val images: List<Image>?,
        val name: String,
        val release_date: String,
        val release_date_precision: String,
        val total_tracks: Int,
        val type: String,
        val uri: String
    )

    data class ExternalIds(
        val isrc: String
    )
}