package app.moosync.moosync.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.moosync.moosync.utils.helpers.ColorStyles
import app.moosync.moosync.utils.helpers.getColor
import app.moosync.moosync.utils.helpers.observe

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(ColorStyles.PRIMARY)
        ColorStyles.PRIMARY.observe(this) {
            setStatusBarColor(it)
        }
    }

    private fun setStatusBarColor(colorStyles: ColorStyles) {
        window.statusBarColor = colorStyles.getColor()
    }
}