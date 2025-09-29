package com.hqumath.demo.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.hqumath.demo.R

class MonitorService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "com.tgdz.ione"
        const val CHANNEL_NAME = "IOne Service"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 构建通知
        val notification = buildNotification()

        // 启动前台服务
        startForeground(NOTIFICATION_ID, notification)

        // 在这里执行你需要的长时间运行任务（例如轮询、保持连接等）
        // 注意：默认是在主线程执行，耗时任务请开子线程

        // 如果服务被杀死，告诉系统如何重启
        return START_STICKY //系统会重建服务
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // 本例未使用绑定服务
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

    override fun onDestroy() {
        super.onDestroy()
        // 清理资源
    }
}