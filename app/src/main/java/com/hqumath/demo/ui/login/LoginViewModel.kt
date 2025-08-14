package com.hqumath.demo.ui.login

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.hqumath.demo.bean.UserInfoEntity
import com.hqumath.demo.repository.MyModel
import com.hqumath.demo.utils.CommonUtil

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private var mModel: MyModel? = null

    var isLoading: MutableLiveData<Boolean> = MutableLiveData() //是否显示进度条
    var userName: MutableLiveData<String> = MutableLiveData()
    var password: MutableLiveData<String> = MutableLiveData()
    var loginResultCode: MutableLiveData<String> = MutableLiveData() //登录结果 code 0成功; other失败
    var loginResultMsg: String? = null //登录结果
    var loginResultData: UserInfoEntity? = null //登录结果

//    companion object {
//        private const val pageSize = 10 //分页
//    }

    init {
        mModel = MyModel()
        userName.postValue("1")
        password.postValue("1")
    }

    override fun onCleared() {
        super.onCleared()
        mModel?.dispose()
        mModel = null
    }

    /**
     * 登录
     */
    fun login() {
        if (TextUtils.isEmpty(userName.value) || TextUtils.isEmpty(password.value)) {
            CommonUtil.toast("请输入用户名和密码")
            return
        }
        //登录请求
        isLoading.postValue(true)
        mModel?.login(
            userName.value!!,
            password.value!!,
            { response ->
                isLoading.postValue(false)
                loginResultData = response as UserInfoEntity
                loginResultCode.postValue("0")
            },
            { errorMsg, code ->
                isLoading.postValue(false)
                loginResultMsg = errorMsg
                loginResultCode.postValue(code)
            }
        )
    }

}