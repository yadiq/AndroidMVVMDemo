package com.hqumath.demo.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.hqumath.demo.R
import com.hqumath.demo.utils.LogUtil


class MonitorService : Service() {
    private val TAG: String = "MonitorService"
    private val binder: IBinder = LocalBinder()

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
        startForegroundService()
        return binder
    }

    //解绑时关闭服务
    override fun onUnbind(intent: Intent?): Boolean {
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

    // 服务提供的业务方法
    fun performTask(taskData: String?) {
        // 执行某些任务
        LogUtil.d("ForegroundService", "执行任务: " + taskData)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理资源
    }
}