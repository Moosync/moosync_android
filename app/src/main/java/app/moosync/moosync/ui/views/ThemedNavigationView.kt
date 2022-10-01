package app.moosync.moosync.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.findViewTreeLifecycleOwner
import app.moosync.moosync.R
import app.moosync.moosync.utils.helpers.ColorStyles
import app.moosync.moosync.utils.helpers.getColorStyle
import com.google.android.material.navigation.NavigationView

class ThemedNavigationView(context: Context, attrs: AttributeSet?): NavigationView(context, attrs) {
    private val colorStyle: MutableLiveData<Int>

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ThemedNavigationView)
        colorStyle = attributes.getColorStyle(R.styleable.ThemedNavigationView_themeStyle, ColorStyles.PRIMARY)
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