package com.hqumath.demo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.hqumath.demo.R
import com.hqumath.demo.databinding.DialogLoadingBinding

class LoadingDialog(
    private val context: Context,
    private var message: String? = null
) : Dialog(context, R.style.dialog_common) {

    private lateinit var binding: DialogLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogLoadingBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root) //根布局会被改为自适应宽高,居中
        //根布局为自适应宽高，有软键盘时必须全屏，否则mate40等手机软键盘无法上推
        /*Window window = getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }*/
        message?.let {
            binding.tvMsg.text = it
        }
    }

    fun setMessage(msg: String) {
        message = msg
        binding.tvMsg.text = message
    }
}