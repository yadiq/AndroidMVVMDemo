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

    private var mCameraClient: MultiCameraClient? = null
    private val mCameraMap = linkedMapOf<Int, MultiCameraClient.ICamera>() //相机列表
    private var mCurrentCamera: MultiCameraClient.ICamera? = null //当前相机
    private var mCameraView: IAspectRatio? = null //预览view

    private var captureFilePath = "" //拍照图片路径
    private val lockCamera = Object() //线程锁
    private var isQuickCameraMode = true //是否抓拍模式

    fun init() {
        registerMultiCamera()
    }

    fun release() {
        unRegisterMultiCamera()
    }

    /**
     * 相机数量
     */
    fun getCameraSize() = mCameraMap.size

    fun setQuickCameraMode(isQuickCameraMode: Boolean) {
        this.isQuickCameraMode = isQuickCameraMode
    }

    fun getDeviceList() = mCameraClient?.getDeviceList()

    fun getCurrentCamera() = mCurrentCamera

    /**
     * 抓拍
     * 请求权限-设备连接成功-打开相机-拍照-关闭相机-设备断开连接
     */
    fun quickCamera(index: Int, textureView: IAspectRatio): String {
        if (!isQuickCameraMode) {
            LogUtil.d(TAG, "quickCamera 非抓拍模式")
            return ""
        }
        LogUtil.d(TAG, "quickCamera size=${mCameraMap.size} select=$index")
        captureFilePath = ""
        if (mCameraMap.size <= index) {
            return captureFilePath
        }
        var camera = mCameraMap.entries.elementAt(index)
        //更新View
        mCameraView = textureView
        //请求权限
        val result = mCameraClient?.requestPermission(camera.value.getUsbDevice())
        if (result == true) {
            //任务未执行完成，需要阻塞线程
            synchronized(lockCamera) {
                try {
                    lockCamera.wait()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        return captureFilePath
    }

    /**
     * 预览
     * 请求权限-设备连接成功-打开相机
     */
    fun openCameraPreview(index: Int, textureView: IAspectRatio) {
        LogUtil.d(TAG, "openCameraPreview size=${mCameraMap.size} select=$index")
        if (mCameraMap.size <= index) {
            return
        }
        var camera = mCameraMap.entries.elementAt(index)
        //更新View
        mCameraView = textureView
        //请求权限
        mCameraClient?.requestPermission(camera.value.getUsbDevice())
    }

    private fun registerMultiCamera() {
        mCameraClient = MultiCameraClient(CommonUtil.getContext(), object : IDeviceConnectCallBack {
            override fun onAttachDev(device: UsbDevice?) { //设备插入时执行
                device ?: return
                if (mCameraMap.containsKey(device.deviceId)) {
                    return
                }
                LogUtil.d(TAG, "onAttachDev deviceId:${device.deviceId}")
                mCameraMap[device.deviceId] = CameraUVC(CommonUtil.getContext(), device)
            }

            override fun onDetachDec(device: UsbDevice?) { //设备拔出时执行
                device ?: return
                LogUtil.d(TAG, "onDetachDec deviceId:${device.deviceId}")
                mCameraMap.remove(device.deviceId)?.apply {
                    setUsbControlBlock(null)
                }
            }

            override fun onCancelDev(device: UsbDevice?) { //请求权限失败后执行
                LogUtil.d(TAG, "onCancelDev deviceId:${device?.deviceId}")
                //唤醒线程
                synchronized(lockCamera) {
                    lockCamera.notifyAll()
                }
            }

            override fun onConnectDev(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) { //请求权限成功后，设备连接时执行
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

            override fun onDisConnectDec(
                device: UsbDevice?,
                ctrlBlock: USBMonitor.UsbControlBlock?,
            ) { //关闭摄像头后，设备连接断开时执行
                LogUtil.d(TAG, "onDisConnectDec deviceId:${device?.deviceId}")
                closeCamera(false) //关闭相机
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
        //唤醒线程
        synchronized(lockCamera) {
            lockCamera.notifyAll()
        }
    }

    private fun openCamera() {
        LogUtil.d(TAG, "openCamera")
        mCurrentCamera?.openCamera(mCameraView, getCameraRequest())
        mCurrentCamera?.setCameraStateCallBack(object : ICameraStateCallBack {
            override fun onCameraState(
                self: MultiCameraClient.ICamera,
                code: ICameraStateCallBack.State,
                msg: String?,
            ) {
                when (code) {
                    ICameraStateCallBack.State.OPENED -> {
                        LogUtil.d(TAG, "openCamera success, start captureImage")
                        if (isQuickCameraMode) { //抓拍模式
                            captureImage() //拍照
                        }
                    }

                    ICameraStateCallBack.State.CLOSED -> {
                        LogUtil.d(TAG, "closeCamera success")
                        //唤醒线程
                        synchronized(lockCamera) {
                            lockCamera.notifyAll()
                        }
                    }

                    ICameraStateCallBack.State.ERROR -> {
                        LogUtil.d(TAG, "openCamera error, msg=$msg")
                        //唤醒线程
                        synchronized(lockCamera) {
                            lockCamera.notifyAll()
                        }
                    }
                }
            }

        })
    }

    fun closeCamera(withLock: Boolean) {
        mCurrentCamera?.closeCamera() //关闭相机
        mCurrentCamera = null
        if (withLock) {
            //任务未执行完成，需要阻塞线程
            synchronized(lockCamera) {
                try {
                    lockCamera.wait()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun captureImage() {
        val fileName = sdf.format(Date()) + ".jpg"
        val filePath = FileUtil.getExternalFile("picture", fileName).absolutePath
        mCurrentCamera?.captureImage(object : ICaptureCallBack {
            override fun onBegin() {
            }

            override fun onError(error: String?) {
                //CommonUtil.toast(error ?: "未知异常")
                LogUtil.d(TAG, "captureImage error, msg=$error")
                closeCamera(false) //关闭相机
            }

            override fun onComplete(path: String?) {
                CommonUtil.toast("拍照完成")
                LogUtil.d(TAG, "captureImage success, path=$path")
                captureFilePath = path ?: ""
                closeCamera(false) //关闭相机
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