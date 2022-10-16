package app.moosync.moosync.ui.handlers

import android.animation.ObjectAnimator
import android.view.View
import app.moosync.moosync.MainActivity
import app.moosync.moosync.databinding.BottomSheetLayoutBinding
import app.moosync.moosync.ui.views.ThemedBottomNavigationView
import app.moosync.moosync.utils.helpers.onCreated
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BottomSheetHandler(private val mainActivity: MainActivity, private val bottomSheetBinding: BottomSheetLayoutBinding, private val themedBottomNavigationView: ThemedBottomNavigationView) {
    private val bottomSheetBehavior
    get() = BottomSheetBehavior.from(bottomSheetBinding.standardBottomSheet)

    fun setupBottomSheet() {
        initialBottomSheetSetup()
        setupBottomSheetBehaviour()
    }

    private fun initialBottomSheetSetup() {
        CoroutineScope(Dispatchers.Main).launch {
            val song = mainActivity.getMediaRemote()?.getCurrentSongAsync(this)?.await()
            if (song != null) {
                setBottomSheetPeek(peek = true, animate = true)
            } else {
                setBottomSheetPeek(peek = false, animate = false)
            }
        }
    }

    private fun setupBottomSheetBehaviour() {
        val behaviour = BottomSheetBehavior.from(bottomSheetBinding.standardBottomSheet)
        behaviour.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                themedBottomNavigationView.alpha = 1 - slideOffset
                themedBottomNavigationView.translationY =
                    themedBottomNavigationView.height * slideOffset * (5 / 2)

                bottomSheetBinding.nowPlaying.nowPlayingContainer.alpha = slideOffset * 3
                bottomSheetBinding.miniPlayer.miniPlayerContainer.alpha =
                    (1 - slideOffset * 3) // Should disappear before 0.32
            }
        })
    }

    fun setBottomSheetPeek(peek: Boolean, animate: Boolean = true) {
        bottomSheetBinding.miniPlayer.miniPlayerContainer.onCreated {
            if (peek) {
                ObjectAnimator.ofInt(bottomSheetBehavior, "peekHeight", themedBottomNavigationView.height + bottomSheetBinding.miniPlayer.miniPlayerContainer.height + 20).apply {
                    duration = if (animate) 300 else 0
                    start()
                }
            } else {
                ObjectAnimator.ofInt(bottomSheetBehavior, "peekHeight", 0).apply {
                    duration = if (animate) 300 else 0
                    start()
                }
            }
        }
    }
}