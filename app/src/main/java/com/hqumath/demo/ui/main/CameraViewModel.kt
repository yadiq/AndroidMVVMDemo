package com.hqumath.demo.ui.main

import android.app.Application
import android.content.Context
import android.hardware.usb.UsbDevice
import android.view.Gravity
import android.view.SurfaceView
import android.view.TextureView
import android.view.ViewGroup
import androidx.lifecycle.AndroidViewModel
import com.hqumath.demo.repository.MyModel
import com.hqumath.demo.utils.LogUtil
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.CameraActivity
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.callback.ICaptureCallBack
import com.jiangdg.ausbc.callback.IDeviceConnectCallBack
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.ausbc.utils.Logger
import com.jiangdg.ausbc.utils.SettableFuture
import com.jiangdg.ausbc.utils.ToastUtils
import com.jiangdg.ausbc.widget.IAspectRatio
import com.jiangdg.usb.USBMonitor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.remove

/**
 * ui无关
 */
class CameraViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "CameraViewModel"

    var mCameraView: IAspectRatio? = null
    var mCameraClient: MultiCameraClient? = null
    val mCameraMap = hashMapOf<Int, MultiCameraClient.ICamera>()
    var mCurrentCamera: SettableFuture<MultiCameraClient.ICamera>? = null

    val mRequestPermission: AtomicBoolean by lazy {
        AtomicBoolean(false)
    }

    init {

    }

    override fun onCleared() {
        super.onCleared()
        unRegisterMultiCamera()
    }

    fun registerMultiCamera(ctx: Context) {
        mCameraClient = MultiCameraClient(ctx, object : IDeviceConnectCallBack {
            override fun onAttachDev(device: UsbDevice?) {
                device ?: return
                if (mCameraMap.containsKey(device.deviceId)) {
                    return
                }
                generateCamera(ctx, device).apply {
                    mCameraMap[device.deviceId] = this
                }
                // Initiate permission request when device insertion is detected
                // If you want to open the specified camera, you need to override getDefaultCamera()
                if (mRequestPermission.get()) {
                    return
                }
                getDefaultCamera()?.apply {
                    if (vendorId == device.vendorId && productId == device.productId) {
                        LogUtil.d(TAG, "default camera pid: $productId, vid: $vendorId")
                        requestPermission(device)
                    }
                    return
                }
                requestPermission(device)
            }

            override fun onDetachDec(device: UsbDevice?) {
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

            override fun onConnectDev(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
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
                    openCamera(mCameraView)
                    Logger.i(TAG, "camera connection. pid: ${device.productId}, vid: ${device.vendorId}")
                }
            }

            override fun onDisConnectDec(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
                closeCamera()
                mRequestPermission.set(false)
            }

            override fun onCancelDev(device: UsbDevice?) {
                mRequestPermission.set(false)
                try {
                    mCurrentCamera?.cancel(true)
                    mCurrentCamera = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
        mCameraClient?.register()
    }

    fun unRegisterMultiCamera() {
        mCameraMap.values.forEach {
            it.closeCamera()
        }
        mCameraMap.clear()
        mCameraClient?.unRegister()
        mCameraClient?.destroy()
        mCameraClient = null
    }

    fun getDeviceList() = mCameraClient?.getDeviceList()

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
    protected fun requestPermission(device: UsbDevice?) {
        mRequestPermission.set(true)
        mCameraClient?.requestPermission(device)
    }

    /**
     * Generate camera
     *
     * @param ctx context [Context]
     * @param device Usb device, see [UsbDevice]
     * @return Inheritor assignment camera api policy
     */
    protected open fun generateCamera(ctx: Context, device: UsbDevice): MultiCameraClient.ICamera {
        return CameraUVC(ctx, device)
    }

    /**
     * Get default camera TODO 260行
     *
     * @return Open camera by default, should be [UsbDevice]
     */
    protected open fun getDefaultCamera(): UsbDevice? = null

    /**
     * Capture image
     *
     * @param callBack capture status, see [ICaptureCallBack]
     * @param savePath custom image path
     */
    protected fun captureImage(callBack: ICaptureCallBack, savePath: String? = null) {
        getCurrentCamera()?.captureImage(callBack, savePath)
    }


    /**
     * Get default effect
     */
    protected fun getDefaultEffect() = getCurrentCamera()?.getDefaultEffect()

    /**
     * Switch camera
     *
     * @param usbDevice camera usb device
     */
    fun switchCamera(usbDevice: UsbDevice) {
        getCurrentCamera()?.closeCamera()
        try {
            Thread.sleep(500)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        requestPermission(usbDevice)
    }

    /**
     * Is camera opened
     *
     * @return camera open status
     */
    protected fun isCameraOpened() = getCurrentCamera()?.isCameraOpened()  ?: false

    /**
     * Update resolution
     *
     * @param width camera preview width
     * @param height camera preview height
     */
    protected fun updateResolution(width: Int, height: Int) {
        getCurrentCamera()?.updateResolution(width, height)
    }


    //TODO 842行
    protected fun openCamera(st: IAspectRatio? = null) {
        when (st) {
            is TextureView, is SurfaceView -> {
                st
            }
            else -> {
                null
            }
        }.apply {
            getCurrentCamera()?.openCamera(this, getCameraRequest())
            getCurrentCamera()?.setCameraStateCallBack(object : ICameraStateCallBack {
                override fun onCameraState(
                    self: MultiCameraClient.ICamera,
                    code: ICameraStateCallBack.State,
                    msg: String?
                ) {
                    when (code) { //TODO
                        ICameraStateCallBack.State.OPENED -> ToastUtils.show("camera opened success")
                        ICameraStateCallBack.State.CLOSED -> ToastUtils.show("camera closed success")
                        ICameraStateCallBack.State.ERROR -> ToastUtils.show("camera opened error: $msg")
                    }
                }

            })
        }
    }

    protected fun closeCamera() {
        getCurrentCamera()?.closeCamera()
    }

    fun surfaceSizeChanged(surfaceWidth: Int, surfaceHeight: Int) {
        getCurrentCamera()?.setRenderSize(surfaceWidth, surfaceHeight)
    }


    /**
     * Camera render view show gravity
     */
    protected open fun getGravity() = Gravity.CENTER

    protected open fun getCameraRequest(): CameraRequest {
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