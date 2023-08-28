package app.moosync.moosync.utils.helpers

import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await

fun <T>get(json: Class<T>, baseUrl: String, path: String, query: Map<String, Any> = emptyMap(), headers: Map<String, String> = emptyMap()): Deferred<T> {
    return CoroutineScope(Dispatchers.Default).async {
        val uriBuilder = Uri.Builder()
            .scheme("https")
            .authority(baseUrl)

        val pathSplit = path.split("/")
        for (p in pathSplit) {
            uriBuilder.appendPath(p)
        }

        for (q in query) {
            uriBuilder.appendQueryParameter(q.key, q.value.toString())
        }

        val client = OkHttpClient()
        val headersParsed = Headers.Builder()
        for (h in headers) {
            headersParsed.add(h.key, h.value)
        }

        Log.d("TAG", "get: Sending GET request to ${uriBuilder.build()}")

        val request =
            Request.Builder().url(uriBuilder.build().toString()).headers(headersParsed.build())
                .get().build()
        val response = client.newCall(request).await()

        val gson = Gson()
        gson.fromJson(response.body?.string(), json)
    }
}

fun <T>postForm(json: Class<T>, baseUrl: String, path: String, body: Map<String, Any> = emptyMap(), headers: Map<String, String> = emptyMap()): Deferred<T> {
    return CoroutineScope(Dispatchers.Default).async {
        val uriBuilder = Uri.Builder()
            .scheme("https")
            .authority(baseUrl)

        val pathSplit = path.split("/")
        for (p in pathSplit) {
            uriBuilder.appendPath(p)
        }

        val formBody = FormBody.Builder()
        for (b in body) {
            formBody.add(b.key, b.value.toString())
        }

        val client = OkHttpClient()
        val headersParsed = Headers.Builder().set("Content-Type", "application/x-www-form-urlencoded")
        for (h in headers) {
            headersParsed.add(h.key, h.value)
        }

        Log.d("TAG", "postForm: Sending POST request to ${uriBuilder.build()}")

        val request =
            Request.Builder().url(uriBuilder.build().toString()).headers(headersParsed.build())
                .post(formBody.build()).build()
        val response = client.newCall(request).await()

        val gson = Gson()
        gson.fromJson(response.body?.string(), json)
    }
}