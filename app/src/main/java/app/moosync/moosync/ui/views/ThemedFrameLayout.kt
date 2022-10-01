package app.moosync.moosync.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.lifecycle.findViewTreeLifecycleOwner
import app.moosync.moosync.R
import app.moosync.moosync.utils.helpers.ColorStyles
import app.moosync.moosync.utils.helpers.getColor
import app.moosync.moosync.utils.helpers.getColorStyle
import app.moosync.moosync.utils.helpers.observe


class ThemedFrameLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val colorStyle: ColorStyles

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ThemedFrameLayout)
        colorStyle =
            attributes.getColorStyle(R.styleable.ThemedFrameLayout_themeStyle, ColorStyles.PRIMARY)
        setCustomColor(colorStyle)
        attributes.recycle()
    }

    private fun setCustomColor(colorStyle: ColorStyles) {
        setBackgroundColor(colorStyle.getColor())
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