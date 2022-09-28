package app.moosync.moosync.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.findViewTreeLifecycleOwner
import app.moosync.moosync.R
import app.moosync.moosync.utils.helpers.ColorStyles
import app.moosync.moosync.utils.helpers.getColorStyle


class ThemedTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {

    private val colorStyle: MutableLiveData<Int>

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ThemedTextView)
        colorStyle = attributes.getColorStyle(
            R.styleable.ThemedTextView_themeStyle,
            ColorStyles.TEXT_PRIMARY
        )
        setCustomColor()
        attributes.recycle()

    }

    private fun setCustomColor() {
        setTextColor(colorStyle.value!!.toInt())
    }

    override fun onAttachedToWindow() {
        val lifecycleOwner = findViewTreeLifecycleOwner()
        if (lifecycleOwner != null) {
            colorStyle.observe(lifecycleOwner) {
                setTextColor(it.toInt())
            }
        }
        super.onAttachedToWindow()
    }
}