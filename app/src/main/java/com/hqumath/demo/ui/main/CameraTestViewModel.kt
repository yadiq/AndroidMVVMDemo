package com.hqumath.demo.ui.main

import android.app.Application
import android.graphics.SurfaceTexture
import android.hardware.usb.UsbDevice
import android.view.Gravity
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.lifecycle.AndroidViewModel
import com.hqumath.demo.utils.LogUtil
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.callback.IDeviceConnectCallBack
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.ausbc.utils.SettableFuture
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.widget.IAspectRatio
import com.jiangdg.usb.USBMonitor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class CameraTestViewModel (application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "CameraFragment"
    }

    private var mCameraView: IAspectRatio? = null
    private var mCameraClient: MultiCameraClient? = null
    private val mCameraMap = hashMapOf<Int, MultiCameraClient.ICamera>() //缓存相机
    private var mCurrentCamera: SettableFuture<MultiCameraClient.ICamera>? = null
    private var cameraStateCallBack: ICameraStateCallBack? = null

    private val mRequestPermission: AtomicBoolean by lazy { //是否在请求usb权限
        AtomicBoolean(false)
    }

//    init {
//    }

    override fun onCleared() {
        super.onCleared()
        unRegisterMultiCamera()
    }

    /**
     * 相机状态回调
     */
    fun setCameraStateCallBack(callBack: ICameraStateCallBack) {
        cameraStateCallBack = callBack
    }

    fun initCameraView(viewGroup: ViewGroup) {
        val cameraView = AspectRatioTextureView(getApplication())//支持 TextureView 和 SurfaceView
        handleTextureView(cameraView as TextureView)
        viewGroup.apply {
            removeAllViews()
            addView(cameraView, getViewLayoutParams(this))
        }
        mCameraView = cameraView
    }

    private fun registerMultiCamera() {
        mCameraClient = MultiCameraClient(getApplication(), object : IDeviceConnectCallBack {
            override fun onAttachDev(device: UsbDevice?) { //设备插入
                LogUtil.d(TAG, "onAttachDev deviceId:${device?.deviceId}")
                device?.let {
                    if (mCameraMap.containsKey(device.deviceId)) {
                        return
                    }
                    mCameraMap[device.deviceId] = CameraUVC(getApplication(), device)
                    if (mRequestPermission.get()) { //检测到设备插入时，发起权限请求
                        return@let
                    }
                    /*getDefaultCamera()?.apply { //打开指定的摄像头 TODO
                        if (vendorId == device.vendorId && productId == device.productId) {
                            Logger.i(TAG, "default camera pid: $productId, vid: $vendorId")
                            requestPermission(device)
                        }
                        return@let
                    }*/
                    requestPermission(device)
                }
            }

            override fun onDetachDec(device: UsbDevice?) { //设备断开
                LogUtil.d(TAG, "onDetachDec deviceId:${device?.deviceId}")
                mCameraMap.remove(device?.deviceId)?.apply {
                    setUsbControlBlock(null)
                }
                mRequestPermission.set(false)
                try {
                    mCurrentCamera?.cancel(true)
                    mCurrentCamera = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCancelDev(device: UsbDevice?) {
                LogUtil.d(TAG, "onCancelDev deviceId:${device?.deviceId}")
                mRequestPermission.set(false)
                try {
                    mCurrentCamera?.cancel(true)
                    mCurrentCamera = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onConnectDev(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) { //设备连接成功
                LogUtil.d(TAG, "onConnectDev deviceId:${device?.deviceId}")
                device ?: return
                ctrlBlock ?: return
                mCameraMap[device.deviceId]?.apply {
                    setUsbControlBlock(ctrlBlock)
                }?.also { camera ->
                    try {
                        mCurrentCamera?.cancel(true)
                        mCurrentCamera = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    mCurrentCamera = SettableFuture()
                    mCurrentCamera?.set(camera)
                    //openCamera(mCameraView) 打开相机
                    getCurrentCamera()?.openCamera(mCameraView, getCameraRequest())
                    getCurrentCamera()?.setCameraStateCallBack(cameraStateCallBack)
                    LogUtil.d(TAG, "camera connection. pid: ${device.productId}, vid: ${device.vendorId}")
                }
            }

            override fun onDisConnectDec(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
                LogUtil.d(TAG, "onDisConnectDec deviceId:${device?.deviceId}")
                //closeCamera() 关闭相机
                getCurrentCamera()?.closeCamera()
                mRequestPermission.set(false)
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

    fun getDeviceList() = mCameraClient?.getDeviceList()

    private fun handleTextureView(textureView: TextureView) {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                registerMultiCamera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                getCurrentCamera()?.setRenderSize(width, height)
            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                unRegisterMultiCamera()
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
            }
        }
    }

    /**
     * Get current opened camera
     *
     * @return current camera, see [MultiCameraClient.ICamera]
     */
    fun getCurrentCamera(): MultiCameraClient.ICamera? {
        return try {
            mCurrentCamera?.get(2, TimeUnit.SECONDS)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Request permission
     *
     * @param device see [UsbDevice]
     */
    private fun requestPermission(device: UsbDevice?) {
        mRequestPermission.set(true)
        mCameraClient?.requestPermission(device)
    }

    //...

    private fun getViewLayoutParams(viewGroup: ViewGroup): ViewGroup.LayoutParams {
        return when(viewGroup) {
            is FrameLayout -> {
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    getGravity()
                )
            }
            is LinearLayout -> {
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    gravity = getGravity()
                }
            }
            is RelativeLayout -> {
                RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
                ).apply{
                    when(getGravity()) {
                        Gravity.TOP -> {
                            addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
                        }
                        Gravity.BOTTOM -> {
                            addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                        }
                        else -> {
                            addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
                            addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
                        }
                    }
                }
            }
            else -> throw IllegalArgumentException("Unsupported container view, " +
                    "you can use FrameLayout or LinearLayout or RelativeLayout")
        }
    }

    /**
     * Camera render view show gravity
     */
    private fun getGravity() = Gravity.CENTER

    /**
     * TODO 参数
     */
    private fun getCameraRequest(): CameraRequest {
        return CameraRequest.Builder()
            .setPreviewWidth(640)
            .setPreviewHeight(480)
            .setRenderMode(CameraRequest.RenderMode.OPENGL)
            .setDefaultRotateType(RotateType.ANGLE_0)
            .setAudioSource(CameraRequest.AudioSource.SOURCE_SYS_MIC)
            .setPreviewFormat(CameraRequest.PreviewFormat.FORMAT_MJPEG)
            .setAspectRatioShow(true)
            .setCaptureRawImage(false)
            .setRawPreviewData(false)
            .create()
    }







}