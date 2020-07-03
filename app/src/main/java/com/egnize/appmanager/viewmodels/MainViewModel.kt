package com.egnize.appmanager.viewmodels

import android.app.Application
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
import com.egnize.appmanager.helpers.SingleLiveEvent
import com.egnize.appmanager.models.App
import com.egnize.appmanager.models.RootState
import com.egnize.appmanager.services.LoadApps
import com.egnize.appmanager.services.RootManager
import com.egnize.chineseapps.utils.Response
import java.util.*

class MainViewModel(application: Application, private val rootManager: RootManager?) : BaseViewModel(application) {
    private var uninstallUserAppsResult = SingleLiveEvent<Boolean>()


    class Factory(private val application: Application?, private val rootManager: RootManager?) : NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T?>): T {
            return MainViewModel(application!!, rootManager) as T
        }

    }

    fun reloadAppsList() {
        val loadApps = loadApps
        loadApps.searchInstalledApps()
    }

    val installedApps: MutableLiveData<MutableList<App>>
        get() {
            val loadApps = loadApps
            return loadApps!!.installedApps
        }
    val firestoreDataFetched: LiveData<Boolean>
        get() {
            val loadApps = loadApps
            return loadApps.firestoreDataFetched
        }
    val installedChineseApps: MutableLiveData<MutableList<App>>
        get() {
            val loadApps = loadApps
            return loadApps!!.installedChineseApps
        }
    val firestoreChineseApps: MutableLiveData<MutableList<Response>>
        get() {
            val loadApps = loadApps
            return loadApps!!.firestoreChineseApps
        }

    private val loadApps: LoadApps
        private get() {
            val uninstallSystemApps = getApplication<com.egnize.appmanager.App>()
            val dataRepository = uninstallSystemApps.dataRepository
            return dataRepository!!.loadApps
        }

    fun orderAppForInstallationDateDesc(installedApps: MutableList<App>): MutableList<App> {
        // TODO: Check if I can remove this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            installedApps!!.sortWith(Comparator { o1: App?, o2: App? -> o2!!.installedDate.compareTo(o1!!.installedDate) })
        }
        return installedApps!!
    }

    fun orderAppInAlfabeticalOrder(installedApps: MutableList<App>): MutableList<App> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            installedApps.sortWith(Comparator { o1: App?, o2: App? -> o1!!.name.compareTo(o2!!.name) })
        }
        return installedApps
    }

    fun hideSystemApps(installedApps: MutableList<App>): MutableList<App> {
        for (app in installedApps) {
            val isUserApp = !app.isSystemApp
            if (isUserApp) app.isVisible = true else app.isVisible = false
        }
        return installedApps
    }

    fun showChineseApps(installedApps: List<App>): MutableList<App> {
        val chineseAppsList = firestoreChineseApps.value!!
        var installedChineseAppList = mutableListOf<App>()
        if (chineseAppsList.isNotEmpty()) {
            val aColIds = chineseAppsList.map { it.pkg }.toSet()
            installedChineseAppList = installedApps.filter { it.packageName in aColIds } as MutableList<App>
        }
        return installedChineseAppList
    }

    fun hideUserApps(installedApps: MutableList<App>): MutableList<App> {
        for (app in installedApps) {
            val isSystemApp = app.isSystemApp
            if (isSystemApp) app.isVisible = true else app.isVisible = false
        }
        return installedApps
    }

    fun showAllApps(installedApps: MutableList<App>?): MutableList<App> {
        for (app in installedApps!!) {
            app.isVisible = true
        }
        return installedApps
    }

    fun uncheckedAllApps(installedApps: MutableList<App>?): MutableList<App> {
        for (app in installedApps!!) {
            app.isSelected = false
        }
        return installedApps
    }

    fun filterApps(query: String?, installedApp: MutableList<App>): MutableList<App> {
        val filteredApps: MutableList<App> = ArrayList()
        return if (query!!.isEmpty()) installedApp else {
            for (app in installedApp!!) {
                if (app!!.name.toLowerCase().contains(query)) filteredApps.add(app)
            }
            filteredApps
        }
    }


    fun removeApps(installedApps: MutableList<App>): SingleLiveEvent<Boolean> {
        val selectedApps = getSelectedApps(installedApps)
        rootManager!!.removeApps(selectedApps)
        return rootManager.getUninstallResult()
    }

    fun getUninstallResult(): SingleLiveEvent<Boolean> {
        return uninstallUserAppsResult
    }

    fun checkRootPermission(): RootState? {
        val hasRootedPermission = rootManager!!.hasRootedPermision()
        if (hasRootedPermission) return RootState.HAVE_ROOT
        val wasRooted = rootManager.wasRooted()
        return if (wasRooted) RootState.BE_ROOT else RootState.NO_ROOT
    }

    fun getSelectedApps(installedApps: List<App>?): MutableList<App> {
        val selectedApps: MutableList<App> = ArrayList()
        for (app in installedApps!!) {
            if (app!!.isVisible) if (app.isSelected) selectedApps.add(app)
        }
        return selectedApps
    }

    fun atLeastAnAppIsSelected(installedApps: MutableList<App>): Boolean {
        for (app in installedApps!!) {
            if (app!!.isVisible) if (app.isSelected) return true
        }
        return false
    }

    fun rebootDevice() {
        rootManager!!.rebootDevice()
    }

}