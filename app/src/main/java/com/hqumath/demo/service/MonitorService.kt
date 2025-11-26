package com.hqumath.demo.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.camera2.CaptureRequest
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Size
import androidx.annotation.MainThread
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExtendableBuilder
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.hqumath.demo.R
import com.hqumath.demo.app.Constant
import com.hqumath.demo.utils.FileUtil
import com.hqumath.demo.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date


class MonitorService : Service() {
    private val TAG: String = "MonitorService"
    private val binder: IBinder = LocalBinder()
    private val lifecycleOwner = ServiceLifecycleOwner() // 自定义 LifecycleOwner
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var useCamera = false

    ////////抓拍相关
    private val lockCamera = Object() //线程锁
    private val lockPicture = Object()
    private val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "com.tgdz.ione"
        const val CHANNEL_NAME = "IOne Service"
    }

    inner class LocalBinder : Binder() {
        fun getService(): MonitorService = this@MonitorService
    }

    //绑定时启动服务
    override fun onBind(intent: Intent?): IBinder? {
        lifecycleOwner.handleServiceStart() // 服务绑定时，将生命周期置为 STARTED
        startForegroundService()
        return binder
    }

    //解绑时关闭服务
    override fun onUnbind(intent: Intent?): Boolean {
        lifecycleOwner.handleServiceStop()  // 服务解绑时，将生命周期置为 DESTROYED
        stopForegroundService()
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy() //清理资源
    }

    // 开启前台服务
    fun startForegroundService() {
        val notification = buildNotification() //构建通知
        startForeground(NOTIFICATION_ID, notification) //启动前台服务
        LogUtil.d(TAG, "服务已启动")
    }

    // 关闭前台服务
    fun stopForegroundService() {
        stopForeground(true) // true 表示同时移除通知
        stopSelf() //停止服务
        LogUtil.d(TAG, "服务已关闭")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // 或 IMPORTANCE_DEFAULT, IMPORTANCE_LOW
            ).apply {
                description = "IOne Service Channel Description"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ione")
            .setContentText("正在运行...")
            .setSmallIcon(R.mipmap.ic_launcher) // 确保有此资源
            .setOngoing(true) // 设置为持续通知，用户通常无法手动划掉（Android 13+行为可能有变）
            // .setContentIntent(pendingIntent) // 可选：点击通知执行的操作
            .build()
    }

    // 打开相机+预览
    @SuppressLint("UnsafeOptInUsageError")
    fun openCameraPreview(surfaceProvider: SurfaceProvider) {
        if (useCamera) {
            closeCamera()
        }
        useCamera = true
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({ //切换到主线程
            try {
                //CameraProvider
                cameraProvider = cameraProviderFuture.get()
                //获取指定摄像头
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                //预览
                val previewBuilder = Preview.Builder()
                configureCamera2Options(previewBuilder)
                val preview = previewBuilder.build()
                preview.setSurfaceProvider(surfaceProvider)
                //拍照
                val captureBuilder = ImageCapture.Builder()
                configureCamera2Options(captureBuilder)
                imageCapture = captureBuilder
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    //.setTargetRotation(Surface.ROTATION_90) //设置目标旋转
                    .setTargetResolution(
                        Size(1080, 1920)
                    ) //设置期望的最小输出分辨率。CameraX会选择不小于该值的最接近设备支持分辨率。提供一定程度的分辨率控制。如果不存在，则选择小于它的最接近分辨率。需通过 attachedSurfaceResolution 获取实际值
                    .build()
                //确保先解绑所有用例。在主线程执行
                cameraProvider?.unbindAll()
                //将用例绑定到生命周期所有者 和 CameraProvider
                val camera = cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtil.d(TAG, "打开相机异常 openCameraPreview")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    //关闭相机
    @MainThread
    fun closeCamera() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        useCamera = false
    }

    //相机白平衡
    @SuppressLint("UnsafeOptInUsageError")
    private fun <T : androidx.camera.core.UseCase> configureCamera2Options(builder: ExtendableBuilder<T>) {
        val extender = Camera2Interop.Extender(builder)

//        extender.setCaptureRequestOption(
//            CaptureRequest.CONTROL_AWB_MODE, //是否启用自动白平衡
//            CaptureRequest.CONTROL_AWB_MODE_OFF //关闭自动白平衡（必须手动设置）
//        )
//        extender.setCaptureRequestOption(
//            CaptureRequest.COLOR_CORRECTION_MODE, //白平衡模式处理方式
//            CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX //手动模式：由你提供 RGGB 增益
//        )
//        extender.setCaptureRequestOption(
//            CaptureRequest.COLOR_CORRECTION_GAINS, //RGGB 增益矩阵（你的色温/白平衡参数）（R/G/G/B 四通道）
//            CameraUtil.KelvinToRggb(3600) // 5000K → RGGB //3200 - 6400 都试一下
//        )

        extender.setCaptureRequestOption(
            CaptureRequest.CONTROL_AWB_MODE, //是否启用自动白平衡
            CaptureRequest.CONTROL_AWB_MODE_FLUORESCENT //CONTROL_AWB_MODE_FLUORESCENT 荧光灯
        )
    }

    //////////////////////////////////////抓拍
    /**
     * 初始化相机 - 拍照 - 释放相机
     */
    fun quickCamera(channel_no: Int): String {
        if (Constant.isCameraTest)
            return ""
        if (useCamera) {
            closeCamera()
        }
        useCamera = true
        var filePath = ""
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({ //切换到主线程
            try {
                //CameraProvider
                cameraProvider = cameraProviderFuture.get()
                //获取指定摄像头
                val cameraSelector = getCameraSelector(cameraProvider!!, channel_no)
                if (cameraSelector == null)
                    throw IllegalStateException("相机状态不正常")
                //拍照
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY) //设置捕获模式为最小化延迟。可根据场景权衡延迟和质量
                    //.setTargetRotation(Surface.ROTATION_90) //设置目标旋转
                    .setTargetResolution(
                        Size(1080, 1920)
                    ) //设置期望的最小输出分辨率。CameraX会选择不小于该值的最接近设备支持分辨率。提供一定程度的分辨率控制。如果不存在，则选择小于它的最接近分辨率。需通过 attachedSurfaceResolution 获取实际值
                    .build()
                //确保先解绑所有用例。在主线程执行
                cameraProvider?.unbindAll()
                //将用例绑定到生命周期所有者 和 CameraProvider
                cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture)
                lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) { //切换到工作协程
                    //开始拍照
                    filePath = takePicture(channel_no, true)
                    //唤醒线程
                    synchronized(lockCamera) {
                        lockCamera.notifyAll()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtil.d(TAG, "打开相机异常 quickCamera")
                //唤醒线程
                synchronized(lockCamera) {
                    lockCamera.notifyAll()
                }
            }
        }, ContextCompat.getMainExecutor(this))
        //阻塞线程
        synchronized(lockCamera) {
            try {
                lockCamera.wait()
            } catch (e: InterruptedException) {
                e.printStackTrace()
                LogUtil.d(TAG, "线程锁异常 lockCamera")
            }
        }
        return filePath
    }

    /**
     * 拍照
     * @param channel_no 相机序号
     * @param close 拍照后是否关闭相机
     * @return 图片路径
     */
    fun takePicture(channel_no: Int, close: Boolean): String {
        var filePath = ""
        if (imageCapture == null)
            return filePath
        val name = sdf.format(Date()) + "_$channel_no.jpg"
        val file = FileUtil.getExternalFile("picture", name)
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture!!.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(getApplication()),
            object : ImageCapture.OnImageSavedCallback { //切换到主线程
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    filePath = file.absolutePath
                    val msg = "拍照成功: $filePath"
                    LogUtil.d(TAG, msg)
                    if (close)
                        closeCamera()
                    //唤醒线程
                    synchronized(lockPicture) {
                        lockPicture.notifyAll()
                    }
                }

                override fun onError(error: ImageCaptureException) {
                    val msg = "拍照失败: ${error.message}"
                    LogUtil.d(TAG, msg)
                    if (close)
                        closeCamera()
                    //唤醒线程
                    synchronized(lockPicture) {
                        lockPicture.notifyAll()
                    }
                }
            })
        //阻塞线程
        synchronized(lockPicture) {
            try {
                lockPicture.wait()
            } catch (e: InterruptedException) {
                e.printStackTrace()
                LogUtil.d(TAG, "线程锁异常 lockPicture")
            }
        }
        return filePath
    }

    /**
     * 获取指定摄像头
     * @param channel_no 相机序号。channel_no:0 对应配置文件中的1
     */
    private fun getCameraSelector(
        cameraProvider: ProcessCameraProvider,
        channel_no: Int
    ): CameraSelector? {
        //获取所有摄像头信息
        val cameraInfos = cameraProvider.availableCameraInfos
        LogUtil.d(TAG, "相机数量: ${cameraInfos.size}, 指定: $channel_no")
        var cameraSelector: CameraSelector? = null
        if (channel_no < cameraInfos.size) {
            cameraSelector = cameraInfos.get(channel_no).cameraSelector
        }
        return cameraSelector
    }
}
