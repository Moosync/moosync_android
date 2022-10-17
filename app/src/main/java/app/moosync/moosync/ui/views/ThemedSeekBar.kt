package app.moosync.moosync.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.lifecycle.findViewTreeLifecycleOwner
import app.moosync.moosync.utils.helpers.ColorStyles
import app.moosync.moosync.utils.helpers.observe

class ThemedSeekBar(context: Context, attrs: AttributeSet?) : AppCompatSeekBar(context, attrs) {

    private fun setCustomColor() {
//        progressTintList = ColorStateList.valueOf(ColorStyles.ACCENT.getColor())
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