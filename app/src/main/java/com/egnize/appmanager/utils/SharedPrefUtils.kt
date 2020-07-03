package com.egnize.appmanager.utils
import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import java.lang.ref.WeakReference

object SharedPrefUtils {
    /**
     * save value in shared preferences.
     *
     * @param context Context of the Applications
     * @param key     Key of value to save against
     * @param value   Value to save
     */
    fun saveData(
        context: Context?,
        key: String?,
        value: Any
    ) {
        val weakReference =
            WeakReference(context)
        if (weakReference.get() != null) {
            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(weakReference.get())
            val editor = sharedPreferences.edit()
            when (value) {
                is Int -> {
                    editor.putInt(key, value)
                }
                is String -> {
                    editor.putString(key, value)
                }
                is Boolean -> {
                    editor.putBoolean(key, value)
                }
                is Long -> {
                    editor.putLong(key, value)
                }
                is Float -> {
                    editor.putFloat(key, value)
                }
                is Double -> {
                    editor.putLong(key, java.lang.Double.doubleToRawLongBits(value))
                }
            }
            editor.apply()
        }
    }

    /**
     * get value in from shared preferences.
     *
     * @param context      Context of the Applications
     * @param key          Key of value to save against
     * @param defaultValue Value return if no value found against key.
     */
    fun getData(
        context: Context?,
        key: String?,
        defaultValue: Any
    ): Any? {
        val weakReference =
            WeakReference(context)
        if (weakReference.get() != null) {
            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(weakReference.get())
            try {
                when (defaultValue) {
                    is Int -> {
                        return sharedPreferences.getInt(key, defaultValue)
                    }
                    is String -> {
                        return sharedPreferences.getString(key, defaultValue)
                    }
                    is Boolean -> {
                        return sharedPreferences.getBoolean(key, defaultValue)
                    }
                    is Long -> {
                        return sharedPreferences.getLong(key, defaultValue)
                    }
                    is Float -> {
                        return sharedPreferences.getFloat(key, defaultValue)
                    }
                    is Double -> {
                        return java.lang.Double.longBitsToDouble(sharedPreferences.getLong(key, java.lang.Double.doubleToRawLongBits(defaultValue)))
                    }
                }
            } catch (e: Exception) {
                Log.e("@SharedPrefUtils: ", e.message)
                return defaultValue
            }
        }
        return defaultValue
    }

    /**
     * remove saved Preferences from shared preferences.
     *
     * @param context Context of the Applications
     * @param key     Key of value to save against
     */
    fun removeFromPref(context: Context?, key: String?) {
        val weakReference =
            WeakReference(context)
        if (weakReference.get() != null) {
            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(weakReference.get())
            val editor = sharedPreferences.edit()
            editor.remove(key)
            editor.apply()
        }
    }
}