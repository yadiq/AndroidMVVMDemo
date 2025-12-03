package com.hqumath.demo.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.View
import androidx.core.app.ActivityCompat
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityCamera2Binding

class Camera2Activity : BaseActivity() {
    private lateinit var binding: ActivityCamera2Binding
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null

    override fun initContentView(savedInstanceState: Bundle?): View {
        binding = ActivityCamera2Binding.inflate(layoutInflater)
        return binding.root
    }

    override fun initListener() {
        binding.btnTakePicture.setOnClickListener {
        }
    }

    override fun initData() {
        binding.textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int,
            ) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int,
            ) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = false

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

        }

    }

    override fun initViewObservable() {
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraCaptureSession?.close()
        cameraDevice?.close()
    }

    //打开摄像头
    private fun openCamera() {
        //获取摄像头列表和属性
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = manager.cameraIdList.first {
            manager.getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
            return
        }
        //打开摄像头
        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) {
                cameraDevice = device
                startPreview()
            }

            override fun onDisconnected(device: CameraDevice) {
                device.close()
            }

            override fun onError(device: CameraDevice, error: Int) {
                device.close()
            }
        }, null)
    }

    private fun startPreview() {
        val texture = binding.textureView.surfaceTexture!!
        texture.setDefaultBufferSize(1280, 720)
        val surface = Surface(texture)
        //定义相机请求（预览、拍照）
        val previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        previewRequestBuilder.addTarget(surface)
        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

        //Session会话管理一组相机请求
        cameraDevice!!.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                cameraCaptureSession = session
                session.setRepeatingRequest(previewRequestBuilder.build(), null, null)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {}
        }, null)
    }


}
