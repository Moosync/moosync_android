package app.moosync.moosync.ui.base

import androidx.fragment.app.Fragment
import app.moosync.moosync.MainActivity
import app.moosync.moosync.utils.MediaServiceRemote

abstract class BaseFragment: Fragment() {
    fun getMediaRemote(): MediaServiceRemote? {
        val activity = requireActivity()
        if (activity is MainActivity) {
            return activity.getMediaRemote()
        }

        return null
    }
}