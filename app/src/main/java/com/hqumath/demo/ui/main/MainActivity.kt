package com.hqumath.demo.ui.main

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hqumath.demo.app.Constant
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityMainBinding
import com.hqumath.demo.dialog.CommonDialog
import com.hqumath.demo.service.MonitorService
import com.hqumath.demo.ui.repos.MyReposActivity
import com.hqumath.demo.utils.CommonUtil
import com.hqumath.demo.utils.LogUtil
import com.yanzhenjie.permission.AndPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


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
            startActivity(Intent(mContext, TakePictureActivity::class.java))
        }
        binding.btnMyRepos.setOnClickListener {
            startActivity(Intent(mContext, Camera2Activity::class.java))
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

        binding.btnMyRepos.performClick()
    }

    override fun initViewObservable() {
    }

    override fun onDestroy() {
        super.onDestroy()
        //解绑服务
        if (Constant.monitorService != null) {
            unbindService(connection)
            Constant.monitorService = null
        }
    }

    override fun onBackPressed() {
        val dialog = CommonDialog(
            context = mContext,
            title = "提示",
            message = "是否确认退出？",
            positiveText = "确定",
            positiveAction = {
                finish()
            },
            negativeText = "取消",
            negativeAction = {}
        )
        dialog.show()
    }

    private fun afterPermission() {
        //绑定服务
        val serviceIntent = Intent(this, MonitorService::class.java)
        bindService(serviceIntent, connection, BIND_AUTO_CREATE)
        //定时拍照
        lifecycleScope.launch(Dispatchers.IO) { //协程和生命周期能绑定
            repeatOnLifecycle(Lifecycle.State.CREATED) { //onCreate()后启动 -> onDestroy()时取消
//                delay(10_000L) //
//                while (isActive) { //协程作用域取消时自动退出
//                    Constant.monitorService?.quickCamera(0)
//                    Constant.monitorService?.quickCamera(1)
//                    delay(10_000L) //
//                }
            }
        }
    }

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder: MonitorService.LocalBinder = service as MonitorService.LocalBinder
            Constant.monitorService = binder.getService()
            LogUtil.d("MonitorService", "服务已绑定")
        }

        //正常解绑时不会调用, 只在服务进程意外崩溃或被系统杀死时调用
        override fun onServiceDisconnected(name: ComponentName?) {
            Constant.monitorService = null;
            LogUtil.d("MonitorService", "服务意外断开连接")
        }
    }
}
