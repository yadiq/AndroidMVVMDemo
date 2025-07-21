package com.hqumath.demo.ui.repos

import android.os.Bundle
import android.view.View
import com.hqumath.demo.R
import com.hqumath.demo.adapter.MyRecyclerAdapters.ReposRecyclerAdapter
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityMyReposBinding
import com.hqumath.demo.dialog.DialogUtil
import com.hqumath.demo.utils.CommonUtil
import com.scwang.smart.refresh.layout.api.RefreshLayout

/**
 * ****************************************************************
 * 作    者: Created by gyd
 * 创建时间: 2023/10/25 10:09
 * 文件描述:
 * 注意事项:
 * ****************************************************************
 */
class MyReposActivity : BaseActivity(), MyReposPresenter.Contract {
    private var binding: ActivityMyReposBinding? = null
    private var mPresenter: MyReposPresenter? = null
    private var recyclerAdapter: ReposRecyclerAdapter? = null
    protected var hasRequested: Boolean = false //在onResume中判断是否已经请求过数据。用于懒加载

    override fun initContentView(savedInstanceState: Bundle?): View {
        binding = ActivityMyReposBinding.inflate(layoutInflater)
        return binding!!.root
    }

    override fun initListener() {
        binding!!.titleLayout.tvTitle.setText(R.string.my_repos)
        binding!!.titleLayout.ivBack.setOnClickListener { v: View? -> finish() }
        binding!!.refreshLayout.setOnRefreshListener { v: RefreshLayout? ->
            mPresenter!!.getMyRepos(
                true
            )
        }
        binding!!.refreshLayout.setOnLoadMoreListener { v: RefreshLayout? ->
            mPresenter!!.getMyRepos(
                false
            )
        }
    }

    override fun initData() {
        mPresenter = MyReposPresenter()
        mPresenter!!.attachView(this)

        recyclerAdapter = ReposRecyclerAdapter(mContext, mPresenter!!.mData)
        recyclerAdapter!!.setOnItemClickListener { v: View?, position: Int ->
            val data = mPresenter!!.mData[position]
            val dialog = DialogUtil(mContext)
            dialog.setTitle("提示")
            dialog.setMessage(data.name)
            dialog.setOneConfirmBtn("确定", null)
            dialog.show()
        }
        binding!!.recyclerView.adapter = recyclerAdapter
    }

    public override fun onResume() {
        super.onResume()
        if (!hasRequested) {
            hasRequested = true
            binding!!.refreshLayout.autoRefresh() //触发自动刷新
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mPresenter != null) {
            mPresenter!!.detachView()
            mPresenter = null
        }
    }

    override fun onGetListSuccess(isRefresh: Boolean, isNewDataEmpty: Boolean) {
        recyclerAdapter!!.notifyDataSetChanged()
        if (isRefresh) {
            if (isNewDataEmpty) {
                binding!!.refreshLayout.finishRefreshWithNoMoreData() //上拉加载功能将显示没有更多数据
            } else {
                binding!!.refreshLayout.finishRefresh()
            }
        } else {
            if (isNewDataEmpty) {
                binding!!.refreshLayout.finishLoadMoreWithNoMoreData() //上拉加载功能将显示没有更多数据
            } else {
                binding!!.refreshLayout.finishLoadMore()
            }
        }
        binding!!.emptyLayout.llEmpty.visibility =
            if (mPresenter!!.mData.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onGetListError(errorMsg: String, code: String, isRefresh: Boolean) {
        CommonUtil.toast(errorMsg)
        if (isRefresh) {
            binding!!.refreshLayout.finishRefresh(false) //刷新失败，会影响到上次的更新时间
        } else {
            binding!!.refreshLayout.finishLoadMore(false)
        }
        binding!!.emptyLayout.llEmpty.visibility =
            if (mPresenter!!.mData.isEmpty()) View.VISIBLE else View.GONE
    }
}
