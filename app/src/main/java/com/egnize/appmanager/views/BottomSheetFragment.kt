package com.egnize.appmanager.views

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pixplicity.easyprefs.library.Prefs
import com.egnize.appmanager.Constants
import com.egnize.appmanager.R
import com.egnize.appmanager.databinding.FragmentBottomSheetBinding
import com.egnize.appmanager.helpers.CustomAlertDialog
import com.egnize.chineseapps.utils.Logs

class BottomSheetFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentBottomSheetBinding
    private var isSelectedBottomSheetFragment: IsSelectedBottomSheetFragment? = null

    interface IsSelectedBottomSheetFragment {
        fun onSelectedAlphabeticalOrder()
        fun onSelectInstallationDateOrder()
//        fun onSelectedHideSystemApps()
//        fun onSelectedHideUserApps()
        fun onSelectedShowAllApps()
        fun onSelectedShowSystemApps()
        fun onSelectedShowUserApps()
        fun onSelectedShowChineseApps()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            isSelectedBottomSheetFragment = activity as IsSelectedBottomSheetFragment?
        } catch (e: ClassCastException) {
            Logs.d(BottomSheetFragment::class.java.simpleName, "Activity doesn't implement all interface method")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        return super.onCreateDialog(savedInstanceState)
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        //Set the custom view
        val view =
                LayoutInflater.from(context).inflate(R.layout.fragment_bottom_sheet, null)
        dialog.setContentView(view)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_bottom_sheet, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readCheckedStored()
        setSavedAppTheme()
        setOnclickListener()
    }

    private fun readCheckedStored() {
        val storedStatusAlphabeticalOrder = Prefs.getBoolean(Constants.FLAG_ALPHABETICAL_ORDER, true)
        val storedStatusInstallationDate = Prefs.getBoolean(Constants.FLAG_INSTALLATION_DATE, false)
        val storedStatusAllApps = Prefs.getBoolean(Constants.FLAG_ALL_APPS, true)
        val storedStatusShowSystemApps = Prefs.getBoolean(Constants.FLAG_SYSTEM_APPS, false)
        val storedStatusShowUserApps = Prefs.getBoolean(Constants.FLAG_USER_APPS, false)
        val storedStatusShowChineseApps = Prefs.getBoolean(Constants.FLAG_CHINESE_APPS, false)
        //        boolean storedStatusHideSystemApps = Prefs.getBoolean(Constants.FLAG_HIDE_SYSTEM_APPS, false);
//        boolean storedStatusHideUserApps = Prefs.getBoolean(Constants.FLAG_HIDE_USER_APPS, false);
        setStateStoredOfcheckedAlphabeticalOrder(storedStatusAlphabeticalOrder)
        setStateStoredOfcheckedInstallationDate(storedStatusInstallationDate)
        //        setStateStoredOfHideSystemApps(storedStatusHideSystemApps);
//        setStateStoredOfHideUserApps(storedStatusHideUserApps);
        setStateStoredOfShowAllApps(storedStatusAllApps)
        setStateStoredOfShowSystemApps(storedStatusShowSystemApps)
        setStateStoredOfShowUserApps(storedStatusShowUserApps)
        setStateStoredOfShowChineseApps(storedStatusShowChineseApps)

    }

    private fun setStateStoredOfcheckedAlphabeticalOrder(status: Boolean) {
        if (status) binding.checkedAlphabeticalOrder.visibility = View.VISIBLE else binding.checkedAlphabeticalOrder.visibility = View.GONE
    }

    private fun setStateStoredOfcheckedInstallationDate(status: Boolean) {
        if (status) binding.checkedInstallationDate.visibility = View.VISIBLE else binding.checkedInstallationDate.visibility = View.GONE
    }

    private fun setStateStoredOfHideSystemApps(status: Boolean) {
        if (status) binding.checkedSystemApps.visibility = View.VISIBLE else binding.checkedSystemApps.visibility = View.GONE
    }

    private fun setStateStoredOfShowSystemApps(status: Boolean) {
        if (status) binding.checkedSystemApps.visibility = View.VISIBLE else binding.checkedSystemApps.visibility = View.GONE
    }

    private fun setStateStoredOfHideUserApps(status: Boolean) {
        if (status) binding.checkedUserApps.visibility = View.VISIBLE else binding.checkedUserApps.visibility = View.GONE
    }

    private fun setStateStoredOfShowUserApps(status: Boolean) {
        if (status) binding.checkedUserApps.visibility = View.VISIBLE else binding.checkedUserApps.visibility = View.GONE
    }
    private fun setStateStoredOfShowChineseApps(status: Boolean) {
        if (status) binding.checkedChineseApps.visibility = View.VISIBLE else binding.checkedChineseApps.visibility = View.GONE
    }

    private fun setStateStoredOfShowAllApps(status: Boolean) {
        if (status) binding.checkedAllApps.visibility = View.VISIBLE else binding.checkedAllApps.visibility = View.GONE
    }

    private fun setSavedAppTheme() {
        val index = indexOfSavingNightMode
        val appThemeOptions = resources.getStringArray(R.array.alert_dialog_app_theme_options)
        binding.subtitleAppTheme.text = appThemeOptions[index]
    }

    private fun setOnclickListener() {
        binding.alphabeticalOrder.setOnClickListener(this)
        binding.installationDate.setOnClickListener(this)
        binding.systemApps.setOnClickListener(this)
        binding.userApps.setOnClickListener(this)
        binding.chooseAppTheme.setOnClickListener(this)
        binding.allApps.setOnClickListener(this)
        binding.chineseApps.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.alphabetical_order -> {
                manageClickAppOrder(binding.checkedAlphabeticalOrder, binding.checkedInstallationDate)
                storedAlphabeticalOrderClicked()
                isSelectedBottomSheetFragment!!.onSelectedAlphabeticalOrder()
                dismiss()
            }
            R.id.installation_date -> {
                manageClickAppOrder(binding.checkedInstallationDate, binding.checkedAlphabeticalOrder)
                storedInstallationDateClicked()
                isSelectedBottomSheetFragment!!.onSelectInstallationDateOrder()
                dismiss()
            }
            R.id.system_apps -> {
                manageClickAppOrder(binding.checkedSystemApps, binding.checkedUserApps, binding.checkedAllApps, binding.checkedChineseApps)
                //                hideSystemAppsClicked();
                showSystemAppsClicked()
                dismiss()
            }
            R.id.user_apps -> {
                manageClickAppOrder(binding.checkedUserApps, binding.checkedSystemApps, binding.checkedAllApps, binding.checkedChineseApps)
                //                hideUserAppsClicked();
                showUserAppsClicked()
                dismiss()
            }
            R.id.choose_app_theme -> {
                manageClickChooseAppTheme()
                dismiss()
            }
            R.id.all_apps -> {
                manageClickAppOrder(binding.checkedAllApps, binding.checkedSystemApps, binding.checkedUserApps, binding.checkedChineseApps)
                showAllAppsClicked()
                dismiss()
            }
            R.id.chinese_apps -> {
                manageClickAppOrder(binding.checkedChineseApps, binding.checkedSystemApps, binding.checkedUserApps, binding.checkedAllApps)
                showChineseAppsClicked()
                dismiss()
            }
        }
    }

    private fun manageClickAppOrder(clicked: ImageView, alreadyClicked: ImageView) {
        if (clicked.visibility == View.VISIBLE) return
        clicked.visibility = View.VISIBLE
        alreadyClicked.visibility = View.GONE
    }

    private fun manageClickAppOrder(clicked: ImageView, alreadyClicked: ImageView, alreadyClicked1: ImageView, alreadyClicked2: ImageView) {
        if (clicked.visibility == View.VISIBLE) return
        clicked.visibility = View.VISIBLE
        alreadyClicked.visibility = View.GONE
        alreadyClicked1.visibility = View.GONE
        alreadyClicked2.visibility = View.GONE
    }

    private fun storedAlphabeticalOrderClicked() {
        Prefs.putBoolean(Constants.FLAG_ALPHABETICAL_ORDER, true)
        Prefs.putBoolean(Constants.FLAG_INSTALLATION_DATE, false)
    }

    private fun storedInstallationDateClicked() {
        Prefs.putBoolean(Constants.FLAG_ALPHABETICAL_ORDER, false)
        Prefs.putBoolean(Constants.FLAG_INSTALLATION_DATE, true)
    }

//    private fun hideSystemAppsClicked() {
//        val systemApps = binding.checkedSystemApps
//        val userApps = binding.checkedUserApps
//        if (systemApps.visibility == View.VISIBLE) {
//            systemApps.visibility = View.GONE
//            Prefs.putBoolean(Constants.FLAG_HIDE_SYSTEM_APPS, false)
//            isSelectedBottomSheetFragment!!.onSelectedShowAllApps()
//        } else {
//            systemApps.visibility = View.VISIBLE
//            userApps.visibility = View.GONE
//            Prefs.putBoolean(Constants.FLAG_HIDE_SYSTEM_APPS, true)
//            Prefs.putBoolean(Constants.FLAG_HIDE_USER_APPS, false)
//            isSelectedBottomSheetFragment!!.onSelectedHideSystemApps()
//        }
//    }

//    private fun hideUserAppsClicked() {
//        val systemApps = binding.checkedSystemApps
//        val userApps = binding.checkedUserApps
//        if (userApps.visibility == View.VISIBLE) {
//            userApps.visibility = View.GONE
//            Prefs.putBoolean(Constants.FLAG_HIDE_USER_APPS, false)
//            isSelectedBottomSheetFragment!!.onSelectedShowAllApps()
//        } else {
//            userApps.visibility = View.VISIBLE
//            systemApps.visibility = View.GONE
//            Prefs.putBoolean(Constants.FLAG_HIDE_USER_APPS, true)
//            Prefs.putBoolean(Constants.FLAG_HIDE_SYSTEM_APPS, false)
//            Prefs.putBoolean(Constants.FLAG_ALL_APPS, false)
//            isSelectedBottomSheetFragment!!.onSelectedHideUserApps()
//        }
//    }

    private fun showAllAppsClicked() {
        Prefs.putBoolean(Constants.FLAG_ALL_APPS, true)
        Prefs.putBoolean(Constants.FLAG_SYSTEM_APPS, false)
        Prefs.putBoolean(Constants.FLAG_USER_APPS, false)
        Prefs.putBoolean(Constants.FLAG_CHINESE_APPS, false)
        isSelectedBottomSheetFragment!!.onSelectedShowAllApps()
    }

    private fun showSystemAppsClicked() {
        Prefs.putBoolean(Constants.FLAG_ALL_APPS, false)
        Prefs.putBoolean(Constants.FLAG_SYSTEM_APPS, true)
        Prefs.putBoolean(Constants.FLAG_USER_APPS, false)
        Prefs.putBoolean(Constants.FLAG_CHINESE_APPS, false)
        isSelectedBottomSheetFragment!!.onSelectedShowSystemApps()
    }

    private fun showUserAppsClicked() {
        Prefs.putBoolean(Constants.FLAG_ALL_APPS, false)
        Prefs.putBoolean(Constants.FLAG_SYSTEM_APPS, false)
        Prefs.putBoolean(Constants.FLAG_USER_APPS, true)
        Prefs.putBoolean(Constants.FLAG_CHINESE_APPS, false)
        isSelectedBottomSheetFragment!!.onSelectedShowUserApps()
    }

    private fun showChineseAppsClicked() {
        Prefs.putBoolean(Constants.FLAG_CHINESE_APPS, true)
        Prefs.putBoolean(Constants.FLAG_ALL_APPS, false)
        Prefs.putBoolean(Constants.FLAG_SYSTEM_APPS, false)
        Prefs.putBoolean(Constants.FLAG_USER_APPS, false)
        isSelectedBottomSheetFragment!!.onSelectedShowChineseApps()
    }
    private fun manageClickChooseAppTheme() {
        val title = resources.getString(R.string.alert_dialog_app_theme_title)
        val appThemeOptions = resources.getStringArray(R.array.alert_dialog_app_theme_options)
        val itemSelected = indexOfSavingNightMode
        val onClickListener = DialogInterface.OnClickListener { dialogInterface: DialogInterface, i: Int ->
            setAppThemeFromIndex(i)
            dialogInterface.dismiss()
        }
        CustomAlertDialog.showAlertDialogWithRadio(
                context,
                title,
                appThemeOptions,
                itemSelected,
                onClickListener
        )
    }

    private val indexOfSavingNightMode: Int
        private get() {
            val itemSelected = Prefs.getInt(Constants.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            return when (itemSelected) {
                AppCompatDelegate.MODE_NIGHT_YES -> 1
                AppCompatDelegate.MODE_NIGHT_NO -> 0
                else -> 2
            }
        }

    private fun setAppThemeFromIndex(index: Int) {
        val themeToSet: Int
        themeToSet = when (index) {
            0 -> AppCompatDelegate.MODE_NIGHT_NO
            1 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(themeToSet)
        savingNighMode(themeToSet)
    }

    private fun savingNighMode(mode: Int) {
        Prefs.putInt(Constants.NIGHT_MODE, mode)
    }
}