package app.moosync.moosync.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.lifecycle.findViewTreeLifecycleOwner
import app.moosync.moosync.utils.helpers.ColorStyles
import app.moosync.moosync.utils.helpers.getColor
import app.moosync.moosync.utils.helpers.observe


class ThemedProgressBar(context: Context, attrs: AttributeSet?) : ProgressBar(context, attrs) {

    private fun setCustomColor() {
        progressTintList = ColorStateList.valueOf(ColorStyles.ACCENT.getColor())
    }

    override fun onAttachedToWindow() {
        val lifecycleOwner = findViewTreeLifecycleOwner()
        if (lifecycleOwner != null) {
            ColorStyles.ACCENT.observe(lifecycleOwner) {
                setCustomColor()
            }
        }
        super.onAttachedToWindow()
    }
}