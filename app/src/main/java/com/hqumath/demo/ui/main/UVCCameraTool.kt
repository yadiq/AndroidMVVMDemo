package com.hqumath.demo.ui.main

import android.hardware.usb.UsbDevice
import com.hqumath.demo.utils.CommonUtil
import com.hqumath.demo.utils.FileUtil
import com.hqumath.demo.utils.LogUtil
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.callback.ICaptureCallBack
import com.jiangdg.ausbc.callback.IDeviceConnectCallBack
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.ausbc.widget.IAspectRatio
import com.jiangdg.usb.USBMonitor
import java.text.SimpleDateFormat
import java.util.Date

object UVCCameraTool {
    private val TAG = "UVCCameraTool"
    private val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")

    private var mCameraView: IAspectRatio? = null
    private var mCameraClient: MultiCameraClient? = null
    val mCameraMap = linkedMapOf<Int, MultiCameraClient.ICamera>() //缓存相机 hashMapOf
    private var mCurrentCamera: MultiCameraClient.ICamera? = null

    fun init() {
        registerMultiCamera()
    }

    fun release() {
        unRegisterMultiCamera()
    }

    fun quickCamera(index: Int, textureView: IAspectRatio) {
        //请求权限-设备连接成功-打开相机(开启预览)-拍照-关闭相机
        LogUtil.d(TAG, "打开相机 size=${mCameraMap.size} select=$index")
        if (mCameraMap.size <= index) {
            return
        }
        var camera = mCameraMap.entries.elementAt(index)
        //更新View
        mCameraView = textureView
        //mCameraView = AspectRatioTextureView(CommonUtil.getContext())
        //请求权限
        mCameraClient?.requestPermission(camera.value.getUsbDevice())
    }

    private fun registerMultiCamera() {
        mCameraClient = MultiCameraClient(CommonUtil.getContext(), object : IDeviceConnectCallBack {
            override fun onAttachDev(device: UsbDevice?) { //设备插入
                device?.let {
                    if (mCameraMap.containsKey(device.deviceId)) {
                        return
                    }
                    mCameraMap[device.deviceId] = CameraUVC(CommonUtil.getContext(), device)
                    LogUtil.d(TAG, "onAttachDev deviceId:${device.deviceId}")
                }
            }

            override fun onDetachDec(device: UsbDevice?) { //设备断开
                LogUtil.d(TAG, "onDetachDec deviceId:${device?.deviceId}")
                mCameraMap.remove(device?.deviceId)?.apply {
                    setUsbControlBlock(null)
                }
            }

            override fun onCancelDev(device: UsbDevice?) {
                LogUtil.d(TAG, "onCancelDev deviceId:${device?.deviceId}")
            }

            override fun onConnectDev(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) { //设备连接成功
                LogUtil.d(TAG, "onConnectDev deviceId:${device?.deviceId}")
                device ?: return
                ctrlBlock ?: return
                mCameraMap[device.deviceId]?.apply {
                    setUsbControlBlock(ctrlBlock)
                }?.also { camera ->
                    mCurrentCamera = camera
                    openCamera() //打开相机
                }
            }

            override fun onDisConnectDec(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
                LogUtil.d(TAG, "onDisConnectDec deviceId:${device?.deviceId}")
                closeCamera() //关闭相机
            }
        })
        mCameraClient?.register()
    }

    private fun unRegisterMultiCamera() {
        mCameraMap.values.forEach {
            it.closeCamera()
        }
        mCameraMap.clear()
        mCameraClient?.unRegister()
        mCameraClient?.destroy()
        mCameraClient = null
    }

    private fun openCamera() {
        LogUtil.d(TAG, "openCamera")
        mCurrentCamera?.openCamera(mCameraView, getCameraRequest())
        mCurrentCamera?.setCameraStateCallBack(object: ICameraStateCallBack {
            override fun onCameraState(
                self: MultiCameraClient.ICamera,
                code: ICameraStateCallBack.State,
                msg: String?,
            ) {
                when (code) {
                    ICameraStateCallBack.State.OPENED -> {
                        LogUtil.d(TAG, "openCamera OPENED")
                        //更新UI 显示帧率 可以拍照
                        //CommonUtil.toast("可以拍照")
                        captureImage() //拍照
                    }
                    //handleCameraOpened()
                    ICameraStateCallBack.State.CLOSED -> {}
                    //handleCameraClosed()
                    ICameraStateCallBack.State.ERROR -> {}
                    //handleCameraError(msg)
                }
            }

        })
    }

    private fun closeCamera() {
        mCurrentCamera?.closeCamera() //关闭相机
        mCurrentCamera = null
    }

    private fun captureImage() {
        val fileName = sdf.format(Date()) + ".jpg"
        val filePath = FileUtil.getExternalFile("picture", fileName).absolutePath
        mCurrentCamera?.captureImage(object: ICaptureCallBack {
            override fun onBegin() {
            }

            override fun onError(error: String?) {
                CommonUtil.toast(error ?: "未知异常")
                LogUtil.d(TAG, "拍照异常 ${error ?: "未知异常"}")
                closeCamera() //关闭相机
            }

            override fun onComplete(path: String?) {
                CommonUtil.toast("拍照完成")
                LogUtil.d(TAG, "拍照完成 $path")
                closeCamera() //关闭相机
            }
        }, filePath)
    }

    /**
     * 相机参数
     */
    private fun getCameraRequest(): CameraRequest {
        return CameraRequest.Builder()
            .setPreviewWidth(1280) //预览/拍照 分辨率，默认 640x480
            .setPreviewHeight(720)
            .setRenderMode(CameraRequest.RenderMode.NORMAL) //渲染模式 OPENGL
            .setDefaultRotateType(RotateType.ANGLE_0) //opengl模式下旋转相机图像
            .setAudioSource(CameraRequest.AudioSource.NONE) //录音源
            .setPreviewFormat(CameraRequest.PreviewFormat.FORMAT_MJPEG) //预览格式
            .setAspectRatioShow(true) //设置宽高比显示
            .setCaptureRawImage(false) //opengl模式下捕获原始图像
            .setRawPreviewData(false) //opengl模式下预览原始图像
            .create()
    }
}