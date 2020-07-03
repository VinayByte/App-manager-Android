package com.egnize.appmanager.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.egnize.appmanager.viewmodels.BaseViewModel
import com.egnize.chineseapps.utils.Logs

abstract class BaseActivity : AppCompatActivity() {
    protected abstract val layoutId: Int
    protected abstract fun setBottomAppBar()
    protected abstract val viewModel: BaseViewModel?
    protected abstract fun setBinding()
    val currentActivity: BaseActivity
        get() = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (layoutId == 0) {
            Logs.w(BaseActivity::class.java.simpleName, "Layout id is zero")
            return
        }
        setBinding()
        setBottomAppBar()
    }
}