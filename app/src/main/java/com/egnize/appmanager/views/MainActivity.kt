package com.egnize.appmanager.views

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.egnize.appmanager.Constants
import com.egnize.appmanager.R
import com.egnize.appmanager.adapters.AppRecyclerAdapter
import com.egnize.appmanager.databinding.ActivityMainBinding
import com.egnize.appmanager.helpers.CustomAlertDialog
import com.egnize.appmanager.helpers.InsetDivider
import com.egnize.appmanager.models.App
import com.egnize.appmanager.models.RootState
import com.egnize.appmanager.viewmodels.MainViewModel
import com.egnize.appmanager.views.BottomSheetFragment.IsSelectedBottomSheetFragment
import com.pixplicity.easyprefs.library.Prefs
import java.util.*

class MainActivity : BaseActivity(), View.OnClickListener, OnRefreshListener, IsSelectedBottomSheetFragment {
    private var UNINSTALL_REQUEST_CODE = 1
    private var mainViewModel: MainViewModel? = null
    private lateinit var binding: ActivityMainBinding
    private var recyclerView: RecyclerView? = null
    private var appRecyclerAdapter: AppRecyclerAdapter? = null
    private var installedApps: MutableList<App> = ArrayList()
    var adapterCallback = MutableLiveData<Boolean>()


    override val layoutId: Int
        get() = R.layout.activity_main

    override fun setBottomAppBar() {
        val bottomAppBar = binding.bar
        setSupportActionBar(bottomAppBar)
    }

    override val viewModel: MainViewModel
        get() {
            if (mainViewModel == null) {
                val application = application
                val dataRepository = (application as com.egnize.appmanager.App).dataRepository
                val rootManager = dataRepository!!.rootManager
                val factory = MainViewModel.Factory(application, rootManager)
                mainViewModel = ViewModelProviders.of(this, factory).get(MainViewModel::class.java)
            }
            return mainViewModel!!
        }

    override fun setBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fab -> removeAppClick()
        }
    }

    private fun removeAppClick() {
//        RootState rootState = checkRootState();
//        if(rootState == RootState.HAVE_ROOT)
        checkAppsAreSelected()
    }

    private fun checkAppsAreSelected() {
        val anAppIsSelected = viewModel.atLeastAnAppIsSelected(installedApps!!)
        if (anAppIsSelected) askPermissionToUninstallSelectedApps() else CustomAlertDialog.showAlertDialogWithOneButton(
                this,
                resources.getString(R.string.alert_dialog_no_app_selected_title),
                resources.getString(R.string.alert_dialog_no_app_selected_message),
                resources.getString(R.string.button_ok),
                null
        )
    }

    private fun askPermissionToUninstallSelectedApps() {
        val positiveListener = DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> startUninstallProcess() }
        CustomAlertDialog.showAlertDialogWithTwoButton(
                this,
                resources.getString(R.string.alert_dialog_ask_permission_to_remove_apps_title),
                resources.getString(R.string.alert_dialog_ask_permission_to_remove_apps_message),
                resources.getString(R.string.button_yes),
                positiveListener,
                resources.getString(R.string.button_no),
                null
        )
    }

    private fun startUninstallProcess() {
        if (isRootAccessAlreadyObtained) {
            val uninstallAnimation = CustomAlertDialog.showProgressDialog(
                    this,
                    resources.getString(R.string.progress_dialog_removing_apps)
            )
            val uninstallResult = viewModel.removeApps(installedApps!!)
            uninstallResult.observeForever(Observer {
                if (it == null) {
                    stopUninstallProcess(it!!, uninstallAnimation)
                }
            })
        } else {
//            val uninstallAnimation = CustomAlertDialog.showProgressDialog(
//                    this,
//                    resources.getString(R.string.progress_dialog_removing_apps)
//            )
            removeUserApps(installedApps)
//            uninstallUserResult.observe(this, Observer { uninstall ->
//                if (uninstall!!) {
//                    stopUninstallProcess(uninstall, uninstallAnimation)
//                }
//            })
        }
    }

    fun removeUserApps(installedApps: List<App>?) {
        val selectedApps: List<App> = mainViewModel!!.getSelectedApps(installedApps)
        if (selectedApps.isEmpty()) {
            onRefresh()
            return
        }
        val packageUri = Uri.parse("package:${selectedApps[0].packageName}")
        val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri)
        uninstallIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
        startActivityForResult(uninstallIntent, 1)
    }

    private fun stopUninstallProcess(uninstallResult: Boolean, uninstallAnimation: AlertDialog) {
        CustomAlertDialog.stopProgressDialog(uninstallAnimation)
        binding.swipeRefresh.isRefreshing = true
        onRefresh()
        if (uninstallResult) uninstallSuccessful() else uninstallError()
    }

    private fun uninstallSuccessful() {
        val rebootButton = DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> viewModel.rebootDevice() }
        CustomAlertDialog.showAlertDialogWithTwoButton(
                this,
                resources.getString(R.string.alert_dialog_title_reboot_now),
                resources.getString(R.string.alert_dialog_message_reboot_now),
                resources.getString(R.string.button_reboot),
                rebootButton,
                resources.getString(R.string.button_no),
                null
        )
    }

    private fun uninstallError() {
        CustomAlertDialog.showAlertDialogWithOneButton(
                this,
                resources.getString(R.string.alert_dialog_title_error_remove_apps),
                resources.getString(R.string.alert_dialog_message_error_remove_apps),
                resources.getString(R.string.button_ok),
                null
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bottomappbar_menu, menu)
        val actionSearch = menu.findItem(R.id.app_bar_search)
        manageSearch(actionSearch)
        return super.onCreateOptionsMenu(menu)
    }

    private fun manageSearch(searchItem: MenuItem) {
        val searchView = searchItem.actionView as SearchView
        manageFabOnSearchItemStatus(searchItem)
        manageInputTextInSearchView(searchView)
    }

    private fun manageFabOnSearchItemStatus(searchItem: MenuItem) {
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                binding.fab.hide()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                binding.fab.show()
                return true
            }
        })
    }

    private fun manageInputTextInSearchView(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (installedApps == null) return false
                val query = newText.toLowerCase().trim { it <= ' ' }
                val filteredApps: List<App?>? = viewModel.filterApps(query, installedApps!!)
                appRecyclerAdapter!!.updataList(filteredApps as ArrayList<App?>?)
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.app_bar_settings -> {
                val bottomSheetFragment = BottomSheetFragment()
                bottomSheetFragment.show(supportFragmentManager, "TAG")
            }
            android.R.id.home -> {
                val navigationDrawerFragment = NavigationDrawerFragment()
                navigationDrawerFragment.show(supportFragmentManager, "TAG")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnclickListener()
        setOnRefreshListener()
        startLoadingAnimation()

        viewModel.installedApps.observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                installedApps = it
                hideAppStoredFlag()
                orderAppInStoredOrder()
                updateRecyclerView()
                stopLoadingAnimation()
                binding.swipeRefresh.isRefreshing = false
            }
        })
        checkRootState()
    }

    private fun setOnclickListener() {
        binding.fab.setOnClickListener(this)
    }

    private fun setOnRefreshListener() {
        binding.swipeRefresh.setProgressBackgroundColorSchemeColor(resources.getColor(R.color.progressCircularBackground))
        binding.swipeRefresh.setColorSchemeColors(resources.getColor(R.color.accent))
        binding.swipeRefresh.setOnRefreshListener(this)
    }

    override fun onResume() {
        super.onResume()
//        viewModel.installedApps.observe(this, Observer {
//            if (!it.isNullOrEmpty()) {
//                installedApps = it
//                hideAppStoredFlag()
//                orderAppInStoredOrder()
//                updateRecyclerView()
//                stopLoadingAnimation()
//                binding.swipeRefresh.isRefreshing = false
//            }
//        })
//        checkRootState()
    }

    private fun hideAppStoredFlag() {
//        boolean isHideSystemApps = Prefs.getBoolean(Constants.FLAG_HIDE_SYSTEM_APPS, false);
//        boolean isHideUserApps = Prefs.getBoolean(Constants.FLAG_HIDE_USER_APPS, false);
        val isShowSystemApps = Prefs.getBoolean(Constants.FLAG_SYSTEM_APPS, false)
        val isShowUserApps = Prefs.getBoolean(Constants.FLAG_USER_APPS, false)
        val isShowAllApps = Prefs.getBoolean(Constants.FLAG_ALL_APPS, true)
        val isShowChineseApps = Prefs.getBoolean(Constants.FLAG_CHINESE_APPS, false)

        //        if(isHideSystemApps && !isHideUserApps) {
//            this.installedApps = (ArrayList<App>?) getViewModel().hideSystemApps(installedApps);
//        } else if(isHideUserApps && !isHideSystemApps) {
//            this.installedApps = (ArrayList<App>?) getViewModel().hideUserApps(installedApps);
//        }
        if (!isShowSystemApps && !isShowUserApps && isShowAllApps && !isShowChineseApps) {
            installedApps = viewModel.showAllApps(installedApps)
        } else if (isShowSystemApps && !isShowUserApps && !isShowAllApps && !isShowChineseApps) {
            installedApps = viewModel.hideUserApps(installedApps)
        } else if (!isShowSystemApps && isShowUserApps && !isShowAllApps && !isShowChineseApps) {
            installedApps = viewModel.hideSystemApps(installedApps)
        } else if (!isShowSystemApps && !isShowUserApps && !isShowAllApps && isShowChineseApps) {
            installedApps = viewModel.showChineseApps(installedApps)
        }
    }

    private fun orderAppInStoredOrder() {
        val isAlphabeticalOrder = Prefs.getBoolean(Constants.FLAG_ALPHABETICAL_ORDER, true)
        val isInstallationDateOrder = Prefs.getBoolean(Constants.FLAG_INSTALLATION_DATE, false)
        installedApps = if (isAlphabeticalOrder && !isInstallationDateOrder) {
            viewModel.orderAppInAlfabeticalOrder(installedApps)
        } else {
            viewModel.orderAppForInstallationDateDesc(installedApps)
        }
    }

    private fun updateRecyclerView() {
        if (recyclerView == null) setRecyclerView() else appRecyclerAdapter!!.updataList(installedApps as ArrayList<App>?)
    }

    private fun setRecyclerView() {
        recyclerView = binding.recyclerView
        val divider = insetDivider
        recyclerView!!.addItemDecoration(divider)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        appRecyclerAdapter = AppRecyclerAdapter(installedApps as ArrayList<App>?, isRootAccessAlreadyObtained, adapterCallback)
        recyclerView!!.adapter = appRecyclerAdapter

        adapterCallback.observe(this, Observer {
            if (it)
                generateRootStateAlertDialog(
                        resources.getString(R.string.alert_dialog_title_no_root_permission),
                        resources.getString(R.string.alert_dialog_message_no_root_permission)
                )
        })
    }

    private val insetDivider: RecyclerView.ItemDecoration
        private get() {
            val dividerHeight = resources.getDimensionPixelSize(R.dimen.divider_height)
            val dividerColor = resources.getColor(R.color.divider)
            val marginLeft = resources.getDimensionPixelSize(R.dimen.divider_inset)
            return InsetDivider.Builder(this)
                    .orientation(InsetDivider.VERTICAL_LIST)
                    .dividerHeight(dividerHeight)
                    .color(dividerColor)
                    .insets(marginLeft, 0)
                    .build()
        }

    private fun startLoadingAnimation() {
        binding.progressCircular.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private fun stopLoadingAnimation() {
        binding.progressCircular.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    private fun checkRootState(): RootState? {
        val rootState = viewModel.checkRootPermission()
        when (rootState) {
            RootState.NO_ROOT -> {
                isRootAccessAlreadyObtained = false
                generateRootStateAlertDialog(
                        resources.getString(R.string.alert_dialog_title_no_root_permission),
                        resources.getString(R.string.alert_dialog_message_no_root_permission)
                )
            }
            RootState.BE_ROOT -> {
                isRootAccessAlreadyObtained = false
                generateRootStateAlertDialog(
                        resources.getString(R.string.alert_dialog_title_be_root),
                        resources.getString(R.string.alert_dialog_message_be_root)
                )
            }
            RootState.HAVE_ROOT -> {
                var rootAccessAlreadyObtained = isRootAccessAlreadyObtained
                if (rootAccessAlreadyObtained)
                    rootAccessAlreadyObtained = true
                generateRootStateAlertDialog(
                        resources.getString(R.string.alert_dialog_title_have_root),
                        resources.getString(R.string.alert_dialog_message_have_root)
                )
            }
        }
        return rootState
    }

    private var isRootAccessAlreadyObtained: Boolean
        get() {
            val key = Constants.FLAG_ROOT_ACCESS_ALREADY_OBTAINED
            return Prefs.getBoolean(key, false)
        }
        private set(status) {
            val key = Constants.FLAG_ROOT_ACCESS_ALREADY_OBTAINED
            Prefs.putBoolean(key, status)
        }

    private fun generateRootStateAlertDialog(title: String, message: String) {
        CustomAlertDialog.showAlertDialogWithOneButton(
                this,
                title,
                message,
                resources.getString(R.string.button_ok),
                null
        )
    }

    override fun onRefresh() {
        viewModel.reloadAppsList()
    }

    override fun onSelectedAlphabeticalOrder() {
        installedApps = viewModel.orderAppInAlfabeticalOrder(installedApps)
        updateRecyclerView()
    }

    override fun onSelectInstallationDateOrder() {
        installedApps = viewModel.orderAppForInstallationDateDesc(installedApps)
        updateRecyclerView()
    }

//    override fun onSelectedHideSystemApps() {
//        installedApps = viewModel.uncheckedAllApps(installedApps)
//        installedApps = viewModel.hideSystemApps(installedApps)
//        updateRecyclerView()
//    }
//
//    override fun onSelectedHideUserApps() {
//        installedApps = viewModel.uncheckedAllApps(installedApps)
//        installedApps = viewModel.hideUserApps(installedApps)
//        updateRecyclerView()
//    }

    override fun onSelectedShowAllApps() {
        onRefresh()
        installedApps = viewModel.uncheckedAllApps(installedApps)
        installedApps = viewModel.showAllApps(installedApps)
        updateRecyclerView()
    }

    override fun onSelectedShowSystemApps() {
        onRefresh()
        installedApps = viewModel.uncheckedAllApps(installedApps)
        installedApps = viewModel.hideUserApps(installedApps)
        updateRecyclerView()
    }

    override fun onSelectedShowUserApps() {
        onRefresh()
        installedApps = viewModel.uncheckedAllApps(installedApps)
        installedApps = viewModel.hideSystemApps(installedApps)
        updateRecyclerView()
    }

    override fun onSelectedShowChineseApps() {
        onRefresh()
        installedApps = viewModel.uncheckedAllApps(installedApps)
        installedApps = viewModel.showChineseApps(installedApps)
        updateRecyclerView()
    }

    fun getInstalledApps(): ArrayList<App>? {
        return installedApps as ArrayList<App>?
    }

    fun setInstalledApps(installedApps: ArrayList<App>?) {
        this.installedApps = installedApps!!
        hideAppStoredFlag()
        orderAppInStoredOrder()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        val uninstallAnimations =
//            showProgressDialog(
//                this,
//                resources.getString(R.string.progress_dialog_removing_apps)
//            )

        val mApps = installedApps as ArrayList<App>
        val mFreeApps: ArrayList<App> = viewModel.getSelectedApps(installedApps) as ArrayList<App>
        if (requestCode == UNINSTALL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val packageName = mFreeApps[0].packageName
                for (i in mApps.indices) {
                    if (mApps[i].packageName.compareTo(packageName) == 0) {
                        mApps.removeAt(i)
                        appRecyclerAdapter!!.notifyDataSetChanged()
                        break
                    }
                }
            } else {
                val packageName = mFreeApps[0].packageName
                for (i in mApps.indices) {
                    if (mApps[i].packageName.compareTo(packageName) == 0) {
                        mApps[i].isSelected = false
                        appRecyclerAdapter!!.notifyDataSetChanged()
                        break
                    }
                }
            }

            mFreeApps.removeAt(0)
            if (mFreeApps.size != 0) {
                /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                     val packageInstaller = this.packageManager.packageInstaller
                     val uninstallIntent = Intent(this, this.javaClass)
                     val sender = PendingIntent.getActivity(this, 0, uninstallIntent, 0)
                     packageInstaller.uninstall(mFreeApps[0].packageName, sender.intentSender);
                 } else {*/
                val packageUri = Uri.parse("package:" + mFreeApps[0].packageName)
                val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri)
                uninstallIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
                startActivityForResult(uninstallIntent, 1)
                // }
            } else {
                mFreeApps.clear()
                onRefresh()
            }
        }
    }

}