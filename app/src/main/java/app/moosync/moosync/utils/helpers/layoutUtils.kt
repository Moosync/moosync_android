package app.moosync.moosync.utils.helpers

import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener

fun View.onCreated(callback: () -> Unit) {
    this.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            callback.invoke()
            this@onCreated.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })
}