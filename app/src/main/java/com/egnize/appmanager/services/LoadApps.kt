/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Nicola Serlonghi <nicolaserlonghi@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.egnize.appmanager.services

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData
import com.egnize.appmanager.AppExecutors
import com.egnize.appmanager.Constants
import com.egnize.appmanager.models.App
import com.google.firebase.perf.FirebasePerformance
import java.util.*

class LoadApps(private val context: Context, private val appExecutors: AppExecutors) {
    val installedApps = MutableLiveData<MutableList<App>>()
    private var installedAppList: MutableList<App> = ArrayList()
    private var packageManager: PackageManager? = null
    fun searchInstalledApps() {
        appExecutors.diskIO().execute {

            // Firebase Monitoring Performance
            val searchAppsTrace = FirebasePerformance.getInstance().newTrace(Constants.FIREBASE_PERFORMANCE_TRACE)
            searchAppsTrace.start()
            installedAppList = ArrayList()
            val installedApplicationsInfo = getInstalledApplication(context)
            appDetails(installedApplicationsInfo)
            updateInstalledApps()
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

}