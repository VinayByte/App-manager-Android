package com.egnize.chineseapps.utils

import android.content.Context
import java.lang.ref.WeakReference

/**
 * Created by VINAY on 2020-02-21.
 * v6kr@outlook.com
 */
private var appContext: WeakReference<Context?> = WeakReference(null)
private var activityContext: WeakReference<Context?> = WeakReference(null)

@Synchronized
fun getActiveContext(activity: Boolean = false): Context? {
    return when {
        activityContext.get() != null -> activityContext.get()
        !activity && appContext.get() != null -> appContext.get()
        else -> {
            throw Exception("No context set (activity: $activity)")
        }
    }
}

@Synchronized
fun Context.setActiveContext(activity: Boolean = false) {
    if (activity) activityContext = WeakReference(this@setActiveContext)
    else appContext = WeakReference(this@setActiveContext)
}

@Synchronized
fun unsetActiveContext() {
    activityContext = WeakReference(null)
}