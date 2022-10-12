package app.moosync.moosync.utils.helpers

import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener

fun View.onCreated(callback: () -> Unit) {
    if (this.height != 0 && this.width != 0) {
        callback.invoke()
    } else {
        this.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                this@onCreated.viewTreeObserver.removeOnGlobalLayoutListener(this)
                callback.invoke()
            }
        })
    }
}