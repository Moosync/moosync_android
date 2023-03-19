package app.moosync.moosync.ui.base

import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.moosync.moosync.MainActivity
import app.moosync.moosync.R
import app.moosync.moosync.ui.views.ThemedLinearLayout
import app.moosync.moosync.ui.views.ThemedTextView
import app.moosync.moosync.ui.views.ThemedToolbar
import app.moosync.moosync.utils.MediaServiceRemote
import app.moosync.moosync.utils.helpers.margin
import app.moosync.moosync.utils.services.interfaces.MediaControls

enum class HeaderButtonTypes {
    SEARCH,
    IMAGE
}

data class HeaderButtons(var label: String, var drawable: Int, var onClick: OnClickListener? = null, var type: HeaderButtonTypes = HeaderButtonTypes.IMAGE)

abstract class BaseFragment: Fragment() {

    lateinit var rootView: View

    protected val TAG: String
        get() = javaClass.kotlin.simpleName ?: javaClass.kotlin.toString()

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

    fun setHeaderVisibility(visibility: Int) {
        if (!this::rootView.isInitialized) {
            throw Error("Root view not initialized")
        }
        val songListHeader = rootView.findViewById<View>(R.id.song_list_header)
        songListHeader.visibility = visibility
    }

    fun setHeaderButtons(vararg headerButtons: HeaderButtons) {
        if (!this::rootView.isInitialized) {
            throw Error("Root view not initialized")
        }
        val songListHeader = rootView.findViewById<ThemedLinearLayout>(R.id.song_list_header_button_group)

        for ((index, button) in headerButtons.withIndex()) {

            if (button.type === HeaderButtonTypes.IMAGE) {
                songListHeader.addView(createImageButton(button, index))
            }

            if (button.type === HeaderButtonTypes.SEARCH) {
                button.onClick = null
                val searchButton = createImageButton(button, index)

                searchButton.setOnClickListener {
                    val root = LinearLayout(requireContext())
                    root.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                    root.orientation = LinearLayout.HORIZONTAL

                    val editText = EditText(requireContext())
                    editText.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                    editText.maxLines = 1
                    editText.isSingleLine = true
                    root.addView(editText)

                    songListHeader.addView(root)
                }


                songListHeader.addView(searchButton)
            }
        }
    }

    private fun createImageButton(button: HeaderButtons, index: Int): View {
        val imageButton = ImageButton(requireContext())
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        imageButton.layoutParams = layoutParams
        imageButton.tooltipText = button.label
        imageButton.background = AppCompatResources.getDrawable(requireContext(), button.drawable)

        if (button.onClick !== null) imageButton.setOnClickListener(button.onClick)

        if (index != 0) {
            imageButton.margin(16f)
        }

        return imageButton
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