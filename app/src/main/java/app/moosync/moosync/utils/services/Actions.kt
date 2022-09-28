package app.moosync.moosync.utils.services

import android.support.v4.media.session.PlaybackStateCompat

object Actions {
    const val MOOSYNC_PACKAGE_NAME = "app.moosync.moosync"

    const val ACTION_SHUFFLE = "$MOOSYNC_PACKAGE_NAME.shuffle"

    const val PLAYBACK_STATE_ACTIONS = (PlaybackStateCompat.ACTION_PLAY
    or PlaybackStateCompat.ACTION_PAUSE
    or PlaybackStateCompat.ACTION_PLAY_PAUSE
    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
    or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
    or PlaybackStateCompat.ACTION_STOP
    or PlaybackStateCompat.ACTION_SEEK_TO)
}