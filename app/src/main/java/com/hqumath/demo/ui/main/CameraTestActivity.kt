package com.hqumath.demo.ui.main

import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityCameraTestBinding
import com.hqumath.demo.utils.LogUtil
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.camera.CameraUVC

class CameraTestActivity : BaseActivity() {
    private lateinit var binding: ActivityCameraTestBinding
    private lateinit var viewModel: CameraTestViewModel

    override fun initContentView(savedInstanceState: Bundle?): View {
        //enableEdgeToEdge() 启用沉浸式布局
        binding = ActivityCameraTestBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initListener() {

    }

    override fun initData() {
        viewModel = ViewModelProvider(this)[CameraTestViewModel::class.java]

        viewModel.setCameraStateCallBack(cameraStateCallBack)
        viewModel.initCameraView(binding.cameraViewContainer)

        //相机信息-获取usb相机列表
        //相机预览-切换usb相机
    }

    override fun initViewObservable() {
        binding.btnGetDeviceList.setOnClickListener {
            val usbDeviceList: MutableList<UsbDevice>? = viewModel.getDeviceList() //相机列表
            val sb = StringBuilder()
            if (usbDeviceList.isNullOrEmpty()) {
                sb.append("Get usb device failed")
            } else {
                for (index in (0 until usbDeviceList.size)) {
                    val dev = usbDeviceList[index]
                    val cameraInfo = "设备${index},${dev.productName},${dev.deviceName},${dev.productId}"
                    sb.append(cameraInfo).append("\n")
                    LogUtil.d(usbDeviceList.joinToString())
                }
            }
            binding.tvInfo.text = sb.toString()
            /*viewModel.getCurrentCamera()?.let { strategy ->
                if (strategy is CameraUVC) {
                    val curDevice = strategy.getUsbDevice()
                    val curDeviceInfo =
                        "当前:${curDevice.productName},${curDevice.deviceName},${curDevice.productId}"
                    sb.append(curDeviceInfo).append("\n")
                }
            }*/
        }
        binding.btnGetPreviewSize.setOnClickListener {
            val sizes = viewModel.getCurrentCamera()?.getAllPreviewSizes()
            val sb = StringBuilder()
            if (sizes.isNullOrEmpty()) {
                sb.append("Get PreviewSize failed")
            } else {
                for (index in (0 until sizes.size)) {
                    sb.append("${sizes[index].width}x${sizes[index].height}").append("\n")
                    LogUtil.d(sizes.joinToString())
                }
            }
            binding.tvInfo.text = sb.toString()
        }
    }

    private var cameraStateCallBack: ICameraStateCallBack = object : ICameraStateCallBack {
        override fun onCameraState(
            self: MultiCameraClient.ICamera,
            code: ICameraStateCallBack.State,
            msg: String?,
        ) {
            when (code) {
                ICameraStateCallBack.State.OPENED -> {}
                    //handleCameraOpened() TODO
                ICameraStateCallBack.State.CLOSED -> {}
                    //handleCameraClosed()
                ICameraStateCallBack.State.ERROR -> {}
                    //handleCameraError(msg)
            }
        }
    }

}