package com.hqumath.demo.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityLoginBinding
import com.hqumath.demo.ui.main.MainActivity
import com.hqumath.demo.utils.CommonUtil

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun initContentView(savedInstanceState: Bundle?): View {
        //enableEdgeToEdge() 启用沉浸式布局
        binding = ActivityLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initListener() {
        binding.btnLogin.setOnClickListener {
            viewModel.login()
        }
    }

    override fun initData() {
        viewModel = ViewModelProvider(mContext)[LoginViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

    }

    override fun initViewObservable() {
        viewModel.isLoading.observe(this) { b ->
            if (b) {
                showLoadingDialog()
            } else {
                dismissLoadingDialog()
            }
        }
        viewModel.loginResultCode.observe(this) { code ->
            when (code) {
                "0" -> {
                    CommonUtil.toast(viewModel.userName.value + "已登录")
                    startActivity(Intent(mContext, MainActivity::class.java))
                    finish()
                }

                else -> {
                    CommonUtil.toast("登录失败\n" + code + "," + viewModel.loginResultMsg)
                }
            }
        }
    }
}