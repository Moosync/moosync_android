package app.moosync.moosync

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import app.moosync.moosync.databinding.ActivityMainBinding
import app.moosync.moosync.ui.base.BaseMainActivity
import app.moosync.moosync.utils.db.repository.SongRepository
import app.moosync.moosync.utils.helpers.AudioScanner
import app.moosync.moosync.utils.helpers.PermissionManager
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
        val behaviour = BottomSheetBehavior.from(binding.bottomSheet.standardBottomSheet)


        binding.themedBottomNavigationView.viewTreeObserver.addOnGlobalLayoutListener {
            behaviour.peekHeight = binding.themedBottomNavigationView.height + binding.bottomSheet.miniPlayer.miniPlayerContainer.height + 20
        }

            behaviour.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.d("TAG", "onStateChanged: $newState")
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.themedBottomNavigationView.alpha = 1 - slideOffset
                binding.themedBottomNavigationView.translationY = binding.themedBottomNavigationView.height * slideOffset * (5/2)

                binding.bottomSheet.nowPlaying.nowPlayingContainer.alpha = slideOffset * slideOffset // Should start being visible at 0.32 * 0.32 ~= 0.1
                binding.bottomSheet.miniPlayer.miniPlayerContainer.alpha = (1 - slideOffset * 3) // Should disappear before 0.32
            }
        })
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
