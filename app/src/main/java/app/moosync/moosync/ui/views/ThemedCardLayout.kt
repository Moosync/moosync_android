package app.moosync.moosync.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.findViewTreeLifecycleOwner
import app.moosync.moosync.R
import app.moosync.moosync.utils.helpers.ColorStyles
import app.moosync.moosync.utils.helpers.getColor
import app.moosync.moosync.utils.helpers.getColorStyle
import app.moosync.moosync.utils.helpers.observe
import com.google.android.material.card.MaterialCardView


class ThemedCardLayout(context: Context, attrs: AttributeSet?) : MaterialCardView(context, attrs) {

    private val colorStyle: ColorStyles

    init {
        strokeWidth = 0

//        setRippleColorResource(R.color.transparent)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ThemedCardLayout)
        colorStyle =
            attributes.getColorStyle(R.styleable.ThemedFrameLayout_themeStyle, ColorStyles.TRANSPARENT)
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

    fun getColor(): Int {
        return colorStyle.getColor()
    }

}