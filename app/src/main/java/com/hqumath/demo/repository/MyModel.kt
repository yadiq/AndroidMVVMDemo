package com.hqumath.demo.repository

import com.hqumath.demo.app.DataStoreKey
import com.hqumath.demo.base.BaseModel
import com.hqumath.demo.net.RetrofitClient
import com.hqumath.demo.utils.DataStoreUtil

/**
 * ****************************************************************
 * 文件名称: MyModel
 * 作    者: Created by gyd
 * 创建时间: 2019/1/21 15:12
 * 文件描述: 数据层，数据的获取、存储和处理 https://juejin.cn/post/6844903812658888712
 * 注意事项:
 * 1.提供的数据业务层直接可用
 * 2.相同的接口不要多次实现
 * 3.方便自动化测试
 * ****************************************************************
 */
class MyModel : BaseModel() {
    fun login(
        userName: String,
        passWord: String,
        onSuccess: (response: Any) -> Unit,
        onError: (errorMsg: String, code: String) -> Unit
    ) {
        sendRequest(
            RetrofitClient.getInstance().apiService.getUserInfo(userName),
            { response ->
                //数据校验、处理
                DataStoreUtil.putData(DataStoreKey.USER_NAME, userName)
                onSuccess(response)
            },
            onError
        )
    }

    fun getMyRepos(
        userName: String,
        pageSize: Int,
        pageIndex: Long,
        onSuccess: (response: Any) -> Unit,
        onError: (errorMsg: String, code: String) -> Unit
    ) {
        sendRequest(
            RetrofitClient.getInstance().apiService.getMyRepos(
                userName,
                pageSize,
                pageIndex
            ),
            onSuccess,
            onError
        )
    }
}
