package app.moosync.moosync

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class DeepLinkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("TAG", "onCreate: ${intent?.action} ${intent?.data}")
    }
}