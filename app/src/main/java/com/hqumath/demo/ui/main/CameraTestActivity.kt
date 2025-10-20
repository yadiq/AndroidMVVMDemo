package com.hqumath.demo.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.hqumath.demo.R
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityCameraTestBinding

class CameraTestActivity : BaseActivity() {
    private lateinit var binding: ActivityCameraTestBinding

    override fun initContentView(savedInstanceState: Bundle?): View {
        binding = ActivityCameraTestBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initListener() {
    }

    override fun initData() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, DemoFragment())
        transaction.commitAllowingStateLoss()
    }

    override fun initViewObservable() {
    }
}