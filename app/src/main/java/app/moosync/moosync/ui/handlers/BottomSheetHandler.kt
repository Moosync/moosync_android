package app.moosync.moosync.ui.handlers

import android.animation.ObjectAnimator
import android.view.View
import app.moosync.moosync.databinding.BottomSheetLayoutBinding
import app.moosync.moosync.ui.views.ThemedBottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior

class BottomSheetHandler(private val bottomSheetBinding: BottomSheetLayoutBinding, private val themedBottomNavigationView: ThemedBottomNavigationView) {
    fun setupBottomSheet() {
        setupBottomSheetBehaviour()
    }

    private fun setupBottomSheetBehaviour() {
        setBottomSheetPeek(peek = false, animate = false)

        val behaviour = BottomSheetBehavior.from(bottomSheetBinding.standardBottomSheet)
        behaviour.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                themedBottomNavigationView.alpha = 1 - slideOffset
                themedBottomNavigationView.translationY = themedBottomNavigationView.height * slideOffset * (5/2)

                bottomSheetBinding.nowPlaying.nowPlayingContainer.alpha = slideOffset * 3
                bottomSheetBinding.miniPlayer.miniPlayerContainer.alpha = (1 - slideOffset * 3) // Should disappear before 0.32
            }
        })
    }

    fun setBottomSheetPeek(peek: Boolean, animate: Boolean = true) {
        val behaviour = BottomSheetBehavior.from(bottomSheetBinding.standardBottomSheet)

        if (peek) {
            ObjectAnimator.ofInt(behaviour, "peekHeight", themedBottomNavigationView.height + bottomSheetBinding.miniPlayer.miniPlayerContainer.height + 20).apply {
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
}