package app.moosync.moosync.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import app.moosync.moosync.R
import app.moosync.moosync.utils.helpers.ColorStyles
import app.moosync.moosync.utils.helpers.getColor
import app.moosync.moosync.utils.helpers.getColorStyle
import app.moosync.moosync.utils.helpers.observe


class ThemedLinearLayout(context: Context, attrs: AttributeSet?) :
    LinearLayoutCompat(context, attrs) {

    private val colorStyle: ColorStyles

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ThemedLinearLayout)
        colorStyle =
            attributes.getColorStyle(R.styleable.ThemedLinearLayout_themeStyle, ColorStyles.PRIMARY)
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