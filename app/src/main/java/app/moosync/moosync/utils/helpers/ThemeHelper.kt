package app.moosync.moosync.utils.helpers

import android.content.res.TypedArray
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer


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
    private val accent = MutableLiveData((0xFF65CB88).toInt())
    private val primary = MutableLiveData((0xFF212121).toInt())
    private val secondary = MutableLiveData((0xFF282828).toInt())
    private val tertiary = MutableLiveData((0xFF151515).toInt())
    private val textPrimary = MutableLiveData((0xFFFFFFFF).toInt())
    private val textSecondary = MutableLiveData((0xFF565656).toInt())
    private val textInverse = MutableLiveData((0xFF000000).toInt())
    private val divider = MutableLiveData((0xFFA0A0A0).toInt())

    fun parseStyleFromName(name: ColorStyles): MutableLiveData<Int> {
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

//    init {
//        val h = android.os.Handler(Looper.getMainLooper())
//        Timer().scheduleAtFixedRate(object: TimerTask() {
//            override fun run() {
//                h.post {
//                    primary.value = getRandomColor()
//                    secondary.value = getRandomColor()
//                    tertiary.value = getRandomColor()
//                    textPrimary.value = getRandomColor()
//                    textSecondary.value = getRandomColor()
//                    accent.value = getRandomColor()
//                }
//            }
//        }, 0, 1000)
//    }
//
//    private fun getRandomColor(): Int {
//        val rand = Random()
//        val r = rand.nextFloat()
//        val g = rand.nextFloat()
//        val b = rand.nextFloat()
//        return Color.rgb(r, g, b)
//    }
}

fun TypedArray.getColorStyle(styleable: Int, defValue: ColorStyles): ColorStyles {
    return ColorStyles.fromInt(getInt(styleable, defValue.value))
}

fun ColorStyles.getColor(): Int {
    return ThemeHelper.parseStyleFromName(this).value!!
}

fun ColorStyles.observe(lifecycle: LifecycleOwner, observer: Observer<ColorStyles>) {
    ThemeHelper.parseStyleFromName(this).observe(lifecycle) {
        observer.onChanged(this)
    }
}

fun ColorStyles.getTransparent(alpha: Int): Int {
    return ColorUtils.setAlphaComponent(this.getColor(), alpha)
}