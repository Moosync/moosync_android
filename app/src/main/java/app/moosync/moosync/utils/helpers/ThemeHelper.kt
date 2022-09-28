package app.moosync.moosync.utils.helpers

import android.content.res.TypedArray
import android.util.Log
import androidx.lifecycle.MutableLiveData

enum class ColorStyles(val value: Int) {
    ACCENT(0),
    PRIMARY(1),
    SECONDARY(2),
    TERTIARY(3),
    TEXT_PRIMARY(4),
    TEXT_SECONDARY(5),
    TEXT_INVERSE(6),
    DIVIDER(7);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}

object ThemeHelper {
    val accent = MutableLiveData((0xFF65CB88).toInt())
    val primary = MutableLiveData((0xFF212121).toInt())
    val secondary = MutableLiveData((0xFF282828).toInt())
    val tertiary = MutableLiveData((0xFF151515).toInt())
    val textPrimary = MutableLiveData((0xFFFFFFFF).toInt())
    val textSecondary = MutableLiveData((0xFF565656).toInt())
    val textInverse = MutableLiveData((0xFF000000).toInt())
    val divider = MutableLiveData((0xFFA0A0A0).toInt())

    fun parseStyleFromName(name: ColorStyles): MutableLiveData<Int> {
        Log.d("TAG", "parseStyleFromName: $name")
        return when(name) {
            ColorStyles.PRIMARY -> primary
            ColorStyles.ACCENT-> accent
            ColorStyles.SECONDARY -> secondary
            ColorStyles.TERTIARY -> tertiary
            ColorStyles.TEXT_PRIMARY -> textPrimary
            ColorStyles.TEXT_SECONDARY -> textSecondary
            ColorStyles.TEXT_INVERSE -> textInverse
            ColorStyles.DIVIDER -> divider
        }
    }
}

fun TypedArray.getColorStyle(styleable: Int, defValue: ColorStyles): MutableLiveData<Int> {
    val style = ColorStyles.fromInt(getInt(styleable, defValue.value))
    return ThemeHelper.parseStyleFromName(style)
}