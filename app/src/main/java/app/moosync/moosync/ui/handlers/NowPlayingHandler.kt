package app.moosync.moosync.ui.handlers

import app.moosync.moosync.MainActivity
import app.moosync.moosync.R
import app.moosync.moosync.databinding.NowPlayingLayoutBinding
import app.moosync.moosync.glide.AudioCover
import app.moosync.moosync.glide.GlideApp
import app.moosync.moosync.ui.base.BaseFragment
import app.moosync.moosync.utils.helpers.toArtistString
import app.moosync.moosync.utils.models.Song
import app.moosync.moosync.utils.services.interfaces.MediaPlayerCallbacks
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.signature.MediaStoreSignature

class NowPlayingHandler(private val mainActivity: MainActivity, private val nowPlayingLayoutBinding: NowPlayingLayoutBinding): BaseFragment() {

    fun setupNowPlaying() {
        setNowPlayingCallbacks()
        setupMiniPlayerButtons()
        nowPlayingInitialSetup()
    }

    private fun nowPlayingInitialSetup() {
        mainActivity.getMediaRemote()?.getCurrentSong { song ->
            if (song != null) {
                setNowPlayingDetails(song)
            }
        }
    }

    private fun setupMiniPlayerButtons() {
        nowPlayingLayoutBinding.playPauseButton.setOnClickListener {
            mainActivity.getMediaRemote()?.togglePlay()
        }

        nowPlayingLayoutBinding.shuffleButton.setOnClickListener {
            mainActivity.getMediaRemote()?.shuffleQueue()
        }
    }

    private fun setNowPlayingDetails(currentSong: Song) {
        nowPlayingLayoutBinding.songTitle.text = currentSong.title
        nowPlayingLayoutBinding.songSubtitle.text = currentSong.artist?.toArtistString() ?: ""
        with(nowPlayingLayoutBinding.seekbar) {
            max = currentSong.duration.toInt()
            min = 0
            progress = 0
        }

        GlideApp
            .with(nowPlayingLayoutBinding.root.context)
            .load(AudioCover(currentSong._id))
            .placeholder(R.drawable.songs)
            .transform(RoundedCorners(24))
            .signature(MediaStoreSignature("", currentSong.modified, 0))
            .into(nowPlayingLayoutBinding.coverImage)
    }

    private fun setNowPlayingCallbacks() {
        mainActivity.getMediaRemote()?.addMediaCallbacks(object: MediaPlayerCallbacks {
            override fun onSongChange(songIndex: Int) {
                mainActivity.getMediaRemote()?.getCurrentSong { currentSong ->
                    if (currentSong != null) {
                        setNowPlayingDetails(currentSong)
                    }
                }
            }

            override fun onTimeChange(time: Int) {
                nowPlayingLayoutBinding.seekbar.progress = time
            }

            override fun onPlay() {
                loadPlayPauseDrawable(false)
            }

            override fun onPause() {
                loadPlayPauseDrawable(true)
            }
        })
    }

    private fun loadPlayPauseDrawable(showPlay: Boolean) {
        val v1 = if (showPlay) nowPlayingLayoutBinding.pauseButton else nowPlayingLayoutBinding.playButton
        val v2 = if (showPlay) nowPlayingLayoutBinding.playButton else nowPlayingLayoutBinding.pauseButton

        v1.animate().alpha(0f).setDuration(300).start()
        v2.animate().alpha(1f).setDuration(300).start()

        v2.bringToFront()
    }
}