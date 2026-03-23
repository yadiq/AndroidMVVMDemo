package com.hqumath.demo.ui.main

import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.view.View
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityCameraTestBinding
import com.hqumath.demo.ui.main.CameraTestViewModelOld.Companion.TAG
import com.hqumath.demo.utils.LogUtil
import kotlin.concurrent.thread

class CameraTestActivity : BaseActivity() {
    private lateinit var binding: ActivityCameraTestBinding

    override fun initContentView(savedInstanceState: Bundle?): View {
        binding = ActivityCameraTestBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initListener() {
        binding.btnGetDeviceList.setOnClickListener { //usb设备
            val usbDeviceList: MutableList<UsbDevice>? = UVCCameraTool.getDeviceList()
            val sb = StringBuilder()
            if (usbDeviceList.isNullOrEmpty()) {
                sb.append("usb device is empty")
            } else {
                // 当前设备
                val curDeviceId = UVCCameraTool.getCurrentCamera()?.getUsbDevice()?.deviceId
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
                    val cameraInfo = "系统ID:${dev.deviceId},厂商ID:${dev.vendorId},产品ID:${dev.productId}" + //设备标识信息 VID PID
                            ",主类别:${dev.deviceClass},子类别:${dev.deviceSubclass},协议:${dev.deviceProtocol}" + //设备主类别 1:音频设备 3:HID(键盘鼠标) 6:相机(老标准) 8:存储设备 14:摄像头(UVC) 239:杂项
                            ",系统路径:${dev.deviceName},序列号:$serialNumber" + //每次插拔都会变化
                            ",厂商名称:${dev.manufacturerName},产品名称:${dev.productName}" //,版本:${dev.version} 限制api版本
                    sb.append(cameraInfo).append("\n\n")
                    LogUtil.d("设备信息:$cameraInfo")
                }
            }
            binding.tvInfo.text = sb.toString()
        }
        binding.btnGetPreviewSize.setOnClickListener { //分辨率列表
            val sizes = UVCCameraTool.getCurrentCamera()?.getAllPreviewSizes()
            val sb = StringBuilder()
            if (sizes.isNullOrEmpty()) {
                sb.append("Get PreviewSize failed")
            } else {
                // 当前分辨率
                val curCameraRequest = UVCCameraTool.getCurrentCamera()?.getCameraRequest()
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
    }

    override fun initData() {
       //打开摄像头
        UVCCameraTool.setQuickCameraMode(false)
        UVCCameraTool.openCameraPreview(0, binding.textureView)
    }

    override fun initViewObservable() {

    }

    override fun onDestroy() {
        super.onDestroy()
        //关闭摄像头
        thread {
            UVCCameraTool.closeCamera(true)
            UVCCameraTool.setQuickCameraMode(true)
        }
    }

    /**
     * 切换摄像头
     */
    private fun switchCamera(index: Int) {
        thread {
            UVCCameraTool.closeCamera(true)
            UVCCameraTool.openCameraPreview(index, binding.textureView)
        }
    }
}