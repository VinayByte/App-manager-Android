package com.egnize.appmanager

import android.app.Application
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatDelegate
import com.egnize.chineseapps.utils.setActiveContext
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pixplicity.easyprefs.library.Prefs

class App : Application() {
    private lateinit var appExecutors: AppExecutors
    private lateinit var db: FirebaseFirestore

    override fun onCreate() {
        super.onCreate()
        setActiveContext(activity = true)
        appExecutors = AppExecutors()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Library EasyPrefs
        Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(BuildConfig.APPLICATION_ID)
                .setUseDefaultSharedPreference(true)
                .build()
        setSavedNightMode()
    }

    val dataRepository: DataRepository?
        get() = DataRepository.getDataRepository(applicationContext, appExecutors)

    private fun setSavedNightMode() {
        val modeSave = Prefs.getInt(Constants.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(modeSave)
    }

//    fun getDataRepository(): DataRepository? {
////        return DataRepository.getDataRepository(applicationContext, appExecutors, db)
//        return DataRepository.getDataRepository(applicationContext, appExecutors)
//
//    }
}