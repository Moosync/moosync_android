package app.moosync.moosync.ui.handlers

import android.animation.ObjectAnimator
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import app.moosync.moosync.MainActivity
import app.moosync.moosync.R
import app.moosync.moosync.databinding.NowPlayingMiniBarBinding
import app.moosync.moosync.glide.AudioCover
import app.moosync.moosync.glide.GlideApp
import app.moosync.moosync.utils.helpers.toArtistString
import app.moosync.moosync.utils.models.Song
import app.moosync.moosync.utils.services.interfaces.MediaPlayerCallbacks
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.signature.MediaStoreSignature
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.math.abs

class MiniBarPlayerHandler(private val mainActivity: MainActivity, private val miniBarBinding: NowPlayingMiniBarBinding, private val bottomSheetBehavior: BottomSheetBehavior<FrameLayout>, private val bottomSheetPeekHandler: (peek: Boolean, animate: Boolean) -> Unit) {

    fun setupMiniBar() {
        setMediaPlayerCallbacks()
        setupMiniPlayerSlideGestures {
            mainActivity.getMediaRemote()?.stopPlayback()
        }
        setMediaPlayerCallbacks()
        setupMiniPlayerButtons()
        miniBarInitialSetup()
    }

    private fun miniBarInitialSetup() {
        mainActivity.getMediaRemote()?.getCurrentSong { song ->
            if (song != null) {
                setMiniBarPlayerDetails(song)
            }
        }
    }

    private fun setupMiniPlayerButtons() {
        miniBarBinding.playPauseButton.setOnClickListener {
            mainActivity.getMediaRemote()?.togglePlay()
        }

        miniBarBinding.shuffleButton.setOnClickListener {
            mainActivity.getMediaRemote()?.shuffleQueue()
        }
    }

    private fun setMiniBarPlayerDetails(currentSong: Song) {
        miniBarBinding.songTitle.text = currentSong.title
        miniBarBinding.songSubtitle.text = currentSong.artist?.toArtistString() ?: ""
        with(miniBarBinding.seekbar) {
            max = currentSong.duration.toInt()
            min = 0
            progress = 0
        }

        GlideApp
            .with(miniBarBinding.root.context)
            .load(AudioCover(currentSong._id))
            .placeholder(R.drawable.songs)
            .transform(RoundedCorners(16))
            .signature(MediaStoreSignature("", currentSong.modified, 0))
            .into(miniBarBinding.coverImage)
    }

    private fun setMediaPlayerCallbacks() {
        mainActivity.getMediaRemote()?.addMediaCallbacks(object: MediaPlayerCallbacks {
            override fun onSongChange(songIndex: Int) {
                mainActivity.getMediaRemote()?.getCurrentSong() { currentSong ->
                    if (currentSong != null) {

                        setMiniBarPlayerDetails(currentSong)
                        bottomSheetPeekHandler(true, true)
                    }
                }
            }

            override fun onStop() {
                bottomSheetPeekHandler(false, true)
            }

            override fun onTimeChange(time: Int) {
                miniBarBinding.seekbar.progress = time
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
        val v1 = if (showPlay) miniBarBinding.pauseButton else miniBarBinding.playButton
        val v2 = if (showPlay) miniBarBinding.playButton else miniBarBinding.pauseButton

        v1.animate().alpha(0f).setDuration(300).start()
        v2.animate().alpha(1f).setDuration(300).start()

        v2.bringToFront()
    }

    private fun setupMiniPlayerSlideGestures(onEndCallback: () -> Unit) {
        miniBarBinding.miniPlayerContainer.setOnTouchListener(object:
            View.OnTouchListener {

            private var touchCoordinateX: Float = 0f
            private var touchCoordinateY: Float = 0f

            private val SWIPE_VELOCITY_THRESHOLD_Y = 2000

            private var isDismissing = false

            val width: Float
                get() = miniBarBinding.miniPlayerContainer.width.toFloat()

            val gestureDetector = GestureDetector(mainActivity, object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (abs(velocityY) > SWIPE_VELOCITY_THRESHOLD_Y) {
                        dismissDown()
                        return true
                    }
                    return false
                }
            })

            private fun onAnimationEnd() {
                bottomSheetPeekHandler(false, false)
                miniBarBinding.miniPlayerContainer.translationX = 0f
                miniBarBinding.miniPlayerContainer.translationY = 0f
                onEndCallback.invoke()
                isDismissing = false
            }

            private fun dismiss(left: Boolean = false) {
                isDismissing = true

                val translateX = width + 30
                ObjectAnimator.ofFloat(miniBarBinding.miniPlayerContainer, "translationX", if (left) -translateX else translateX).apply {
                    duration = 100
                    doOnEnd {
                        onAnimationEnd()
                    }
                    start()
                }
            }

            private fun dismissDown() {
                isDismissing = true

                val translateY = miniBarBinding.miniPlayerContainer.height.toFloat() + 30

                ObjectAnimator.ofFloat(miniBarBinding.miniPlayerContainer, "translationY", translateY).apply {
                    duration = 100
                    doOnEnd {
                        onAnimationEnd()
                    }
                    start()
                }
            }

            private fun animateToOriginalPos() {
                ObjectAnimator.ofFloat(
                    miniBarBinding.miniPlayerContainer, "translationX", 0f).apply {
                    duration = 100
                    start()
                }
            }

            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                if (p1 != null) {

                    if (p1.action == MotionEvent.ACTION_DOWN) {
                        touchCoordinateX = p1.x
                        touchCoordinateY = p1.y
                    }

                    if (p1.action == MotionEvent.ACTION_CANCEL) {
                        animateToOriginalPos()
                    }

                    if (p1.action == MotionEvent.ACTION_MOVE) {
                        miniBarBinding.miniPlayerContainer.translationX += p1.x - touchCoordinateX
                    }

                    if (p1.action == MotionEvent.ACTION_UP) {
                        if (p1.x == touchCoordinateX && p1.y == touchCoordinateY) {
                            p0?.performClick()
                        }

                        val movement = miniBarBinding.miniPlayerContainer.translationX

                        if (abs(movement) > width / 4) {
                            dismiss(movement <= 0)
                        } else {
                            animateToOriginalPos()
                        }
                    }

                    gestureDetector.onTouchEvent(p1)
                }

                return true
            }
        })

        miniBarBinding.miniPlayerContainer.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
}