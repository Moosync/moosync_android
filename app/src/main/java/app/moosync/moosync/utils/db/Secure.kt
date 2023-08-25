package app.moosync.moosync.utils.db

import android.content.Context
import android.content.Context.MODE_PRIVATE

import android.content.SharedPreferences




// TODO: Use keystore or something
class Secure(private val context: Context) {
    fun set(key: String, value: String) {
        val pref: SharedPreferences =
            context.getSharedPreferences("SecureStorage", MODE_PRIVATE)
        val editor = pref.edit()

        editor.putString(key, value)
        editor.apply()
    }

    fun set(key: String, value: Long) {
        val pref: SharedPreferences =
            context.getSharedPreferences("SecureStorage", MODE_PRIVATE)
        val editor = pref.edit()

        editor.putLong(key, value)
        editor.apply()
    }

    fun get(key: String): String? {
        val pref: SharedPreferences =
            context.getSharedPreferences("SecureStorage", MODE_PRIVATE)
        return pref.getString(key, null)
    }

    fun getLong(key: String): Long {
        val pref: SharedPreferences =
            context.getSharedPreferences("SecureStorage", MODE_PRIVATE)
        return pref.getLong(key, 0)
    }
}