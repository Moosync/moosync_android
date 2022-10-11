package app.moosync.moosync

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
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
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.signature.MediaStoreSignature
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.d("TAG", "onStateChanged: $newState")
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.themedBottomNavigationView.alpha = 1 - slideOffset
                binding.themedBottomNavigationView.translationY = binding.themedBottomNavigationView.height * slideOffset * (5/2)

                binding.bottomSheet.nowPlaying.nowPlayingContainer.alpha = slideOffset * 3
                binding.bottomSheet.miniPlayer.miniPlayerContainer.alpha = (1 - slideOffset * 3) // Should disappear before 0.32
            }
        })

        setMediaPlayerCallbacks()
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
                        .transform(CenterCrop(), RoundedCorners(16))
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
        })
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
