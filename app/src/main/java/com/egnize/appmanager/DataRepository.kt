package com.egnize.appmanager

import android.content.Context
import com.egnize.appmanager.services.LoadApps
import com.egnize.appmanager.services.RootManager
import com.google.firebase.firestore.FirebaseFirestore

class DataRepository private constructor(context: Context, appExecutors: AppExecutors, db: FirebaseFirestore) {
    val loadApps: LoadApps
    val rootManager: RootManager


    companion object {
        private var dataRepository: DataRepository? = null
        fun getDataRepository(context: Context, appExecutors: AppExecutors, db: FirebaseFirestore): DataRepository? {
            if (dataRepository == null) {
                synchronized(DataRepository::class.java) { if (dataRepository == null) dataRepository = DataRepository(context, appExecutors, db) }
            }
            return dataRepository
        }
    }

    init {
        loadApps = LoadApps(context, appExecutors, db)
        rootManager = RootManager(appExecutors)
    }
}