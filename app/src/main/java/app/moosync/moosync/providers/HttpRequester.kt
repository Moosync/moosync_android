package app.moosync.moosync.providers

import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException

class HttpRequester {

    companion object {
        suspend fun getAsync(url: String): ResponseBody? {
            return suspendCancellableCoroutine {
                val client = OkHttpClient()
                val request = Request.Builder().method("GET", null).url(url).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        it.resumeWith(Result.failure(e))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        it.resumeWith(Result.success(response.body))
                    }
                })
            }
        }
    }
}