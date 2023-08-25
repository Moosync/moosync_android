package app.moosync.moosync.utils

object Constants {
    const val NOTIFICATION_CHANNEL_ID = "now_playing_media"
    const val NOTIFICATION_ID = 0xb339

    const val ACTION_FROM_MAIN_ACTIVITY = "from_main_activity"
}

enum class PlayerTypes {
    LOCAL,
    YOUTUBE
}

enum class PlaybackState {
    PLAYING,
    PAUSED,
    STOPPED
}