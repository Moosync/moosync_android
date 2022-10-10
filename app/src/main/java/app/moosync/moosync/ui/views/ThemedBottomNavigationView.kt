package app.moosync.moosync.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.lifecycle.findViewTreeLifecycleOwner
import app.moosync.moosync.utils.helpers.ColorStyles
import app.moosync.moosync.utils.helpers.getColor
import app.moosync.moosync.utils.helpers.getTransparent
import app.moosync.moosync.utils.helpers.observe
import com.google.android.material.bottomnavigation.BottomNavigationView

class ThemedBottomNavigationView(context: Context, attrs: AttributeSet?) : BottomNavigationView(context, attrs) {
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

        val itemBackgroundStates = arrayOf(
            arrayOf(android.R.attr.state_enabled).toIntArray(),
        )
        val itemBackgroundStateColors = arrayOf(
            ColorStyles.ACCENT.getTransparent(0x25),
        ).toIntArray()

        itemActiveIndicatorColor = ColorStateList(itemBackgroundStates, itemBackgroundStateColors)
    }

//    private fun getDrawable(): Drawable {
//        // 8 dp rounded corners
//        val roundedCorner = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics)
//
//        // By default fill size matches parent
//        val materialShapeDrawable = ShapeAppearanceModel
//                .Builder()
//                .setAllCorners(CornerFamily.ROUNDED, roundedCorner)
//                .build()
//
//
//        val states = arrayOf(
//            arrayOf(-android.R.attr.state_checked).toIntArray(),
//            arrayOf(android.R.attr.state_checked).toIntArray(),
//        )
//
//        val stateColors = arrayOf(
//            Color.TRANSPARENT,
//            ColorStyles.ACCENT.getTransparent(0x20), // ~= 12.5%
//        ).toIntArray()
//
////        materialShapeDrawable. = ColorStateList(states, stateColors)
//
//        return InsetDrawable(materialShapeDrawable, 30, 15, 30, 15)
//    }

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