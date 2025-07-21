package com.hqumath.demo.ui.repos

import com.hqumath.demo.base.BasePresenter
import com.hqumath.demo.bean.ReposEntity
import com.hqumath.demo.net.HttpListener
import com.hqumath.demo.repository.MyModel

/**
 * ****************************************************************
 * 文件名称: LoginPresenter
 * 作    者: Created by gyd
 * 创建时间: 2019/1/21 15:12
 * 文件描述: 业务逻辑层
 * 注意事项:
 * 版权声明:
 * ****************************************************************
 */
class MyReposPresenter : BasePresenter<MyReposPresenter.Contract?>() {
    interface Contract {
        fun onGetListSuccess(isRefresh: Boolean, isNewDataEmpty: Boolean)

        fun onGetListError(errorMsg: String, code: String, isRefresh: Boolean)
    }

    private var pageIndex: Long = 0 //索引
    @JvmField
    var mData: MutableList<ReposEntity> = ArrayList() //列表数据


    init {
        mModel = MyModel()
    }

    /**
     * 获取列表
     *
     * @param isRefresh true 下拉刷新；false 上拉加载
     */
    fun getMyRepos(isRefresh: Boolean) {
        if (mView == null) return
        if (isRefresh) {
            pageIndex = 1
        }
        val userName = "yadiq"
        (mModel as MyModel).getMyRepos(userName, pageSize, pageIndex, object : HttpListener {
            override fun onSuccess(`object`: Any) {
                if (mView == null) return
                val list = `object` as List<ReposEntity>
                pageIndex++ //偏移量+1
                if (isRefresh)  //下拉覆盖，上拉增量
                    mData.clear()
                if (!list.isEmpty()) mData.addAll(list)
                mView!!.onGetListSuccess(isRefresh, list.isEmpty())
            }

            override fun onError(errorMsg: String, code: String) {
                if (mView == null) return
                mView!!.onGetListError(errorMsg, code, isRefresh)
            }
        })
    }

    companion object {
        private const val pageSize = 10 //分页
    }
}
