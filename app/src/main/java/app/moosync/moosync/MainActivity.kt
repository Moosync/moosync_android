package app.moosync.moosync

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import app.moosync.moosync.databinding.ActivityMainBinding
import app.moosync.moosync.ui.base.BaseMainActivity
import app.moosync.moosync.ui.handlers.BottomSheetHandler
import app.moosync.moosync.ui.handlers.MiniBarPlayerHandler
import app.moosync.moosync.ui.handlers.NowPlayingHandler
import app.moosync.moosync.utils.db.repository.SongRepository
import app.moosync.moosync.utils.helpers.AudioScanner
import app.moosync.moosync.utils.helpers.DeeplinkHandler
import app.moosync.moosync.utils.helpers.PermissionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : BaseMainActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setupOverlayPlayers()

        CoroutineScope(Dispatchers.Main).launch {
            val providers = providerStore.getAllProviders()
            for (p in providers) {
                p.login().await()
            }
        }

        PermissionManager(this).requestPermission {
            CoroutineScope(Dispatchers.IO).launch {
                val songs = AudioScanner().readDirectory(this@MainActivity)
                val repo = SongRepository(this@MainActivity)
                repo.insert(*songs.map { it.toDatabaseEntity() }.toTypedArray())

                Log.d(TAG, "onCreate: added songs")
            }
        }

        val navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        binding.themedBottomNavigationView.setupWithNavController(navController)
    }

    private fun setupOverlayPlayers() {
        val behaviour = BottomSheetBehavior.from(binding.bottomSheet.standardBottomSheet)
        val bottomSheetHandler =
            BottomSheetHandler(this, binding.bottomSheet, binding.themedBottomNavigationView)
        bottomSheetHandler.setupBottomSheet()
        MiniBarPlayerHandler(
            this,
            binding.bottomSheet.miniPlayer,
            behaviour,
            bottomSheetHandler::setBottomSheetPeek
        ).setupMiniBar()
        NowPlayingHandler(this, binding.bottomSheet.nowPlaying).setupNowPlaying()
    }

    override fun onDestroy() {
        Log.d("TAG", "onDestroy: Destroying main activity")
        getMediaRemote().release()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onCreate: ${intent?.action} ${intent?.data}")
        val data = intent?.data
        if (data !== null) {
            DeeplinkHandler.triggerCallback(data)
        }
    }
}
