package app.moosync.moosync.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.findViewTreeLifecycleOwner
import app.moosync.moosync.utils.helpers.ColorStyles
import app.moosync.moosync.utils.helpers.getColor
import app.moosync.moosync.utils.helpers.observe


class ThemedToolbar(context: Context, attrs: AttributeSet?) : Toolbar(context, attrs) {

    private val colorStyle: ColorStyles = ColorStyles.PRIMARY

    init {
        setCustomColor(colorStyle)
    }

    private fun setCustomColor(colorStyle: ColorStyles) {
        setBackgroundColor(colorStyle.getColor())
        setTitleTextColor(ColorStyles.TEXT_PRIMARY.getColor())
    }

    override fun onAttachedToWindow() {
        val lifecycleOwner = findViewTreeLifecycleOwner()
        if (lifecycleOwner != null) {
            colorStyle.observe(lifecycleOwner) {
                setCustomColor(it)
            }
        }
        super.onAttachedToWindow()
    }
}