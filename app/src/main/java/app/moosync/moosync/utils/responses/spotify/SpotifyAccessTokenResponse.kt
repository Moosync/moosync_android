package app.moosync.moosync.utils.responses.spotify

data class SpotifyAccessTokenResponse(val access_token: String?, val refresh_token: String?, val expires_in: Double)