package app.moosync.moosync.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import androidx.lifecycle.findViewTreeLifecycleOwner
import app.moosync.moosync.R
import app.moosync.moosync.utils.helpers.ColorStyles
import app.moosync.moosync.utils.helpers.getColor
import app.moosync.moosync.utils.helpers.getColorStyle
import app.moosync.moosync.utils.helpers.observe


class ThemedCardLayout(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {

    private val colorStyle: ColorStyles

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ThemedCardLayout)
        colorStyle =
            attributes.getColorStyle(R.styleable.ThemedFrameLayout_themeStyle, ColorStyles.PRIMARY)
        setCustomColor(colorStyle)
        attributes.recycle()
    }

    private fun setCustomColor(colorStyle: ColorStyles) {
        setCardBackgroundColor(colorStyle.getColor())
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

    override fun performClick(): Boolean {
        return super.performClick()
    }
}