package com.hqumath.demo.ui.main

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityMainBinding


/**
 * ****************************************************************
 * 作    者: Created by gyd
 * 创建时间: 2023/10/25 9:35
 * 文件描述: 主界面
 * 注意事项:
 * ****************************************************************
 */
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun initContentView(savedInstanceState: Bundle?): View {
        binding = ActivityMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initListener() {
    }

    override fun initData() {
        val intent = Intent(Settings.ACTION_SOUND_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun initViewObservable() {
    }
}
