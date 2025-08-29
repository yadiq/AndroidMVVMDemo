package com.hqumath.demo.ui.repos

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.hqumath.demo.R
import com.hqumath.demo.adapter.MyRecyclerAdapters
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityMyReposBinding
import com.hqumath.demo.dialog.CommonDialog
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
    private var needRequest: Boolean = true //在onResume中判断，是否需要请求数据

//    companion object {
//        fun getStartIntent(mContext: Context, id: String): Intent {
//            val intent = Intent(mContext, PersonSelectActivity::class.java)
//            intent.putExtra("ID", id)
//            return intent
//        }
//    }

    override fun initContentView(savedInstanceState: Bundle?): View {
        binding = ActivityMyReposBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initListener() {
        binding.titleLayout.tvTitle.setText("我的仓库")
        binding.titleLayout.ivBack.setOnClickListener { finish() }
        binding.refreshLayout.setOnRefreshListener { viewModel.getMyRepos(true) }
        binding.refreshLayout.setOnLoadMoreListener { viewModel.getMyRepos(false) }
    }

    override fun initData() {
        viewModel = ViewModelProvider(this)[MyReposViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        recyclerAdapter = MyRecyclerAdapters.ReposRecyclerAdapter(mContext, viewModel.list)
        recyclerAdapter?.setOnItemClickListener { _: View?, position: Int ->
            if (viewModel.list.size <= position)
                return@setOnItemClickListener
            val data = viewModel.list[position]
            val dialog = CommonDialog(
                context = mContext,
                title = "提示",
                message = data.name,
                oneButtonText = "确定"
            )
            dialog.show()
        }
        binding.recyclerView.adapter = recyclerAdapter
    }

    override fun initViewObservable() {
        viewModel.isLoading.observe(this) { b ->
            if (b) {
                showLoadingDialog()
            } else {
                dismissLoadingDialog()
            }
        }
        //网络请求错误号
        viewModel.listResultCode.observe(this) { code: String ->
            if (code == "0") {
                recyclerAdapter?.notifyDataSetChanged()
                if (viewModel.listRefresh) {
                    if (viewModel.listNewEmpty) {
                        binding.refreshLayout.finishRefreshWithNoMoreData() //上拉加载功能将显示没有更多数据
                    } else {
                        binding.refreshLayout.finishRefresh()
                    }
                } else {
                    if (viewModel.listNewEmpty) {
                        binding.refreshLayout.finishLoadMoreWithNoMoreData() //上拉加载功能将显示没有更多数据
                    } else {
                        binding.refreshLayout.finishLoadMore()
                    }
                }
            } else {
                if (viewModel.listRefresh) {
                    binding.refreshLayout.finishRefresh(false) //刷新失败，会影响到上次的更新时间
                } else {
                    binding.refreshLayout.finishLoadMore(false)
                }
            }
            binding.emptyLayout.llEmpty.visibility =
                if (viewModel.list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        if (needRequest) {
            needRequest = false
            viewModel.isLoading.postValue(true)
            viewModel.getMyRepos(true)
        }
    }
}
