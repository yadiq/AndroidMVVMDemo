package com.hqumath.demo.ui.main

import android.graphics.SurfaceTexture
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.view.TextureView
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityCameraTest1Binding
import com.hqumath.demo.utils.LogUtil
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.widget.IAspectRatio

class CameraTestActivity1 : BaseActivity() {
    private lateinit var binding: ActivityCameraTest1Binding
    private lateinit var viewModel: CameraViewModel

    override fun initContentView(savedInstanceState: Bundle?): View {
        binding = ActivityCameraTest1Binding.inflate(layoutInflater)
        return binding.root
    }

    override fun initListener() {
        binding.btnGetCameraList.setOnClickListener {
            val sb = StringBuilder()
            //相机列表
            val usbDeviceList: MutableList<UsbDevice>? = viewModel.getDeviceList()
            if (usbDeviceList.isNullOrEmpty()) {
                sb.append("Get usb device failed")
                binding.tvInfo.setText(sb.toString())
                return@setOnClickListener
            }
            for (index in (0 until usbDeviceList.size)) {
                val dev = usbDeviceList[index]
                val cameraInfo =
                    "相机${index},${dev.productName},${dev.deviceName},${dev.productId}"
                sb.append(cameraInfo).append("\n")
                LogUtil.d(cameraInfo)
            }
            viewModel.getCurrentCamera()?.let { strategy ->
                if (strategy is CameraUVC) {
                    val curDevice = strategy.getUsbDevice()
                    val curDeviceInfo =
                        "当前:${curDevice.productName},${curDevice.deviceName},${curDevice.productId}"
                    sb.append(curDeviceInfo).append("\n")
                }
            }
            binding.tvInfo.setText(sb.toString())
        }
        binding.btnCamera0.setOnClickListener {
            viewModel.switchCamera(viewModel.getDeviceList()!![0])
        }
        binding.btnCamera1.setOnClickListener {
            viewModel.switchCamera(viewModel.getDeviceList()!![1])
        }
        binding.btnCamera2.setOnClickListener {
            viewModel.switchCamera(viewModel.getDeviceList()!![2])
        }
    }

    override fun initData() {
        viewModel = ViewModelProvider(this)[CameraViewModel::class.java]

        initCameraView(binding.cameraView)
    }

    override fun initViewObservable() {
    }

    private fun initCameraView(textureView: TextureView) {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                viewModel.registerMultiCamera(mContext)
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                viewModel.surfaceSizeChanged(width, height)
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                viewModel.unRegisterMultiCamera()
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            }
        }
        viewModel.mCameraView = textureView as IAspectRatio
    }
}