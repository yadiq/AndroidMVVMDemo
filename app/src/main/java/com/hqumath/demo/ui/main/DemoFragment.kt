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
import com.jiangdg.ausbc.utils.ToastUtils
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.widget.CaptureMediaView
import com.jiangdg.ausbc.widget.IAspectRatio

class DemoFragment : CameraFragment(), View.OnClickListener, CaptureMediaView.OnViewClickListener {
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
            getCurrentCamera()?.let { strategy ->
                if (strategy is CameraUVC) {
                    val sb = StringBuilder()
                    val curDevice = strategy.getUsbDevice()
                    val curDeviceInfo =
                        "当前: ,${curDevice.productName},${curDevice.deviceName},${curDevice.productId}"
                    sb.append(curDeviceInfo).append("\n")
                    LogUtil.d(curDeviceInfo)
                    //相机列表
                    val usbDeviceList: MutableList<UsbDevice>? = getDeviceList()
                    if (usbDeviceList.isNullOrEmpty()) {
                        ToastUtils.show("Get usb device failed")
                        return@let
                    }
                    for (index in (0 until usbDeviceList.size)) {
                        val dev = usbDeviceList[index]
                        val cameraInfo =
                            "相机${index},${dev.productName},${dev.deviceName},${dev.productId}"
                        sb.append(cameraInfo).append("\n")
                        LogUtil.d(cameraInfo)
                    }
                    binding.tvInfo.setText(sb.toString())
                }
            }
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

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }

    override fun onViewClick(mode: CaptureMediaView.CaptureMode?) {
        TODO("Not yet implemented")
    }

    /////////////////////////
    private fun handleCameraError(msg: String?) {
//        mViewBinding.uvcLogoIv.visibility = View.VISIBLE
//        mViewBinding.frameRateTv.visibility = View.GONE
        ToastUtils.show("camera opened error: $msg")
    }

    private fun handleCameraClosed() {
//        mViewBinding.uvcLogoIv.visibility = View.VISIBLE
//        mViewBinding.frameRateTv.visibility = View.GONE
        ToastUtils.show("camera closed success")
    }

    private fun handleCameraOpened() {
//        mViewBinding.uvcLogoIv.visibility = View.GONE
//        mViewBinding.frameRateTv.visibility = View.VISIBLE
//        mViewBinding.brightnessSb.max = (getCurrentCamera() as? CameraUVC)?.getBrightnessMax() ?: 100
//        mViewBinding.brightnessSb.progress = (getCurrentCamera() as? CameraUVC)?.getBrightness() ?: 0
//        Logger.i(com.jiangdg.demo.DemoFragment.Companion.TAG, "max = ${mViewBinding.brightnessSb.max}, progress = ${mViewBinding.brightnessSb.progress}")
//        mViewBinding.brightnessSb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                (getCurrentCamera() as? CameraUVC)?.setBrightness(progress)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//
//            }
//        })
        ToastUtils.show("camera opened success")
    }
}