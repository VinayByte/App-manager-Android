package com.egnize.appmanager.views

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.egnize.appmanager.BuildConfig
import com.egnize.appmanager.Constants
import com.egnize.appmanager.Constants.READ_REQUEST_CODE
import com.egnize.appmanager.Constants.WRITE_REQUEST_CODE
import com.egnize.appmanager.R
import com.egnize.appmanager.databinding.FragmentNavigationDrawerBinding
import com.egnize.appmanager.helpers.CustomAlertDialog
import com.egnize.appmanager.models.App
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class NavigationDrawerFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentNavigationDrawerBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_navigation_drawer, container, false)
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.applicationVersion.text = String.format(getString(R.string.menu_application_version), getString(R.string.app_version_code))
        setOnclickListener()
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_navigation_drawer, null)
        dialog.setContentView(view)
    }

    private fun setOnclickListener() {
        binding.boxImportSelected.setOnClickListener(this)
        binding.boxExportSelected.setOnClickListener(this)
        binding.boxShareSelected.setOnClickListener(this)
        binding.boxLeaveFeedback.setOnClickListener(this)
        binding.boxRateThisApp.setOnClickListener(this)
        binding.boxInfoDeveloper.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.box_import_selected -> performFileSearch()
            R.id.box_export_selected -> if (atLeasOneAppSelected()) {
                val formatter = SimpleDateFormat("ddMMyyyy_HHmmss")
                val now = Date()
                createFile("text/egnizeapp", formatter.format(now) + ".egnizeapp")
            } else {
                CustomAlertDialog.showAlertDialogWithOneButton(
                        requireContext(),
                        resources.getString(R.string.alert_dialog_no_app_selected_title),
                        resources.getString(R.string.alert_dialog_no_app_selected_message),
                        resources.getString(R.string.button_ok),
                        null)
            }
            R.id.box_share_selected -> {
                openShareApp()
            }
            R.id.box_leave_feedback -> {
                val mail = Constants.MAIL
                val subject = Constants.SUBJECT
                openMailFeedback(mail, subject)
            }
            R.id.box_rate_this_app -> {
                rateThisApp()
            }
            R.id.box_info_developer -> {
                val url = Constants.MY_WEB_SITE
                openWebSite(url)
            }
        }
    }

    private fun rateThisApp() {
        val uri = Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        startActivity(goToMarket)
    }

    private fun openShareApp() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.about_app_name))
        val extraText = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
        intent.putExtra(Intent.EXTRA_TEXT, extraText)
        startActivity(Intent.createChooser(intent, getString(R.string.action_share_app)))
    }

    private fun openWebSite(url: String) {
        val uri = Uri.parse(url)
        val openWebsite = Intent(Intent.ACTION_VIEW)
        openWebsite.data = uri
        startActivity(openWebsite)
    }

    private fun openMailFeedback(mailtTo: String, subject: String) {
        val subjectEncode = Uri.encode(subject)
        val uriText = "mailto:$mailtTo?SUBJECT=$subjectEncode"
        val uri = Uri.parse(uriText)
        val sendIntent = Intent(Intent.ACTION_SENDTO)
        sendIntent.data = uri
        startActivity(Intent.createChooser(sendIntent, "Send email"))
    }

    private fun atLeasOneAppSelected(): Boolean {
        val apps = (activity as MainActivity?)!!.getInstalledApps()
        for (app in apps!!) {
            if (app.isSelected) return true
        }
        return false
    }

    private fun performFileSearch() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        // Filter to show only images, using the image MIME data type.
        intent.type = "*/*"
        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    // Open browser to save the file
    private fun createFile(mimeType: String, fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        // Filter to only show results that can be "opened"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        // Create a file with the requested MIME type.
        intent.type = mimeType
        intent.putExtra(Intent.EXTRA_TITLE, fileName)
        startActivityForResult(intent, WRITE_REQUEST_CODE)
    }

    // Create or upload the file according to the code shown
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (resultCode == Activity.RESULT_OK && resultData != null) {
            when (requestCode) {
                READ_REQUEST_CODE -> try {
                    readFileContent(resultData.data, (activity as MainActivity?)!!.getInstalledApps())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                WRITE_REQUEST_CODE -> writeFileContent(resultData.data, (activity as MainActivity?)!!.getInstalledApps())
            }
        }
    }

    // I write the selected apps within the newly created file
    private fun writeFileContent(uri: Uri?, apps: ArrayList<App>?) {
        var selectedApp = ""
        var count = 0
        for (app in apps!!) {
            if (app.isSelected) {
                selectedApp = selectedApp + app.packageName + ","
                count++
            }
        }
        try {
            val pfd = requireContext().contentResolver.openFileDescriptor(uri!!, "w")
            val fileOutputStream = FileOutputStream(pfd!!.fileDescriptor)
            fileOutputStream.write(selectedApp.toByteArray())
            fileOutputStream.close()
            pfd.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        CustomAlertDialog.showAlertDialogWithOneButton(
                requireContext(),
                resources.getString(R.string.alert_dialog_title_exported_correctly),
                resources.getString(R.string.alert_dialog_message_exported_correctly),
                resources.getString(R.string.button_ok),
                null
        )
    }

    // I read the selected apps in the file that is passed to me and update the recyclerView
    @Throws(IOException::class)
    private fun readFileContent(uri: Uri?, apps: ArrayList<App>?) {
        val inputStream = requireContext().contentResolver.openInputStream(uri!!)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var currentline: String?
        while (reader.readLine().also { currentline = it } != null) {
            stringBuilder.append(currentline)
        }
        inputStream!!.close()
        val selectedApp = ArrayList(Arrays.asList(*stringBuilder.toString().split(",".toRegex()).toTypedArray()))
        var count = 0
        for (app in apps!!) {
            if (selectedApp.contains(app.packageName)) {
                app.isSelected = true
                count++
            }
        }
        (activity as MainActivity?)!!.setInstalledApps(apps)
        if (count == 0) {
            CustomAlertDialog.showAlertDialogWithOneButton(
                    requireContext(),
                    resources.getString(R.string.alert_dialog_title_no_app_selected_present),
                    resources.getString(R.string.alert_dialog_message_no_app_selected_present),
                    resources.getString(R.string.button_ok),
                    null
            )
        } else {
            CustomAlertDialog.showAlertDialogWithOneButton(
                    requireContext(),
                    resources.getString(R.string.alert_dialog_title_imported_correctly),
                    count.toString() + " " + resources.getString(R.string.alert_dialog_message_imported_correctly),
                    resources.getString(R.string.button_ok),
                    null
            )
        }
    }
}