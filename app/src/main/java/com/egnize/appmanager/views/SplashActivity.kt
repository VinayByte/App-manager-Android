package com.egnize.appmanager.views

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.egnize.appmanager.App
import com.egnize.appmanager.DataRepository
import com.egnize.appmanager.R
import com.egnize.appmanager.databinding.ActivitySplashBinding
import com.egnize.appmanager.services.LoadApps
import com.egnize.appmanager.utils.Utils
import com.egnize.appmanager.viewmodels.BaseViewModel
import com.egnize.appmanager.viewmodels.MainViewModel
import java.util.ArrayList

class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding
    private var mainViewModel: MainViewModel? = null

    override val layoutId: Int
         get() = R.layout.activity_splash

    override fun setBottomAppBar() {}

    override fun setBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
    }

//    private val loadApps: LoadApps
//        private get() {
//            val uninstallSystemApps = application as UninstallSystemApps
//            val dataRepository = uninstallSystemApps.dataRepository
//            return dataRepository!!.loadApps
//        }

    override val viewModel: MainViewModel
        get() {
            if (mainViewModel == null) {
                val application = application
                val dataRepository = (application as App).dataRepository
                val rootManager = dataRepository!!.rootManager
                val factory =
                    MainViewModel.Factory(application, rootManager)
                mainViewModel = ViewModelProviders.of(this, factory).get(MainViewModel::class.java)
            }
            return mainViewModel!!
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getLoadApps()!!.searchInstalledApps()
        viewModel.firestoreDataFetched.observe(this, Observer {
            if (it) {

            }
        })

//        Utils.readJson(this)
        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }

    private fun getLoadApps(): LoadApps? {
        val uninstallSystemApps = application as App
        val dataRepository: DataRepository? = uninstallSystemApps.dataRepository
        return dataRepository!!.loadApps
    }

}