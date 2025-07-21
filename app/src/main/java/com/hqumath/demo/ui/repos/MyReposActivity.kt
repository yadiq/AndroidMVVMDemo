package com.hqumath.demo.ui.repos

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.hqumath.demo.R
import com.hqumath.demo.adapter.MyRecyclerAdapters
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityMyReposBinding
import com.hqumath.demo.dialog.DialogUtil
import com.hqumath.demo.utils.CommonUtil

/**
 * ****************************************************************
 * 作    者: Created by gyd
 * 创建时间: 2023/10/25 10:09
 * 文件描述:
 * 注意事项:
 * ****************************************************************
 */
class MyReposActivity : BaseActivity() {
    private lateinit var binding: ActivityMyReposBinding
    private lateinit var viewModel: MyReposViewModel

    private var recyclerAdapter: MyRecyclerAdapters.ReposRecyclerAdapter? = null
    private var hasRequested: Boolean = false //在onResume中判断是否已经请求过数据。用于懒加载

    override fun initContentView(savedInstanceState: Bundle?): View {
        binding = ActivityMyReposBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initListener() {
        binding.titleLayout.tvTitle.setText(R.string.my_repos)
        binding.titleLayout.ivBack.setOnClickListener { finish() }
        binding.refreshLayout.setOnRefreshListener { viewModel.getMyRepos(true) }
        binding.refreshLayout.setOnLoadMoreListener { viewModel.getMyRepos(false) }
    }

    override fun initData() {
        viewModel = ViewModelProvider(mContext)[MyReposViewModel::class.java]

        recyclerAdapter = MyRecyclerAdapters.ReposRecyclerAdapter(mContext, viewModel.myReposData)
        recyclerAdapter?.setOnItemClickListener { _: View?, position: Int ->
            val data = viewModel.myReposData[position]
            val dialog = DialogUtil(mContext)
            dialog.setTitle("提示")
            dialog.setMessage(data.name)
            dialog.setOneConfirmBtn("确定", null)
            dialog.show()
        }
        binding.recyclerView.adapter = recyclerAdapter
    }

    override fun initViewObservable() {
        //网络请求错误号
        viewModel.myReposResultCode.observe(this) { code: String ->
            if (code == "0") {
                recyclerAdapter?.notifyDataSetChanged()
                if (viewModel.myReposRefresh) {
                    if (viewModel.myReposNewEmpty) {
                        binding.refreshLayout.finishRefreshWithNoMoreData() //上拉加载功能将显示没有更多数据
                    } else {
                        binding.refreshLayout.finishRefresh()
                    }
                } else {
                    if (viewModel.myReposNewEmpty) {
                        binding.refreshLayout.finishLoadMoreWithNoMoreData() //上拉加载功能将显示没有更多数据
                    } else {
                        binding.refreshLayout.finishLoadMore()
                    }
                }
            } else {
                CommonUtil.toast(viewModel.myReposResultMsg)
                if (viewModel.myReposRefresh) {
                    binding.refreshLayout.finishRefresh(false) //刷新失败，会影响到上次的更新时间
                } else {
                    binding.refreshLayout.finishLoadMore(false)
                }
            }
            binding.emptyLayout.llEmpty.visibility =
                if (viewModel.myReposData.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    public override fun onResume() {
        super.onResume()
        if (!hasRequested) {
            hasRequested = true
            binding.refreshLayout.autoRefresh() //触发自动刷新
        }
    }
}
