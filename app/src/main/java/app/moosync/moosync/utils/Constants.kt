package app.moosync.moosync.utils

object Constants {
    const val NOTIFICATION_CHANNEL_ID = "now_playing_media"
    const val NOTIFICATION_ID = 0xb339
}

enum class PlayerTypes {
    LOCAL,
    YOUTUBE
}

enum class PlaybackStates {
    PLAYING,
    PAUSED,
    STOPPED
}
