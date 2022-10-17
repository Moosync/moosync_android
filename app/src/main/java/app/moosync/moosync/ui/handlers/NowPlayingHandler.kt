package app.moosync.moosync.ui.handlers

import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import app.moosync.moosync.MainActivity
import app.moosync.moosync.R
import app.moosync.moosync.databinding.NowPlayingLayoutBinding
import app.moosync.moosync.glide.AudioCover
import app.moosync.moosync.glide.GlideApp
import app.moosync.moosync.ui.base.BaseFragment
import app.moosync.moosync.utils.helpers.toArtistString
import app.moosync.moosync.utils.helpers.toTimeString
import app.moosync.moosync.utils.models.Song
import app.moosync.moosync.utils.services.interfaces.MediaPlayerCallbacks
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.signature.MediaStoreSignature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NowPlayingHandler(private val mainActivity: MainActivity, private val nowPlayingLayoutBinding: NowPlayingLayoutBinding): BaseFragment() {

    private var isSeeking = false

    fun setupNowPlaying() {
        setNowPlayingCallbacks()
        setupButtons()
        nowPlayingInitialSetup()
    }

    private fun nowPlayingInitialSetup() {
        CoroutineScope(Dispatchers.Main).launch {
            val song = mainActivity.getMediaRemote().getCurrentSongAsync(this).await()
            if (song != null) {
                setNowPlayingDetails(song)
            }

            val repeat = mainActivity.getMediaRemote().getRepeatAsync(this).await()
            setRepeat(repeat)
        }
    }

    private fun setupButtons() {
        nowPlayingLayoutBinding.playPauseButton.setOnClickListener {
            mainActivity.getMediaControls()?.togglePlay()
        }

        nowPlayingLayoutBinding.shuffleButton.setOnClickListener {
            mainActivity.getMediaControls()?.shuffleQueue()
        }

        nowPlayingLayoutBinding.skipNext.setOnClickListener {
            mainActivity.getMediaControls()?.next()
        }

        nowPlayingLayoutBinding.skipPrev.setOnClickListener {
            mainActivity.getMediaControls()?.previous()
        }

        nowPlayingLayoutBinding.shuffleButton.setOnClickListener {
            mainActivity.getMediaControls()?.shuffleQueue()
        }

        nowPlayingLayoutBinding.repeatButton.setOnClickListener {
            mainActivity.getMediaControls()?.repeat()
        }

        nowPlayingLayoutBinding.seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (isSeeking) {
                    nowPlayingLayoutBinding.currentTime.text = p1.toTimeString()
                    mainActivity.getMediaControls()?.seek(p1)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                isSeeking = true
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                isSeeking = false
            }

        })
    }

    private fun setRepeat(repeat: Boolean) {
        // TODO: Set repeat button
    }

    private fun setNowPlayingDetails(currentSong: Song) {
        nowPlayingLayoutBinding.songTitle.text = currentSong.title
        nowPlayingLayoutBinding.songSubtitle.text = currentSong.artist?.toArtistString() ?: ""
        with(nowPlayingLayoutBinding.seekbar) {
            max = currentSong.duration.toInt()
            min = 0
            progress = 0
        }

        nowPlayingLayoutBinding.totalTime.text = currentSong.duration.toInt().toTimeString()
        nowPlayingLayoutBinding.currentTime.text = "0:00"

        GlideApp
            .with(nowPlayingLayoutBinding.root.context)
            .load(AudioCover(currentSong._id))
            .placeholder(R.drawable.songs)
            .transform(RoundedCorners(24))
            .signature(MediaStoreSignature("", currentSong.modified, 0))
            .into(nowPlayingLayoutBinding.coverImage)
    }

    private fun setNowPlayingCallbacks() {
        mainActivity.getMediaRemote().addMediaCallbacks(object: MediaPlayerCallbacks {
            override fun onSongChange(song: Song?) {
                if (song != null) {
                    setNowPlayingDetails(song)
                }
            }

            override fun onTimeChange(time: Int) {
                if (!isSeeking) {
                    nowPlayingLayoutBinding.seekbar.progress = time
                    nowPlayingLayoutBinding.currentTime.text = time.toTimeString()
                }
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