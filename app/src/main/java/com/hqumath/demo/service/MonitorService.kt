package com.hqumath.demo.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.hqumath.demo.R
import com.hqumath.demo.utils.LogUtil


class MonitorService : Service() {
    private val TAG: String = "MonitorService"
    private val binder: IBinder = LocalBinder()
    private val lifecycleOwner = ServiceLifecycleOwner() // 自定义 LifecycleOwner
    private var cameraProvider: ProcessCameraProvider? = null
    private var useCamera = false

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

    // 打开相机
    fun openCamera(surfaceProvider: SurfaceProvider) {
        if (useCamera) {
            return
        } else {
            useCamera = true
        }
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                //获取指定摄像头
                val selector = CameraSelector.DEFAULT_BACK_CAMERA
                //预览
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(surfaceProvider)
                //拍照
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetResolution(
                        Size(1080, 1920)
                    ) //设置期望的最小输出分辨率。CameraX会选择不小于该值的最接近设备支持分辨率。提供一定程度的分辨率控制。如果不存在，则选择小于它的最接近分辨率。需通过 attachedSurfaceResolution 获取实际值
                    .build()
                //CameraProvider
                cameraProvider = cameraProviderFuture.get()
                //确保先解绑所有用例。在主线程执行
                cameraProvider?.unbindAll()
                //将用例绑定到生命周期所有者 和 CameraProvider
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtil.d(TAG, "打开相机异常")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // 关闭相机
    fun closeCamera() {
        cameraProvider?.unbindAll()
        useCamera = false
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理资源
    }
}