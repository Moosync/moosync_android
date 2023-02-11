package app.moosync.moosync.ui.base

import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.moosync.moosync.MainActivity
import app.moosync.moosync.R
import app.moosync.moosync.ui.views.ThemedTextView
import app.moosync.moosync.ui.views.ThemedToolbar
import app.moosync.moosync.utils.MediaServiceRemote
import app.moosync.moosync.utils.services.interfaces.MediaControls

abstract class BaseFragment: Fragment() {
    fun getMediaRemote(): MediaServiceRemote? {
        val activity = requireActivity()
        if (activity is MainActivity) {
            return activity.getMediaRemote()
        }

        return null
    }

    fun getMediaControls(): MediaControls? {
        return this.getMediaRemote()?.controls
    }

    fun setToolbar(view: View) {
        val toolbar = view.findViewById<ThemedToolbar>(R.id.toolbar)
        (activity as MainActivity).setSupportActionBar(toolbar)
        val label = findNavController().currentDestination?.label
        if (!label.isNullOrEmpty()) {
            view.findViewById<ThemedTextView>(R.id.song_list_title)?.text = label
        }

    }
}