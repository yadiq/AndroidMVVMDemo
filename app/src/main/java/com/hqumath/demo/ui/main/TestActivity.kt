package com.hqumath.demo.ui.main

import android.hardware.usb.UsbDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hqumath.demo.databinding.ActivityTestBinding
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.CameraActivity
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.ausbc.utils.ToastUtils
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.widget.IAspectRatio

class TestActivity : CameraActivity() {
    private lateinit var binding: ActivityTestBinding

    override fun getRootView(inflater: LayoutInflater): View? {
        binding = ActivityTestBinding.inflate(inflater)
        return binding.root
    }

    override fun initView() {
    }

    override fun getCameraView(): IAspectRatio? {
        return AspectRatioTextureView(this)
    }

    override fun getCameraViewContainer(): ViewGroup? {
        return binding.cameraViewContainer
    }

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

    private fun handleCameraClosed() {
//        mViewBinding.uvcLogoIv.visibility = View.VISIBLE
//        mViewBinding.frameRateTv.visibility = View.GONE
        ToastUtils.show("camera closed success")
    }

    private fun handleCameraError(msg: String?) {
//        mViewBinding.uvcLogoIv.visibility = View.VISIBLE
//        mViewBinding.frameRateTv.visibility = View.GONE
        ToastUtils.show("camera opened error: $msg")
    }

    //默认打开的相机 TODO
//    override fun getDefaultCamera(): UsbDevice? {
//        return super.getDefaultCamera()
//    }

    override fun getCameraRequest(): CameraRequest {
        return CameraRequest.Builder()
            .setPreviewWidth(1280) //预览分辨率
            .setPreviewHeight(720)
            .setRenderMode(CameraRequest.RenderMode.OPENGL) //渲染模式
            .setDefaultRotateType(RotateType.ANGLE_0) //opengl模式下旋转相机图像
//            .setAudioSource(CameraRequest.AudioSource.SOURCE_SYS_MIC)
            .setPreviewFormat(CameraRequest.PreviewFormat.FORMAT_MJPEG) //设置预览格式，推荐MJPEG
            .setAspectRatioShow(true) //aspect render,default is true
            .setCaptureRawImage(false) //opengl模式下捕获raw image图片
            .setRawPreviewData(false) //opengl模式下预览原始图像
            .create()
    }

}