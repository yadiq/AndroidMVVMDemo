package com.hqumath.demo.ui.main

import android.os.Bundle
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityTakePictureBinding
import com.hqumath.demo.utils.LogUtil

// Camera2 API
// CameraX
class TakePictureActivity : BaseActivity() {
    private lateinit var binding: ActivityTakePictureBinding

    override fun initContentView(savedInstanceState: Bundle?): View {
        binding = ActivityTakePictureBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initListener() {
        binding.btnTakePicture.setOnClickListener {

        }

    }

    override fun initData() {
        startCamera()
    }

    override fun initViewObservable() {
    }

    // 注意：需要先处理相机权限请求
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            // CameraProvider
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider) // 为Preview设置SurfaceProvider
                }
            // 选择后置摄像头
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                // 解除所有绑定
                cameraProvider.unbindAll()
                // 将用例绑定到生命周期所有者（如Activity或Fragment）和CameraProvider
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview)
            } catch(exc: Exception) {
                LogUtil.e("Use case binding failed " + exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }
}
