package com.hqumath.demo.ui.main

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hqumath.demo.databinding.FragmentDemoBinding
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.CameraFragment
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.utils.ToastUtils
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.widget.CaptureMediaView
import com.jiangdg.ausbc.widget.IAspectRatio

class DemoFragment : CameraFragment(), View.OnClickListener, CaptureMediaView.OnViewClickListener {
    private lateinit var mViewBinding: FragmentDemoBinding

    override fun getRootView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): View? {
        mViewBinding = FragmentDemoBinding.inflate(inflater, container, false)
        return mViewBinding.root
    }

    override fun getCameraView(): IAspectRatio {
        return AspectRatioTextureView(requireContext())
    }

    override fun getCameraViewContainer(): ViewGroup? {
        return mViewBinding.cameraViewContainer
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