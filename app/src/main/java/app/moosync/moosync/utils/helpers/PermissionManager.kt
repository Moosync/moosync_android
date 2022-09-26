package app.moosync.moosync.utils.helpers

import android.Manifest.permission.*
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PermissionManager(private val activity: AppCompatActivity) {
    fun requestPermission(onSuccess: (() -> Unit)?) {
        val permissionArr = arrayListOf<String>()
        if (Build.VERSION.SDK_INT >= 33) {
            permissionArr.add(READ_MEDIA_AUDIO)
            permissionArr.add((READ_MEDIA_IMAGES))
        } else {
            permissionArr.add(READ_EXTERNAL_STORAGE)
        }

        val launcher = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            for (e in it.entries) {
                if (e.value) {
                    onSuccess?.invoke()
                } else {
                    Toast.makeText(activity, "Please grant permission for ${e.key}", Toast.LENGTH_LONG).show()
                }
            }
        }

        launcher.launch(permissionArr.toTypedArray())
    }
}