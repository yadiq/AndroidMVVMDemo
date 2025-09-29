package com.hqumath.demo.ui.main

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityMainBinding
import com.hqumath.demo.dialog.CommonDialog
import com.hqumath.demo.service.MonitorService
import com.hqumath.demo.ui.repos.MyReposActivity
import com.hqumath.demo.utils.CommonUtil
import com.hqumath.demo.utils.PermissionUtil
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission

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
            AndPermission.with(mContext)
                .runtime()
                .permission(Permission.CAMERA)
                .onGranted { permissions: List<String?>? ->
                    startActivity(Intent(mContext, TakePictureActivity::class.java))
                }
                .onDenied { permissions: List<String?>? ->  //未全部授权
                    PermissionUtil.showSettingDialog(mContext, permissions) //自定义弹窗 去设置界面
                }.start()
        }
        binding.btnMyRepos.setOnClickListener {
            startActivity(Intent(mContext, MyReposActivity::class.java))
        }
    }

    override fun initData() {
        val permissions = mutableListOf<String>()
        permissions.add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {//33
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        AndPermission.with(mContext)
            .runtime()
            .permission(permissions.toTypedArray())
            .onGranted { permissions: List<String?>? ->
                afterPermission()
            }
            .onDenied { permissions: List<String?>? ->  //未全部授权
                CommonUtil.toast("请您到设置页面手动授权")
                //PermissionUtil.showSettingDialog(mContext, permissions) //自定义弹窗 去设置界面
            }.start()
    }

    override fun initViewObservable() {
    }

    override fun onBackPressed() {
        val dialog = CommonDialog(
            context = mContext,
            title = "提示",
            message = "是否确认退出？",
            positiveText = "确定",
            positiveAction = {
                stopMyForegroundService()
                finish()
            },
            negativeText = "取消",
            negativeAction = {}
        )
        dialog.show()
    }

    private fun afterPermission() {
        startMyForegroundService()//启动前台服务
    }

    //前台服务
    fun startMyForegroundService() {
        val serviceIntent = Intent(this, MonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent) // Android 8.0+ 推荐方式
        } else {
            startService(serviceIntent)
        }
    }

    fun stopMyForegroundService() {
        val serviceIntent = Intent(this, MonitorService::class.java)
        stopService(serviceIntent)
    }
}
