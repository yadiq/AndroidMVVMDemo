package com.hqumath.demo.app

import com.hqumath.demo.service.MonitorService
import com.hqumath.demo.utils.DataStoreUtil

/**
 * ****************************************************************
 * 作    者: Created by gyd
 * 创建时间: 2025/7/27 22:27
 * 文件描述: APP常量
 * 注意事项:
 * ****************************************************************
 */
class Constant {
    companion object {
        const val BASE_API: String = "https://api.github.com/" //API服务器

        var monitorService: MonitorService? = null //拍照服务
        var isCameraTest = false //是否在测试相机
    }
}