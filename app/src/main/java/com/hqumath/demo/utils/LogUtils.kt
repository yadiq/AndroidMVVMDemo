package com.hqumath.demo.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.hqumath.demo.BuildConfig
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


object LogUtils {

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
                //rotateIfNeeded()
                deleteOldFiles()

                // 写入日志内容
                val logContent = "${DATE_FORMAT_LOG.format(Date())} [$level] $tag: $msg\n"
                //writer.write(time + " " + level + "/" + tag + ": " + msg + "\n"); TODO 日志格式
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
     * 日志文件轮转，文件大小超过阈值则新建文件 TODO
     */
//    private fun rotateIfNeeded() {
//        // 检查文件大小，超过阈值则新建文件
//        if (currentFile.length() >= MAX_FILE_SIZE) {
//            rotateIfNeeded()
//        }
//
//        val files = logDir.listFiles { file -> file.name.endsWith(".txt") } ?: return
//        if (files.size >= MAX_FILE_COUNT) {
//            // 按修改时间排序，删除最旧的文件
//            Arrays.sort(files) { f1, f2 -> f1.lastModified().compareTo(f2.lastModified()) }
//            for (i in 0 until files.size - MAX_FILE_COUNT + 1) {
//                files[i].delete()
//            }
//        }
//    }

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





    /**
     * 导出日志文件到下载目录（供用户访问） TODO
     * @return 导出后的文件路径，失败返回null
     */
    fun exportLogFile(): String? {
        return try {
            val currentFile = getCurrentLogFile()
            if (!currentFile.exists()) return null

            // 下载目录（Android 10+需适配Scoped Storage，这里简化处理）
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val exportFileName = "app_log_${DATE_FORMAT_FILE.format(Date())}.txt"
            val exportFile = File(downloadDir, exportFileName)

            // 复制文件
            FileInputStream(currentFile).use { input ->
                FileOutputStream(exportFile).use { output ->
                    input.copyTo(output)
                }
            }
            exportFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "导出日志失败", e)
            null
        }
    }

    /**
     * 获取所有日志文件列表
     */
    fun getLogFiles(): Array<File>? {
        return logDir.listFiles { file -> file.name.endsWith(".txt") }
    }

    /**
     * 清空所有日志文件
     */
    fun clearLogs() {
        getLogFiles()?.forEach { it.delete() }
    }
}