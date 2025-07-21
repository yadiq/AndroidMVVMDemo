package com.hqumath.demo.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityMainBinding
import com.hqumath.demo.ui.repos.MyReposActivity

/**
 * ****************************************************************
 * 作    者: Created by gyd
 * 创建时间: 2023/10/25 9:35
 * 文件描述: 主界面
 * 注意事项:
 * ****************************************************************
 */
class MainActivity : BaseActivity() {
    private var binding: ActivityMainBinding? = null

    override fun initContentView(savedInstanceState: Bundle): View {
        //enableEdgeToEdge() 启用沉浸式布局
        binding = ActivityMainBinding.inflate(layoutInflater)
        return binding!!.root
    }

    override fun initListener() {
        binding!!.btnMyRepos.setOnClickListener {
            startActivity(Intent(mContext, MyReposActivity::class.java))
        }
    }

    override fun initData() {
    }
}
