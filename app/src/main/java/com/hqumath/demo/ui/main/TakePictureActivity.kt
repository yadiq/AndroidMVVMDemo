package com.hqumath.demo.ui.main

import android.os.Bundle
import android.util.Size
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityTakePictureBinding
import com.hqumath.demo.utils.FileUtil
import com.hqumath.demo.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// Camera2 API
// CameraX
class TakePictureActivity : BaseActivity() {
    private lateinit var binding: ActivityTakePictureBinding
    private var imageCapture: ImageCapture? = null

    override fun initContentView(savedInstanceState: Bundle?): View {
        binding = ActivityTakePictureBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initListener() {
        binding.btnTakePicture.setOnClickListener {
            takePicture()
        }

    }

    override fun initData() {
        startCamera()
        //定时拍照
        lifecycleScope.launch(Dispatchers.IO) { //协程和生命周期能绑定
            //repeatOnLifecycle(Lifecycle.State.CREATED) { //onCreate()后启动 -> onDestroy()时取消
            repeatOnLifecycle(Lifecycle.State.STARTED) { //onStart()时启动 -> onStop()时取消。
                while (isActive) { //协程作用域取消时自动退出
                    takePicture()
                    delay(3_000L) //1s一次
                }
            }
        }
    }

    override fun initViewObservable() {
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable { //在视图创建后成功执行
            //CameraProvider
            val cameraProvider = cameraProviderFuture.get()
            //选择后置摄像头
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            /*var cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()*/
            //预览
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            //拍照
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY) //设置捕获模式为最小化延迟。可根据场景权衡延迟和质量
                //.setTargetRotation(viewFinder.display.rotation) //设置目标旋转
                .setTargetResolution(
                    Size(1080, 1920)
                ) //设置期望的最小输出分辨率。CameraX会选择不小于该值的最接近设备支持分辨率。提供一定程度的分辨率控制。如果不存在，则选择小于它的最接近分辨率。需通过 attachedSurfaceResolution 获取实际值
                .build()
            try {
                //解除所有绑定
                cameraProvider.unbindAll()
                //将用例绑定到生命周期所有者 和 CameraProvider
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                LogUtil.e("Use case binding failed " + exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePicture() {
        val name = System.currentTimeMillis().toString() + ".jpg"
        val file = FileUtil.getExternalFile("snapshot", name)
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture?.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException) {
                    LogUtil.e("Photo capture failed: ${error.message}")
                    //CommonUtil.toast("拍照失败")
                    // insert your code here.
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri ?: return
                    val msg = "Photo capture succeeded: $savedUri"
                    //CommonUtil.toast(msg)
                    LogUtil.d(msg)
                }
            })
    }
}
