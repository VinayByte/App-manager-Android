package com.egnize.chineseapps.utils

import android.util.Log
import com.egnize.appmanager.BuildConfig.DEBUG

class Logs {
    companion object {
        private const val LOG_PREFIX = "@eg_"

        fun d(tag: String?, msg: String?) {
            if (DEBUG) Log.d(LOG_PREFIX + tag, msg)
        }

        fun v(tag: String?, msg: String?) {
            if (DEBUG) Log.v(LOG_PREFIX + tag, msg)
        }

        fun i(tag: String?, msg: String?) {
            if (DEBUG) Log.i(LOG_PREFIX + tag, msg)
        }

        fun w(tag: String?, msg: String?) {
            if (DEBUG) Log.w(LOG_PREFIX + tag, msg)
        }

        fun e(tag: String?, msg: String?, t: Throwable) {
            if (DEBUG) Log.e(LOG_PREFIX + tag, msg, t)
        }

        private fun getTag(): String? {
            val traceElement = Throwable().stackTrace[2]
            val fileName = traceElement.fileName ?: return LOG_PREFIX
            return fileName.split("[.]").toTypedArray()[0]
        }
    }
}