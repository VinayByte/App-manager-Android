package com.egnize.appmanager.models

import android.graphics.drawable.Drawable
import java.util.*

class App(val name: String,
          val path: String,
          val packageName: String,
          val icon: Drawable,
          val isSystemApp: Boolean,
          val installedDate: Date) {
    var isSelected = false
    var isVisible = true

}