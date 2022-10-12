package app.moosync.moosync

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent.*
import android.view.View.OnTouchListener
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.animation.doOnEnd
import androidx.databinding.DataBindingUtil
import app.moosync.moosync.databinding.ActivityMainBinding
import app.moosync.moosync.glide.AudioCover
import app.moosync.moosync.glide.GlideApp
import app.moosync.moosync.ui.base.BaseMainActivity
import app.moosync.moosync.utils.db.repository.SongRepository
import app.moosync.moosync.utils.helpers.AudioScanner
import app.moosync.moosync.utils.helpers.PermissionManager
import app.moosync.moosync.utils.helpers.toArtistString
import app.moosync.moosync.utils.services.interfaces.MediaPlayerCallbacks
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.signature.MediaStoreSignature
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : BaseMainActivity() {

    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityMainBinding

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

//        setToolbar()
//        setNavigationDrawer()

        setupBottomSheet()

        PermissionManager(this).requestPermission {
            // TODO: Decide which scope would be better
            GlobalScope.launch(Dispatchers.IO) {
                val songs = AudioScanner().readDirectory(this@MainActivity)
                val repo = SongRepository(this@MainActivity)
                repo.insert(*songs.map { it.toDatabaseEntity() }.toTypedArray())

                Log.d("TAG", "onCreate: added songs")
            }
        }
    }

    private fun setupBottomSheet() {
        setBottomSheetPeek(peek = false, animate = false)

        val behaviour = BottomSheetBehavior.from(binding.bottomSheet.standardBottomSheet)
        behaviour.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.themedBottomNavigationView.alpha = 1 - slideOffset
                binding.themedBottomNavigationView.translationY = binding.themedBottomNavigationView.height * slideOffset * (5/2)

                binding.bottomSheet.nowPlaying.nowPlayingContainer.alpha = slideOffset * 3
                binding.bottomSheet.miniPlayer.miniPlayerContainer.alpha = (1 - slideOffset * 3) // Should disappear before 0.32
            }
        })

        setMediaPlayerCallbacks()
        setupMiniPlayerSlideGestures {
            getMediaRemote()?.stopPlayback()
        }

        setupMiniPlayerButtons()
    }

    private fun setupMiniPlayerButtons() {
        binding.bottomSheet.miniPlayer.playPauseButton.setOnClickListener {
            getMediaRemote()?.togglePlay()
        }

        binding.bottomSheet.miniPlayer.shuffleButton.setOnClickListener {
            getMediaRemote()?.shuffleQueue()
        }
    }

    private fun setupMiniPlayerSlideGestures(onEndCallback: () -> Unit) {
        binding.bottomSheet.miniPlayer.miniPlayerContainer.setOnTouchListener(object: OnTouchListener {

            private var touchCoordinateX: Float = 0f
            private var touchCoordinateY: Float = 0f

            private val SWIPE_VELOCITY_THRESHOLD_Y = 2000

            private var isDismissing = false

            val width: Float
            get() = binding.bottomSheet.miniPlayer.miniPlayerContainer.width.toFloat()

            val gestureDetector = GestureDetector(this@MainActivity, object : SimpleOnGestureListener() {
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
                setBottomSheetPeek(peek = false, animate = false)
                binding.bottomSheet.miniPlayer.miniPlayerContainer.translationX = 0f
                binding.bottomSheet.miniPlayer.miniPlayerContainer.translationY = 0f
                onEndCallback.invoke()
                isDismissing = false
            }

            private fun dismiss(left: Boolean = false) {
                isDismissing = true

                val translateX = width + 30
                ObjectAnimator.ofFloat(binding.bottomSheet.miniPlayer.miniPlayerContainer, "translationX", if (left) -translateX else translateX).apply {
                    duration = 100
                    doOnEnd {
                        onAnimationEnd()
                    }
                    start()
                }
            }

            private fun dismissDown() {
                isDismissing = true

                val translateY = binding.bottomSheet.miniPlayer.miniPlayerContainer.height.toFloat() + 30

                ObjectAnimator.ofFloat(binding.bottomSheet.miniPlayer.miniPlayerContainer, "translationY", translateY).apply {
                    duration = 100
                    doOnEnd {
                        onAnimationEnd()
                    }
                    start()
                }
            }

            private fun animateToOriginalPos() {
                ObjectAnimator.ofFloat(
                    binding.bottomSheet.miniPlayer.miniPlayerContainer, "translationX", 0f).apply {
                    duration = 100
                    start()
                }
            }

            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                if (p1 != null) {

                    if (p1.action == ACTION_DOWN) {
                        touchCoordinateX = p1.x
                        touchCoordinateY = p1.y
                    }

                    if (p1.action == ACTION_CANCEL) {
                        animateToOriginalPos()
                    }

                    if (p1.action == ACTION_MOVE) {
                        binding.bottomSheet.miniPlayer.miniPlayerContainer.translationX += p1.x - touchCoordinateX
                    }

                    if (p1.action == ACTION_UP) {
                        if (p1.x == touchCoordinateX && p1.y == touchCoordinateY) {
                            p0?.performClick()
                        }

                        val movement = binding.bottomSheet.miniPlayer.miniPlayerContainer.translationX

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

        binding.bottomSheet.miniPlayer.miniPlayerContainer.setOnClickListener {
            val behaviour = BottomSheetBehavior.from(binding.bottomSheet.standardBottomSheet)
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun setMediaPlayerCallbacks() {
        getMediaRemote()?.addMediaCallbacks(object: MediaPlayerCallbacks {
            override fun onSongChange(songIndex: Int) {
                val currentSong = getMediaRemote()?.getCurrentSong()
                if (currentSong != null) {

                    binding.bottomSheet.miniPlayer.songTitle.text = currentSong.title
                    binding.bottomSheet.miniPlayer.songSubtitle.text = currentSong.artist?.toArtistString() ?: ""
                    with(binding.bottomSheet.miniPlayer.seekbar) {
                        max = currentSong.duration.toInt()
                        min = 0
                        progress = 0
                    }

                    GlideApp
                        .with(binding.root.context)
                        .load(AudioCover(currentSong._id))
                        .placeholder(R.drawable.songs)
                        .transform(RoundedCorners(16))
                        .signature(MediaStoreSignature("", currentSong.modified, 0))
                        .into(binding.bottomSheet.miniPlayer.coverImage)

                    setBottomSheetPeek(true)
                }
            }

            override fun onStop() {
                setBottomSheetPeek(false)
            }

            override fun onTimeChange(time: Int) {
                binding.bottomSheet.miniPlayer.seekbar.progress = time
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
        val v1 = if (showPlay) binding.bottomSheet.miniPlayer.pauseButton else binding.bottomSheet.miniPlayer.playButton
        val v2 = if (showPlay) binding.bottomSheet.miniPlayer.playButton else binding.bottomSheet.miniPlayer.pauseButton

        v1.animate().alpha(0f).setDuration(300).start()
        v2.animate().alpha(1f).setDuration(300).start()

        v2.bringToFront()
    }

    private fun setBottomSheetPeek(peek: Boolean, animate: Boolean = true) {
        val behaviour = BottomSheetBehavior.from(binding.bottomSheet.standardBottomSheet)

        if (peek) {
            ObjectAnimator.ofInt(behaviour, "peekHeight", binding.themedBottomNavigationView.height + binding.bottomSheet.miniPlayer.miniPlayerContainer.height + 20).apply {
                duration = if (animate) 300 else 0
                start()
            }
        } else {
            ObjectAnimator.ofInt(behaviour, "peekHeight", 0).apply {
                duration = if (animate) 300 else 0
                start()
            }
        }
    }

//    private fun setToolbar() {
//        setSupportActionBar(binding.appBarMain.toolbar)
//
//        with(supportActionBar!!) {
//            setDisplayHomeAsUpEnabled(true)
//        }
//    }
//
//    private fun setNavigationDrawer() {
//        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.nav_open, R.string.nav_close)
//        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)
//        actionBarDrawerToggle.syncState()
//    }

    override fun onDestroy() {
        Log.d("TAG", "onDestroy: Destroying main activity")
        getMediaRemote()?.release()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
