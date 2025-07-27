package com.hqumath.demo.app

import com.hqumath.demo.utils.SPUtil

/**
 * ****************************************************************
 * 作    者: Created by gyd
 * 创建时间: 2025/7/27 22:27
 * 文件描述:
 * 注意事项:
 * ****************************************************************
 */
class Config {
    companion object {
        const val BASE_URL: String = "https://api.github.com/" //API服务器

        //请求通用参数
        fun getBaseMap(): HashMap<String, String> {
            val token = SPUtil.getInstance().getString(SPKey.TOKEN)
            val map = HashMap<String, String>()
            map["token"] = token
            return map
        }
    }
}