package com.egnize.appmanager.services

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.egnize.appmanager.AppExecutors
import com.egnize.appmanager.BuildConfig
import com.egnize.appmanager.Constants
import com.egnize.appmanager.models.App
import com.egnize.chineseapps.utils.Logs
import com.egnize.chineseapps.utils.Response
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.perf.FirebasePerformance
import java.util.*

class LoadApps(private val context: Context, private val appExecutors: AppExecutors, private val db: FirebaseFirestore) {
    val installedApps = MutableLiveData<MutableList<App>>()
    private var installedAppList: MutableList<App> = ArrayList()
    private var packageManager: PackageManager? = null
    val firestoreDataFetched = MutableLiveData<Boolean>()
    val chineseAppsList: MutableList<Response> = mutableListOf()
    val firestoreChineseApps = MutableLiveData<MutableList<Response>>()

    val installedChineseApps = MutableLiveData<MutableList<App>>()
    private var installedChineseAppList: List<App> = ArrayList()

    fun searchInstalledApps() {
        appExecutors.diskIO().execute {

            // Firebase Monitoring Performance
            val searchAppsTrace = FirebasePerformance.getInstance().newTrace(Constants.FIREBASE_PERFORMANCE_TRACE)
            searchAppsTrace.start()
            installedAppList = ArrayList()
            val installedApplicationsInfo = getInstalledApplication(context)
            appDetails(installedApplicationsInfo)
            updateInstalledApps()
            getFireStoreData()
            searchAppsTrace.stop()
        }
    }

    private fun getInstalledApplication(context: Context): List<ApplicationInfo> {
        getPackageManager(context)
        return packageManager!!.getInstalledApplications(0)
    }

    private fun getPackageManager(context: Context) {
        packageManager = context.packageManager
    }

    private fun appDetails(installedApplicationsInfo: List<ApplicationInfo>) {
        for (applicationInfo in installedApplicationsInfo) {
            createApp(applicationInfo)
        }
    }

    private fun createApp(applicationInfo: ApplicationInfo) {
        val systemApp = isSystemApps(applicationInfo)
        val label = getApplicationLabel(applicationInfo)
        val sourceDir = getApplicationSourceDir(applicationInfo)
        val packageName = getApplicationPackageName(applicationInfo)
        val icon = getAppliactionIcon(applicationInfo)
        val installedDate = getInstalledDate(packageName)
        if (packageName == BuildConfig.APPLICATION_ID){
            return
        }
        val app = App(
                label,
                sourceDir,
                packageName,
                icon,
                systemApp,
                installedDate
        )
        addAppToArrayList(app)
    }

    private fun isSystemApps(applicationInfo: ApplicationInfo): Boolean {
        val systemApp: Boolean
        systemApp = if (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 1) true else false
        return systemApp
    }

    private fun getApplicationLabel(applicationInfo: ApplicationInfo): String {
        return packageManager!!.getApplicationLabel(applicationInfo) as String
    }

    private fun getApplicationSourceDir(applicationInfo: ApplicationInfo): String {
        return applicationInfo.sourceDir
    }

    private fun getApplicationPackageName(applicationInfo: ApplicationInfo): String {
        return applicationInfo.packageName
    }

    private fun getAppliactionIcon(applicationInfo: ApplicationInfo): Drawable {
        val applicationIcon: Drawable
        applicationIcon = try {
            packageManager!!.getApplicationIcon(applicationInfo.processName)
        } catch (e: PackageManager.NameNotFoundException) {
            defaultApplicationIcon
        }
        return applicationIcon
    }

    private val defaultApplicationIcon: Drawable
        private get() = packageManager!!.defaultActivityIcon

    private fun getInstalledDate(packageName: String): Date {
        var installDate: Long? = null
        installDate = try {
            packageManager!!.getPackageInfo(packageName, 0).firstInstallTime
        } catch (e: PackageManager.NameNotFoundException) {
            Calendar.getInstance().timeInMillis
        }
        return Date(installDate)
    }

    private fun addAppToArrayList(app: App) {
        installedAppList.add(app)
    }

    private fun updateInstalledApps() {
        installedApps.postValue(installedAppList)
    }

    fun getFireStoreData() {
        val docRef = db.collection("data").document("app_list")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("@", "Listen failed.", e)
                firestoreDataFetched.postValue(false)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                chineseAppsList.clear()
                for (item in snapshot.data?.getValue("list") as ArrayList<HashMap<String, String>>) {
                    chineseAppsList.add(Response(item["name"], item["pkg"]))
                }
                Logs.d("Firestore: ", "Current data: ${snapshot.data}")
                filteredChineseApps()
                firestoreChineseApps.postValue(chineseAppsList)
                firestoreDataFetched.postValue(true)
            } else {
                Log.d("Firestore: ", "Current data: null")
                firestoreDataFetched.postValue(false)

            }
        }
    }

    private fun filteredChineseApps() {
        if (chineseAppsList.isNotEmpty()) {
            val aColIds = chineseAppsList.map { it.pkg }.toSet()
            installedChineseAppList = installedAppList.filter { it.packageName in aColIds }
            installedChineseApps.postValue(installedChineseAppList as MutableList<App>)
        }
    }

}