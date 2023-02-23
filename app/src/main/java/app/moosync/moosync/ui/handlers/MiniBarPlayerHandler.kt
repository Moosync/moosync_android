package app.moosync.moosync.ui.handlers

import android.animation.ObjectAnimator
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import app.moosync.moosync.MainActivity
import app.moosync.moosync.R
import app.moosync.moosync.databinding.NowPlayingMiniBarBinding
import app.moosync.moosync.glide.GlideApp
import app.moosync.moosync.utils.helpers.toArtistString
import app.moosync.moosync.utils.models.Song
import app.moosync.moosync.utils.services.interfaces.MediaPlayerCallbacks
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.signature.MediaStoreSignature
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

class MiniBarPlayerHandler(private val mainActivity: MainActivity, private val miniBarBinding: NowPlayingMiniBarBinding, private val bottomSheetBehavior: BottomSheetBehavior<FrameLayout>, private val bottomSheetPeekHandler: (peek: Boolean, animate: Boolean) -> Unit) {

    private var circularSongsArray: Array<Song?> = arrayOf()
    private var currentDisplayIndex: Int = 1


    fun setupMiniBar() {
        setMediaPlayerCallbacks()
        setupMiniPlayerSlideGestures {
            mainActivity.getMediaControls()?.stop()
        }
        setMediaPlayerCallbacks()
        setupMiniPlayerButtons()
        miniBarInitialSetup()
    }

    private fun miniBarInitialSetup() {
        CoroutineScope(Dispatchers.Main).launch {
            val song = mainActivity.getMediaRemote().getCurrentSongAsync(this).await()
            if (song != null) {
                setMiniBarPlayerDetails(song)
            }

            this@MiniBarPlayerHandler.circularSongsArray = mainActivity.getMediaRemote().getCircularSongsAsync().await()
        }
    }

    private fun setupMiniPlayerButtons() {
        miniBarBinding.playPauseButton.setOnClickListener {
            mainActivity.getMediaControls()?.togglePlay()
        }

        miniBarBinding.shuffleButton.setOnClickListener {
            mainActivity.getMediaControls()?.shuffleQueue()
        }
    }

    private fun setMiniBarPlayerDetails(currentSong: Song) {
        miniBarBinding.songTitle.text = currentSong.title
        miniBarBinding.songTitle.isSelected = true
        miniBarBinding.songSubtitle.text = currentSong.artist?.toArtistString() ?: ""
        with(miniBarBinding.seekbar) {
            max = currentSong.duration.toInt()
            min = 0
            progress = 0
        }

        GlideApp
            .with(miniBarBinding.root.context)
            .load(currentSong.coverImage)
            .placeholder(R.drawable.songs)
            .transform(CenterCrop(), RoundedCorners(16))
            .signature(MediaStoreSignature("", currentSong.modified, 0))
            .into(miniBarBinding.coverImage)
    }

    private fun setMediaPlayerCallbacks() {
        mainActivity.getMediaRemote().addMediaCallbacks(object: MediaPlayerCallbacks {
            override fun onSongChange(song: Song?) {
                if (song != null) {
                    setMiniBarPlayerDetails(song)
                    bottomSheetPeekHandler(true, true)

                    CoroutineScope(Dispatchers.Default).launch {
                        this@MiniBarPlayerHandler.circularSongsArray = mainActivity.getMediaRemote().getCircularSongsAsync().await()
                        currentDisplayIndex = 1
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
        miniBarBinding.miniPlayerContainer.setOnTouchListener(object: OnTouchListener {
            private var touchCoordinateX: Float = 0f
            private var touchCoordinateY: Float = 0f

            private var isDismissing = false

            val height: Float
            get() = miniBarBinding.miniPlayerContainer.height.toFloat()

            private fun onAnimationEnd() {
                bottomSheetPeekHandler(false, false)
                miniBarBinding.miniPlayerContainer.translationX = 0f
                miniBarBinding.miniPlayerContainer.translationY = 0f
                onEndCallback.invoke()
                isDismissing = false
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
                    miniBarBinding.miniPlayerContainer, "translationY", 0f).apply {
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
                        miniBarBinding.miniPlayerContainer.translationY =
                            0f.coerceAtLeast(miniBarBinding.miniPlayerContainer.translationY + (p1.y - touchCoordinateY))
                    }

                    if (p1.action == MotionEvent.ACTION_UP) {
                        if (p1.x == touchCoordinateX && p1.y == touchCoordinateY) {
                            p0?.performClick()
                        }

                        val movement = miniBarBinding.miniPlayerContainer.translationY

                        if (abs(movement) > (height * 2/3)) {
                            dismissDown()
                        } else {
                            animateToOriginalPos()
                        }
                    }
                }

                return true
            }
        })

        miniBarBinding.textContainer.setOnTouchListener(object : OnTouchListener {
            private var touchCoordinateX: Float = 0f
            private var touchCoordinateY: Float = 0f

            val width: Float
                get() = miniBarBinding.textContainer.width.toFloat()

            private fun animateToOriginalPos() {
                ObjectAnimator.ofFloat(
                    miniBarBinding.textContainer, "translationX", 0f).apply {
                    duration = 100
                    start()
                }

                ObjectAnimator.ofFloat(
                    miniBarBinding.textContainer, "alpha", 1f).apply {
                    duration = 100
                    start()
                }

                onOtherSide = false
            }

            private var onOtherSide = false


            private fun setSongDetails(index: Int) {
                miniBarBinding.songTitle.text = circularSongsArray[index]?.title ?: ""
                miniBarBinding.songSubtitle.text = circularSongsArray[index]?.artist?.toArtistString() ?: ""

                currentDisplayIndex = index
            }

            private fun isNotSingleSongInQueue(): Boolean {
                return circularSongsArray.filterNotNull().size > 1
            }

            private fun setNextSong() {
                var nextIndex = currentDisplayIndex + 1
                if (nextIndex >= circularSongsArray.filterNotNull().size) nextIndex = 0

                setSongDetails(nextIndex)
            }

            private fun setPrevSong() {
                var prevIndex = currentDisplayIndex - 1
                if (prevIndex < 0) prevIndex = circularSongsArray.filterNotNull().size - 1

                setSongDetails(prevIndex)
            }


            override fun onTouch(p0: View, p1: MotionEvent): Boolean {
                if (p1.action == MotionEvent.ACTION_DOWN) {
                    touchCoordinateX = p1.x
                    touchCoordinateY = p1.y
                }

                if (p1.action == MotionEvent.ACTION_CANCEL) {
                    animateToOriginalPos()
                }

                if (isNotSingleSongInQueue()) {
                    if (p1.action == MotionEvent.ACTION_MOVE) {
                        miniBarBinding.textContainer.translationX += p1.x - touchCoordinateX

                        val swipeRightThreshold = width * 2/3
                        val shiftOffsetSwipeRight = width / 3
                        val touchCoordXOffsetSwipeRight = shiftOffsetSwipeRight + abs(miniBarBinding.textContainer.translationX)

                        val swipeLeftThreshold = width / 3
                        val shiftOffsetSwipeLeft = width
                        val touchCoordXOffsetSwipeLeft = shiftOffsetSwipeLeft + abs(miniBarBinding.textContainer.translationX)

                        if (!onOtherSide) {
                            if (miniBarBinding.textContainer.translationX > 0) {
                                miniBarBinding.textContainer.alpha = abs(1 - abs(miniBarBinding.textContainer.translationX / swipeRightThreshold))
                            } else {
                                miniBarBinding.textContainer.alpha = abs(1 - abs(miniBarBinding.textContainer.translationX / swipeLeftThreshold))
                            }
                        } else {
                            if (miniBarBinding.textContainer.translationX <= 0) {
                                miniBarBinding.textContainer.alpha = abs(1 - abs(miniBarBinding.textContainer.translationX / swipeLeftThreshold))
                            } else {
                                miniBarBinding.textContainer.alpha = abs(1 - abs(miniBarBinding.textContainer.translationX / shiftOffsetSwipeLeft))
                            }
                        }


                        if (!onOtherSide) {
                            if (miniBarBinding.textContainer.translationX > 0) {
                                if (abs(miniBarBinding.textContainer.translationX) > swipeRightThreshold) {
                                    setPrevSong()

                                    touchCoordinateX += touchCoordXOffsetSwipeRight
                                    miniBarBinding.textContainer.translationX = -shiftOffsetSwipeRight
                                    onOtherSide = true
                                }

                            } else if (miniBarBinding.textContainer.translationX < 0) {
                                if (abs(miniBarBinding.textContainer.translationX) > swipeLeftThreshold) {

                                    setNextSong()

                                    touchCoordinateX -= touchCoordXOffsetSwipeLeft
                                    miniBarBinding.textContainer.translationX = shiftOffsetSwipeLeft
                                    onOtherSide = true
                                }
                            }
                        }

                        if (onOtherSide) {
                            if (miniBarBinding.textContainer.translationX < 0) {
                                if (abs(miniBarBinding.textContainer.translationX) > shiftOffsetSwipeRight) {
                                    onOtherSide = false
                                }

                            } else if (miniBarBinding.textContainer.translationX > 0) {
                                if (abs(miniBarBinding.textContainer.translationX) > shiftOffsetSwipeLeft) {
                                    onOtherSide = false
                                }
                            }
                        }
                    }
                }

                if (p1.action == MotionEvent.ACTION_UP) {
                    animateToOriginalPos()

                    if (p1.x == touchCoordinateX && p1.y == touchCoordinateY) {
                        p0.performClick()
                    }

                    if (currentDisplayIndex != 1) {
                        if (currentDisplayIndex > 1) {
                            mainActivity.getMediaControls()?.next()
                        } else {
                            mainActivity.getMediaControls()?.previous()
                        }
                    }

                }

                return true
            }
        })

        miniBarBinding.miniPlayerContainer.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
}