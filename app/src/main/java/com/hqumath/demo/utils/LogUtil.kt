package com.hqumath.demo.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.hqumath.demo.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


object LogUtil {

    private val isDebug: Boolean = BuildConfig.DEBUG //是否需要打印bug，buildTypes.debug中配置
    private const val TAG: String = "DEBUG"

    // 日志文件最大大小：5MB
    //private const val MAX_FILE_SIZE = 5 * 1024 * 1024L
    // 最大保留日志文件数量 14天
    private const val MAX_FILE_COUNT = 14

    // 日志文件命名格式
    private val DATE_FORMAT_FILE = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
    private val DATE_FORMAT_LOG = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA)

    // 单线程池处理日志写入，避免多线程并发问题
    private val writeExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var logDir: File

    /**
     * 初始化日志模块（建议在Application的onCreate中调用）
     */
    fun init(context: Context) {
        logDir = File(context.getExternalFilesDir("logs"), "")
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
    }

    //////////////////////////////自定义日志级别：VERBOSE/DEBUG/INFO/WARN/ERROR
    /**
     * @param msg 日志内容
     */
    fun v(msg: String) {
        v(TAG, msg)
    }

    fun d(msg: String) {
        d(TAG, msg)
    }

    fun i(msg: String) {
        i(TAG, msg)
    }

    fun w(msg: String) {
        w(TAG, msg)
    }

    fun e(msg: String) {
        e(TAG, msg)
    }

    /**
     * @param tag 日志类型
     * @param msg 日志内容
     */
    fun v(tag: String, msg: String) {
        v(tag, msg, false)
    }

    fun d(tag: String, msg: String) {
        d(tag, msg, false)
    }

    fun i(tag: String, msg: String) {
        i(tag, msg, false)
    }

    fun w(tag: String, msg: String) {
        w(tag, msg, false)
    }

    fun e(tag: String, msg: String) {
        e(tag, msg, false)
    }

    /**
     * @param tag 日志类型
     * @param msg 日志内容
     * @param writeLog 是否写到文件
     */
    fun v(tag: String, msg: String, writeLog: Boolean) {
        if (isDebug) {
            Log.v(tag, msg)
        }
        if (writeLog) {
            writeLog("V", tag, msg)
        }
    }

    fun d(tag: String, msg: String, writeLog: Boolean) {
        if (isDebug) {
            Log.d(tag, msg)
        }
        if (writeLog) {
            writeLog("D", tag, msg)
        }
    }

    fun i(tag: String, msg: String, writeLog: Boolean) {
        if (isDebug) {
            Log.i(tag, msg)
        }
        if (writeLog) {
            writeLog("I", tag, msg)
        }
    }

    fun w(tag: String, msg: String, writeLog: Boolean) {
        if (isDebug) {
            Log.w(tag, msg)
        }
        if (writeLog) {
            writeLog("W", tag, msg)
        }
    }

    fun e(tag: String, msg: String, writeLog: Boolean) {
        if (isDebug) {
            Log.e(tag, msg)
        }
        if (writeLog) {
            writeLog("E", tag, msg)
        }
    }

    //////////////////////////////写入日志到文件
    /**
     * 写入日志到文
     */
    private fun writeLog(level: String, tag: String, msg: String) {
        // 异步写入文件
        writeExecutor.execute {
            try {
                val currentFile = getCurrentLogFile()
                //rotateIfNeeded() //日志文件超过大小需要分片
                deleteOldFiles()

                // 写入日志内容
                val logContent = "${DATE_FORMAT_LOG.format(Date())} [$level] $tag: $msg\n"
                //writer.write(time + " " + level + "/" + tag + ": " + msg + "\n"); 日志格式
                BufferedWriter(FileWriter(currentFile, true)).use { writer ->
                    writer.appendLine(logContent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "写入日志失败", e)
            }
        }
    }

    /**
     * 获取当前要写入的日志文件
     */
    private fun getCurrentLogFile(): File {
        val fileName = "log_${DATE_FORMAT_FILE.format(Date()).substring(0, 8)}.txt" // 按日期命名
        return File(logDir, fileName)
    }

    /**
     * 删除旧文件，保留指定数量
     */
    private fun deleteOldFiles() {
        val files = logDir.listFiles { file -> file.name.endsWith(".txt") } ?: return
        if (files.size >= MAX_FILE_COUNT) {
            // 按修改时间排序，删除最旧的文件
            Arrays.sort(files) { f1, f2 -> f1.lastModified().compareTo(f2.lastModified()) }
            for (i in 0 until files.size - MAX_FILE_COUNT + 1) {
                files[i].delete()
            }
        }
    }

    //////////////////////////////导出日志文件
    //使用方法 (需要切换到IO线程, Android6.0-9.0需要申请写入权限)
    /*lifecycleScope.launch { //默认Main线程
        val result = withContext(Dispatchers.IO) {
            LogUtil.exportLogsToDownload(mContext)
        }
        CommonUtil.toast(if (result) "日志已导出到下载目录" else "日志导出失败")
    }*/

    /**
     * 导出整个日志目录到Download目录
     * 需要切换到IO线程, Android6.0-9.0需要申请写入权限
     */
    fun exportLogsToDownload(context: Context): Boolean {
        try {
            val zipFile = zipLogDirectory(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 及以上
                exportWithMediaStore(context, zipFile)
            } else { // Android 9 及以下
                exportLegacy(context, zipFile)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "日志导出失败", e)
            return false
        }
    }

    /**
     * 压缩整个日志目录
     */
    private fun zipLogDirectory(context: Context): File {
        val device = Build.MODEL.replace(" ", "_")
        val version = BuildConfig.VERSION_CODE
        val zipFile = File(context.externalCacheDir, "log_${device}_${version}_${DATE_FORMAT_FILE.format(Date())}.zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            logDir.listFiles()?.forEach { file ->
                FileInputStream(file).use { fis ->
                    zos.putNextEntry(ZipEntry(file.name))
                    fis.copyTo(zos)
                    zos.closeEntry()
                }
            }
        }
        return zipFile
    }

    /**
     * 导出文件到Download目录，Android 10 及以上
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun exportWithMediaStore(context: Context, zipFile: File) {
        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, zipFile.name)
            put(MediaStore.Downloads.MIME_TYPE, "application/zip")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val itemUri = resolver.insert(collection, contentValues) ?: throw Exception("MediaStore insert failed")
        resolver.openOutputStream(itemUri)?.use { outputStream ->
            FileInputStream(zipFile).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        contentValues.clear()
        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(itemUri, contentValues, null, null)
    }

    /**
     * 导出文件到Download目录，Android 9 及以下
     * Android6.0-9.0需要申请写入权限
     */
    private fun exportLegacy(context: Context, zipFile: File) {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadDir.exists()) downloadDir.mkdirs()
        val targetFile = File(downloadDir, zipFile.name)
        zipFile.copyTo(targetFile, overwrite = true)
        /*AndPermission.with(context)
            .runtime()
            .permission(Permission.WRITE_EXTERNAL_STORAGE)
            .onGranted { permissions ->
                //...
            }
            .onDenied { permissions ->
                if (AndPermission.hasAlwaysDeniedPermission(context, permissions)) {
                    PermissionUtil.showSettingDialog(context, permissions)//自定义弹窗 去设置界面
                }
            }
            .start()*/
    }
}