package app.moosync.moosync.utils

object Constants {
    const val BUNDLE_SONG_KEY = "song"

    const val TRANSPORT_CONTROL_PLAY_SONG = "playCustomSong"

    const val NOTIFICATION_CHANNEL_ID = "now_playing_media"
    const val NOTIFICATION_ID = 0xb339
}

enum class PlayerTypes {
    LOCAL,
    YOUTUBE
}
