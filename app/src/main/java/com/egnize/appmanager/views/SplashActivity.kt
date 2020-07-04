package com.egnize.appmanager.views

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.egnize.appmanager.App
import com.egnize.appmanager.Constants.PRF_IS_FIRST_LAUNCH
import com.egnize.appmanager.DataRepository
import com.egnize.appmanager.R
import com.egnize.appmanager.databinding.ActivitySplashBinding
import com.egnize.appmanager.services.LoadApps
import com.egnize.appmanager.utils.SharedPrefUtils
import com.egnize.appmanager.viewmodels.MainViewModel

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
        val isFirstLaunch = SharedPrefUtils.getData(this, PRF_IS_FIRST_LAUNCH, true) as Boolean
        if (isFirstLaunch){
            binding.btnNext.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
            binding.textView2.visibility = View.VISIBLE
//            binding.btnNext.textColors = resources.getColor(R.color.primary_text)
            binding.btnNext.text = resources.getString(R.string.get_started)
            binding.textView2.text = resources.getString(R.string.privacy_terms)
        }else{
            binding.btnNext.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
            binding.textView2.visibility = View.GONE
            launch()
        }

//        Utils.readJson(this)
        binding.btnNext.setOnClickListener {
            binding.btnNext.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
            launch()
        }

    }

    private fun launch() {
        getLoadApps()!!.searchInstalledApps()
        viewModel.firestoreDataFetched.observe(this, Observer {
            if (it!!) {
                SharedPrefUtils.saveData(this, PRF_IS_FIRST_LAUNCH, false)
                binding.progressBar.visibility = View.GONE
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }else{
//                SharedPrefUtils.saveData(this, PRF_IS_FIRST_LAUNCH, true)
                binding.btnNext.visibility = View.VISIBLE
                binding.textView2.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.btnNext.text = resources.getString(R.string.retry)
                binding.textView2.text = resources.getString(R.string.network_error)
            }
        })
    }

    private fun getLoadApps(): LoadApps? {
        val uninstallSystemApps = application as App
        val dataRepository: DataRepository? = uninstallSystemApps.dataRepository
        return dataRepository!!.loadApps
    }

}