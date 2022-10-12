package app.moosync.moosync

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.MotionEvent.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import app.moosync.moosync.databinding.ActivityMainBinding
import app.moosync.moosync.ui.base.BaseMainActivity
import app.moosync.moosync.ui.handlers.BottomSheetHandler
import app.moosync.moosync.ui.handlers.MiniBarPlayerHandler
import app.moosync.moosync.utils.db.repository.SongRepository
import app.moosync.moosync.utils.helpers.AudioScanner
import app.moosync.moosync.utils.helpers.PermissionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
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

        setupOverlayPlayers()

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

    private fun setupOverlayPlayers() {
        val behaviour = BottomSheetBehavior.from(binding.bottomSheet.standardBottomSheet)
        val bottomSheetHandler = BottomSheetHandler(binding.bottomSheet, binding.themedBottomNavigationView)
        bottomSheetHandler.setupBottomSheet()
        MiniBarPlayerHandler(this, binding.bottomSheet.miniPlayer, behaviour, bottomSheetHandler::setBottomSheetPeek).setupMiniBar()
    }

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
