package app.moosync.moosync.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.findViewTreeLifecycleOwner
import app.moosync.moosync.R
import app.moosync.moosync.utils.helpers.ColorStyles
import app.moosync.moosync.utils.helpers.getColorStyle


class ThemedLinearLayout(context: Context, attrs: AttributeSet?) :
    LinearLayoutCompat(context, attrs) {

    private val colorStyle: MutableLiveData<Int>

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ThemedLinearLayout)
        colorStyle =
            attributes.getColorStyle(R.styleable.ThemedLinearLayout_themeStyle, ColorStyles.PRIMARY)
        setCustomColor()
        attributes.recycle()
    }

    private fun setCustomColor() {
        setBackgroundColor(colorStyle.value!!.toInt())
    }

    override fun onAttachedToWindow() {
        val lifecycleOwner = findViewTreeLifecycleOwner()
        if (lifecycleOwner != null) {
            colorStyle.observe(lifecycleOwner) {
                setBackgroundColor(it.toInt())
            }
        }
        super.onAttachedToWindow()
    }
}