package com.hqumath.demo.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityMainBinding
import com.hqumath.demo.utils.PermissionUtil
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission
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
            //startActivity(Intent(mContext, CameraTestActivity::class.java))
            AndPermission.with(mContext)
                .runtime()
                .permission(Permission.CAMERA, Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted { permissions -> startActivity(Intent(mContext, CameraTestActivity::class.java)) }
                .onDenied { permissions ->
                    if (AndPermission.hasAlwaysDeniedPermission(mContext, permissions)) {
                        PermissionUtil.showSettingDialog(mContext, permissions);//自定义弹窗 去设置界面
                    }
                }
                .start()
        }

        binding.btnMyRepos.setOnClickListener {
            //startActivity(Intent(mContext, MyReposActivity::class.java))
            //开启线程，定时拍照
            lifecycleScope.launch(Dispatchers.IO) { //协程和生命周期能绑定
                repeatOnLifecycle(Lifecycle.State.CREATED) { //onCreate()后启动 -> onDestroy()时取消
                    delay(10_000L) //
                    while (isActive) { //协程作用域取消时自动退出
                        UVCCameraTool.quickCamera(0, binding.textureView)
//                        UVCCameraTool.quickCamera(1, binding.textureView)
                        delay(10_000L)
                    }
                }
            }
        }
    }

    override fun initData() {
        UVCCameraTool.init()
    }

    override fun initViewObservable() {
    }

    override fun onDestroy() {
        super.onDestroy()
        UVCCameraTool.release()
    }
}
