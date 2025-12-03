package com.hqumath.demo.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hqumath.demo.R
import com.hqumath.demo.adapter.MyRecyclerAdapters
import com.hqumath.demo.databinding.DialogBottomSelectBinding
import kotlin.let

/**
 * 底部选择弹窗 BottomSelectAdapter
 */
class BottomSelectDialog(
    private val context: Context,
    private val title: String? = null,
    private val names: MutableList<String>,
    private val positiveAction: ((position: Int) -> Unit), //选择
    private val negativeAction: (() -> Unit)? = null, //取消
) : Dialog(context, R.style.dialog_common) {

    private val binding = DialogBottomSelectBinding.inflate(LayoutInflater.from(context))
    private var recyclerAdapter: MyRecyclerAdapters.BottomSelectAdapter? = null

    init {
        setContentView(binding.root) //根布局会被改为自适应宽高,居中
        //根布局为自适应宽高，有软键盘时必须全屏，否则mate40等手机软键盘无法上推
        val window = getWindow()
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        setOnDismissListener {
            negativeAction?.invoke()
        }
        binding.tvClose.setOnClickListener {
            dismiss()
        }
        binding.llTitle.setOnClickListener { }
        binding.llParent.setOnClickListener {
            dismiss()
        }
        title?.let {
            binding.tvTitle.text = it
            binding.llTitle.visibility = View.VISIBLE
        }

        initData()
    }

    private fun initData() {
        recyclerAdapter = MyRecyclerAdapters.BottomSelectAdapter(context, names)
        recyclerAdapter?.setOnItemClickListener { _: View?, position: Int ->
            if (names.size <= position)
                return@setOnItemClickListener
            positiveAction.invoke(position)
            dismiss()
        }
        binding.recyclerView.adapter = recyclerAdapter
    }

    fun setNames(newNames: List<String>) {
        names.clear()
        names.addAll(newNames)
        recyclerAdapter?.notifyDataSetChanged()
    }
}