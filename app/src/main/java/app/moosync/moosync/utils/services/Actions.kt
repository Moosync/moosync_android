package app.moosync.moosync.utils.services

import android.support.v4.media.session.PlaybackStateCompat

object Actions {
    const val MOOSYNC_PACKAGE_NAME = "app.moosync.moosync"

    const val ACTION_TOGGLE_PAUSE = "$MOOSYNC_PACKAGE_NAME.togglepause"
    const val ACTION_PLAY = "$MOOSYNC_PACKAGE_NAME.play"
    const val ACTION_PLAY_PLAYLIST = "$MOOSYNC_PACKAGE_NAME.play.playlist"
    const val ACTION_PAUSE = "$MOOSYNC_PACKAGE_NAME.pause"
    const val ACTION_STOP = "$MOOSYNC_PACKAGE_NAME.stop"
    const val ACTION_SKIP = "$MOOSYNC_PACKAGE_NAME.skip"
    const val ACTION_REWIND = "$MOOSYNC_PACKAGE_NAME.rewind"
    const val ACTION_QUIT = "$MOOSYNC_PACKAGE_NAME.quitservice"
    const val ACTION_PENDING_QUIT = "$MOOSYNC_PACKAGE_NAME.pendingquitservice"
    const val ACTION_SHUFFLE = "$MOOSYNC_PACKAGE_NAME.shuffle"

    const val PLAYBACK_STATE_ACTIONS = (PlaybackStateCompat.ACTION_PLAY
    or PlaybackStateCompat.ACTION_PAUSE
    or PlaybackStateCompat.ACTION_PLAY_PAUSE
    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
    or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
    or PlaybackStateCompat.ACTION_STOP
    or PlaybackStateCompat.ACTION_SEEK_TO)

    const val NOTIFICATION_CHANNEL_ID = "now_playing_media"
    const val NOTIFICATION_ID = 0xb339

}

object BundleConstants {
    const val BUNDLE_SONG_ID_KEY = "songId"
}