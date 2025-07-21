package com.hqumath.demo.ui.repos

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hqumath.demo.bean.ReposEntity
import com.hqumath.demo.net.HttpListener
import com.hqumath.demo.repository.MyModel

class MyReposViewModel : ViewModel() {
    private var mModel: MyModel? = null

    private var myReposPageIndex: Long = 0 //索引
    var myReposResultCode: MutableLiveData<String> = MutableLiveData() //网络请求错误号 0成功；other失败
    var myReposResultMsg: String? = null //错误原因
    var myReposRefresh: Boolean = false //true 下拉刷新；false 上拉加载
    var myReposNewEmpty: Boolean = false //true 增量为空；false 增量不为空
    var myReposData: MutableList<ReposEntity> = ArrayList() //列表数据

    companion object {
        private const val pageSize = 10 //分页
    }

    init {
        mModel = MyModel()
    }

    override fun onCleared() {
        super.onCleared()
        mModel?.dispose()
        mModel = null
    }

    /**
     * 获取列表
     *
     * @param isRefresh true 下拉刷新；false 上拉加载
     */
    fun getMyRepos(isRefresh: Boolean) {
        if (isRefresh) {
            myReposPageIndex = 1
        }
        val userName = "yadiq"
        mModel?.getMyRepos(userName, pageSize, myReposPageIndex, object : HttpListener {
            override fun onSuccess(`object`: Any) {
                val list = `object` as List<ReposEntity>
                myReposPageIndex++ //偏移量+1
                if (isRefresh)  //下拉覆盖，上拉增量
                    myReposData.clear()
                if (list.isNotEmpty())
                    myReposData.addAll(list)

                myReposRefresh = isRefresh //是否下拉
                myReposNewEmpty = list.isEmpty() //增量是否为空
                myReposResultCode.postValue("0") //错误码 0成功
            }

            override fun onError(errorMsg: String, code: String) {
                myReposRefresh = isRefresh //是否下拉
                myReposResultMsg = errorMsg //错误原因
                myReposResultCode.postValue(code) //错误码 0成功
            }
        })
    }
}