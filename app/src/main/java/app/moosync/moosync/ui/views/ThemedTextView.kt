package app.moosync.moosync.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.findViewTreeLifecycleOwner
import app.moosync.moosync.R
import app.moosync.moosync.utils.helpers.ColorStyles
import app.moosync.moosync.utils.helpers.getColor
import app.moosync.moosync.utils.helpers.getColorStyle
import app.moosync.moosync.utils.helpers.observe


class ThemedTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {

    private val colorStyle: ColorStyles

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ThemedTextView)
        colorStyle = attributes.getColorStyle(
            R.styleable.ThemedTextView_themeStyle,
            ColorStyles.TEXT_PRIMARY
        )
        setCustomColor(colorStyle)
        attributes.recycle()

    }

    private fun setCustomColor(colorStyle: ColorStyles) {
        setTextColor(colorStyle.getColor())
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