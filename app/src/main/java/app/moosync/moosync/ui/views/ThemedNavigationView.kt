package app.moosync.moosync.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.util.AttributeSet
import android.util.TypedValue
import androidx.lifecycle.findViewTreeLifecycleOwner
import app.moosync.moosync.utils.helpers.ColorStyles
import app.moosync.moosync.utils.helpers.getColor
import app.moosync.moosync.utils.helpers.getTransparent
import app.moosync.moosync.utils.helpers.observe
import com.google.android.material.navigation.NavigationView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class ThemedNavigationView(context: Context, attrs: AttributeSet?): NavigationView(context, attrs) {

    private val dependOnColors:Array<ColorStyles> = arrayOf(ColorStyles.PRIMARY, ColorStyles.TEXT_PRIMARY, ColorStyles.ACCENT, ColorStyles.TEXT_SECONDARY)

    private fun setCustomColor() {
        setBackgroundColor(ColorStyles.PRIMARY.getColor())

        val states = arrayOf(
            arrayOf(android.R.attr.state_checked).toIntArray(),
            arrayOf(android.R.attr.state_enabled).toIntArray(),
            arrayOf(-android.R.attr.state_enabled).toIntArray(), // Disabled
            arrayOf(android.R.attr.fillColor).toIntArray(),
        )

        val stateColors = arrayOf(
            ColorStyles.ACCENT.getColor(),
            ColorStyles.TEXT_PRIMARY.getColor(),
            ColorStyles.TEXT_SECONDARY.getColor(),
            ColorStyles.ACCENT.getColor(),
        ).toIntArray()

        val colorStateList = ColorStateList(states, stateColors)
        itemTextColor = colorStateList
        itemIconTintList = colorStateList

        itemBackground = getDrawable()
    }

    // Creates an background drawable for menu items
    private fun getDrawable(): Drawable {
        // 8 dp rounded corners
        val roundedCorner = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics)

        // By default fill size matches parent
        val materialShapeDrawable = MaterialShapeDrawable(
            ShapeAppearanceModel
                .Builder()
                .setAllCorners(CornerFamily.ROUNDED, roundedCorner)
                .build()
        )

        val states = arrayOf(
            arrayOf(-android.R.attr.state_checked).toIntArray(),
            arrayOf(android.R.attr.state_checked).toIntArray(),
        )

        val stateColors = arrayOf(
            Color.TRANSPARENT,
            ColorStyles.ACCENT.getTransparent(0x20), // ~= 12.5%
        ).toIntArray()

        materialShapeDrawable.fillColor = ColorStateList(states, stateColors)

        return InsetDrawable(materialShapeDrawable, 30, 15, 30, 15)
    }

    override fun onAttachedToWindow() {
        val lifecycleOwner = findViewTreeLifecycleOwner()
        if (lifecycleOwner != null) {
            dependOnColors.forEach {
                it.observe(lifecycleOwner) {
                    setCustomColor()
                }
            }
        }

        super.onAttachedToWindow()
    }
}