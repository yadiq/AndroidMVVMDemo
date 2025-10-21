package com.hqumath.demo.ui.main

import android.hardware.usb.UsbDevice
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hqumath.demo.databinding.FragmentDemoBinding
import com.hqumath.demo.utils.LogUtil
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.CameraFragment
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.ausbc.utils.ToastUtils
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.widget.IAspectRatio

open class DemoFragment : CameraFragment() {
    private lateinit var binding: FragmentDemoBinding

    override fun getRootView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): View? {
        binding = FragmentDemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {
        super.initView()
        binding.btnGetCameraList.setOnClickListener {
            val sb = StringBuilder()
            //相机列表
            val usbDeviceList: MutableList<UsbDevice>? = getDeviceList()
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
            getCurrentCamera()?.let { strategy ->
                if (strategy is CameraUVC) {
                    val curDevice = strategy.getUsbDevice()
                    val curDeviceInfo =
                        "当前: ,${curDevice.productName},${curDevice.deviceName},${curDevice.productId}"
                    sb.append(curDeviceInfo).append("\n")
                }
            }
            binding.tvInfo.setText(sb.toString())
        }

        binding.btnSwitch.setOnClickListener {
            try {
                val index = Integer.parseInt(binding.edtNum.text.toString())
                switchCamera(getDeviceList()!![index])
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun initData() {
        super.initData()
    }

    override fun getCameraView(): IAspectRatio {
        return AspectRatioTextureView(requireContext())
    }

    override fun getCameraViewContainer(): ViewGroup? {
        return binding.cameraViewContainer
    }

    override fun getGravity(): Int = Gravity.CENTER

    override fun onCameraState(
        self: MultiCameraClient.ICamera,
        code: ICameraStateCallBack.State,
        msg: String?
    ) {
        when (code) {
            ICameraStateCallBack.State.OPENED -> handleCameraOpened()
            ICameraStateCallBack.State.CLOSED -> handleCameraClosed()
            ICameraStateCallBack.State.ERROR -> handleCameraError(msg)
        }
    }

    //打开默认相机
    override fun getDefaultCamera(): UsbDevice? {
        return null
    }

    //相机配置
    override fun getCameraRequest(): CameraRequest {
        return CameraRequest.Builder()
            .setPreviewWidth(640)
            .setPreviewHeight(480)
            .setRenderMode(CameraRequest.RenderMode.OPENGL)
            .setDefaultRotateType(RotateType.ANGLE_0)
//            .setAudioSource(CameraRequest.AudioSource.SOURCE_SYS_MIC)
            .setPreviewFormat(CameraRequest.PreviewFormat.FORMAT_MJPEG)
            .setAspectRatioShow(true)
            .setCaptureRawImage(false)
            .setRawPreviewData(false)
            .create()
    }

    /////////////////////////
    private fun handleCameraError(msg: String?) {
        ToastUtils.show("camera opened error: $msg")
    }

    private fun handleCameraClosed() {
        ToastUtils.show("camera closed success")
    }

    private fun handleCameraOpened() {
        ToastUtils.show("camera opened success")
    }
}