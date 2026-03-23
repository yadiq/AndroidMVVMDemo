package com.hqumath.demo.ui.main

import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityCameraTestBinding
import com.hqumath.demo.ui.main.CameraTestViewModel.Companion.TAG
import com.hqumath.demo.utils.CommonUtil
import com.hqumath.demo.utils.FileUtil
import com.hqumath.demo.utils.LogUtil
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.callback.ICaptureCallBack
import java.text.SimpleDateFormat
import java.util.Date

class CameraTestActivity : BaseActivity() {
    private lateinit var binding: ActivityCameraTestBinding
    private lateinit var viewModel: CameraTestViewModel
    private val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")

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
    }

    override fun initViewObservable() {
        binding.btnGetDeviceList.setOnClickListener { //usb设备
            val usbDeviceList: MutableList<UsbDevice>? = viewModel.getDeviceList()
            val sb = StringBuilder()
            if (usbDeviceList.isNullOrEmpty()) {
                sb.append("usb device is empty")
            } else {
                // 当前设备
                val curDeviceId = viewModel.getCurrentCamera()?.getUsbDevice()?.deviceId
                sb.append("当前设备(主类别为14或239):$curDeviceId").append("\n")
                // 全部设备
                for (index in (0 until usbDeviceList.size)) {
                    val dev = usbDeviceList[index]
                    var serialNumber: String? = ""
                    try {
                        serialNumber = dev.serialNumber //海康摄像头 44434000_P030C01_SN0002; 老usb摄像头 null
                    } catch (e: Exception) {
                        //e.printStackTrace()
                        LogUtil.d("设备信息异常:$e")
                    }
                    val cameraInfo = "厂商ID:${dev.vendorId},产品ID:${dev.productId}" + //设备标识信息 VID PID
                            ",主类别:${dev.deviceClass},子类别:${dev.deviceSubclass},协议:${dev.deviceProtocol}" + //设备主类别 1:音频设备 3:HID(键盘鼠标) 6:相机(老标准) 8:存储设备 14:摄像头(UVC) 239:杂项
                            ",系统路径:${dev.deviceName},系统ID:${dev.deviceId},序列号:$serialNumber" + //每次插拔都会变化
                            ",厂商名称:${dev.manufacturerName},产品名称:${dev.productName}" //,版本:${dev.version} 限制api版本
                    sb.append(cameraInfo).append("\n\n")
                    LogUtil.d("设备信息:$cameraInfo")
                }
            }
            binding.tvInfo.text = sb.toString()
        }
        binding.btnGetPreviewSize.setOnClickListener { //分辨率列表
            val sizes = viewModel.getCurrentCamera()?.getAllPreviewSizes()
            val sb = StringBuilder()
            if (sizes.isNullOrEmpty()) {
                sb.append("Get PreviewSize failed")
            } else {
                // 当前分辨率
                val curCameraRequest = viewModel.getCurrentCamera()?.getCameraRequest()
                sb.append("当前分辨率:${curCameraRequest?.previewWidth}x${curCameraRequest?.previewHeight}").append("\n")
                // 全部分辨率
                for (index in (0 until sizes.size)) {
                    val sizeInfo = "${sizes[index].width}x${sizes[index].height}"
                    sb.append(sizeInfo).append("\n")
                    LogUtil.d("分辨率信息:$sizeInfo")
                }
            }
            binding.tvInfo.text = sb.toString()
        }
        binding.btnCamera0.setOnClickListener { //切换摄像头
            switchCamera(0)
        }
        binding.btnCamera1.setOnClickListener { //切换摄像头
            switchCamera(1)
        }
        binding.btnCamera2.setOnClickListener { //切换摄像头
            switchCamera(2)
        }
        binding.btnCaptureImage.setOnClickListener {
            if (!viewModel.isCameraOpened()) {
                CommonUtil.toast("camera not worked!")
                return@setOnClickListener
            }
            val fileName = sdf.format(Date()) + ".jpg"
            val filePath = FileUtil.getExternalFile("picture", fileName).absolutePath
            viewModel.getCurrentCamera()?.captureImage(object: ICaptureCallBack {
                override fun onBegin() {
                }

                override fun onError(error: String?) {
                    CommonUtil.toast(error ?: "未知异常")
                    LogUtil.d("拍照异常 ${error ?: "未知异常"}")
                }

                override fun onComplete(path: String?) {
                    CommonUtil.toast("拍照完成")
                    LogUtil.d("拍照完成 $path")
                }
            }, filePath)
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
                    //handleCameraOpened() 更新UI 显示帧率
                ICameraStateCallBack.State.CLOSED -> {}
                    //handleCameraClosed()
                ICameraStateCallBack.State.ERROR -> {}
                    //handleCameraError(msg)
            }
        }
    }

    /**
     * 切换摄像头
     */
    private fun switchCamera(index: Int) {
        if (viewModel.mCameraMap.size <= index)
            return
        var camera = viewModel.mCameraMap.entries.elementAt(index)

        LogUtil.d(TAG, "closeCamera")
        viewModel.getCurrentCamera()?.closeCamera() //有延迟，不能保证释放完成。阻塞现场，等onDisConnectDec
        try {
            Thread.sleep(1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        LogUtil.d(TAG, "requestPermission")
        viewModel.requestPermission(camera.value.getUsbDevice())
    }

}