package com.hqumath.demo.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityMainBinding
import com.hqumath.demo.ui.login.LoginActivity
import com.hqumath.demo.ui.repos.MyReposActivity
import com.hqumath.demo.utils.CommonUtil
import com.jiangdg.ausbc.base.CameraActivity

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
        //enableEdgeToEdge() 启用沉浸式布局
        binding = ActivityMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initListener() {
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(mContext, CameraTestActivity1::class.java))
        }
        binding.btnMyRepos.setOnClickListener {
            startActivity(Intent(mContext, MyReposActivity::class.java))
        }
    }

    override fun initData() {
        //申请相机权限
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0);
        } else {
            CommonUtil.toast("未授予相机权限")
        }
    }

    override fun initViewObservable() {
    }
}
