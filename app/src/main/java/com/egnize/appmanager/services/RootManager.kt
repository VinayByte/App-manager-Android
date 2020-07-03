package com.egnize.appmanager.services

import com.egnize.appmanager.AppExecutors
import com.egnize.appmanager.helpers.SingleLiveEvent
import com.egnize.appmanager.models.App
import eu.chainfire.libsuperuser.Shell
import eu.chainfire.libsuperuser.Shell.ShellDiedException
import java.io.File
import java.util.*

class RootManager(private val appExecutors: AppExecutors) {
    private val SU_BINARY_DIRS = arrayOf(
            "/system/bin",
            "/system/sbin",
            "/system/xbin",
            "/vendor/bin",
            "/sbin")
    private var uninstallResult = SingleLiveEvent<Boolean>()

    fun hasRootedPermision(): Boolean {
        return Shell.SU.available()
    }

    fun wasRooted(): Boolean {
        var hasRooted = false
        for (path in SU_BINARY_DIRS) {
            val su = File("$path/su")
            if (su.exists()) {
                hasRooted = true
                break
            } else {
                hasRooted = false
            }
        }
        return hasRooted
    }

    fun removeApps(appsToRemove: MutableList<App>?) {
        appExecutors.diskIO().execute {
            var uninstalledNoProblems = true
            for (app in appsToRemove!!) {
                var result = true
                if (app!!.isSystemApp) {
                    result = uninstallSystemApp(app.path)
                    if (!result) result = uninstallSystemAppAlternativeMethod(app.packageName)
                } else result = uninstallUserApp(app.packageName)
                if (!result) uninstalledNoProblems = false
            }
            uninstallResult.postValue(uninstalledNoProblems)
        }
    }

    private fun uninstallSystemApp(appApk: String): Boolean {
        executeCommandSU("mount -o rw,remount /system")
        executeCommandSU("rm $appApk")
        executeCommandSU("mount -o ro,remount /system")
        return checkUninstallSuccessful(appApk)
    }

    private fun uninstallSystemAppAlternativeMethod(packageName: String): Boolean {
        val commandOutput = executeCommandSU("pm uninstall --user 0 $packageName")
        return checkPMCommandSuccesfull(commandOutput)
    }

    private fun uninstallUserApp(packageName: String): Boolean {
        val commandOutput = executeCommandSU("pm uninstall $packageName")
        return checkPMCommandSuccesfull(commandOutput)
    }

    private fun executeCommandSU(command: String): String? {
        val stdout: List<String> = ArrayList()
        val stderr: List<String> = ArrayList()
        try {
            Shell.Pool.SU.run(command, stdout, stderr, true)
        } catch (e: ShellDiedException) {
            e.printStackTrace()
        }
        if (stdout == null) return null
        val stringBuilder = StringBuilder()
        for (line in stdout) {
            stringBuilder.append(line).append("\n")
        }
        return stringBuilder.toString()
    }

    private fun executeCommandSH(command: String): String? {
        val stdout: List<String> = ArrayList()
        val stderr: List<String> = ArrayList()
        try {
            Shell.Pool.SH.run(command, stdout, stderr, true)
        } catch (e: ShellDiedException) {
            e.printStackTrace()
        }
        if (stdout == null) return null
        val stringBuilder = StringBuilder()
        for (line in stdout) {
            stringBuilder.append(line).append("\n")
        }
        return stringBuilder.toString()
    }

    private fun checkUninstallSuccessful(appApk: String): Boolean {
        val output = executeCommandSH("ls $appApk")
        return output != null && output.trim { it <= ' ' }.isEmpty()
    }

    private fun checkPMCommandSuccesfull(commandOutput: String?): Boolean {
        return commandOutput != null && commandOutput.toLowerCase().contains("success")
    }

    fun getUninstallResult(): SingleLiveEvent<Boolean> {
        return uninstallResult
    }

    fun rebootDevice(): String? {
        return executeCommandSU("reboot")
    }

}