package com.hqumath.demo.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.hqumath.demo.R
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityMainBinding
import com.hqumath.demo.utils.CommonUtil
import com.king.app.dialog.AppDialog
import com.king.app.updater.AppUpdater
import com.king.app.updater.UpdateConfig
import com.king.app.updater.callback.UpdateCallback
import com.king.app.updater.http.OkHttpManager
import java.io.File


/**
 * ****************************************************************
 * 作    者: Created by gyd
 * 创建时间: 2023/10/25 9:35
 * 文件描述: 主界面
 * 注意事项:
 * ****************************************************************
 */
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mAppUpdater: AppUpdater? = null
    private var tvProgress: TextView? = null
    private var progressBar: ProgressBar? = null

    override fun initContentView(savedInstanceState: Bundle?): View {
        //enableEdgeToEdge() 启用沉浸式布局
        binding = ActivityMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initListener() {
        binding.btnMyRepos.setOnClickListener {
            //startActivity(Intent(mContext, MyReposActivity::class.java))
            //无需权限，后台下载，自动安装
            val mUrl = "http://cps.yingyonghui.com/cps/yyh/channel/ac.union.m2/com.yingyonghui.market_1_30063293.apk"
            //AppUpdater(mContext, url).start()
            //? 没有通知栏进度，需要通知权限？ TODO

            //简单弹框升级
//            val config = AppDialogConfig(mContext)
//            config.setTitle("简单弹框升级")
//                .setConfirm("升级") //旧版本使用setOk
//                .setContent("1、新增某某功能、\n2、修改某某问题、\n3、优化某某BUG、")
//                .setOnClickConfirm {
//                    AppUpdater.Builder(mContext)
//                        .setUrl(url)
//                        .build()
//                        .start()
//                    AppDialog.INSTANCE.dismissDialog()
//                }
//            AppDialog.INSTANCE.showDialog(mContext, config)

            //一键下载并监听

            val config: UpdateConfig = UpdateConfig()
            config.setUrl(mUrl)
            config.addHeader("token", "xxxxxx")
            mAppUpdater = AppUpdater(mContext, config)
                .setHttpManager(OkHttpManager.getInstance())
                .setUpdateCallback(object : UpdateCallback {
                    override fun onDownloading(isDownloading: Boolean) {
                        if (isDownloading) {
                            CommonUtil.toast("已经在下载中,请勿重复下载。")
                        } else {
//                            showToast("开始下载…");
                            val view: View = LayoutInflater.from(mContext)
                                .inflate(R.layout.dialog_progress, null)
                            tvProgress = view.findViewById<TextView>(R.id.tvProgress)
                            progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
                            AppDialog.INSTANCE.showDialog(mContext, view, false)
                        }
                    }

                    override fun onStart(url: String) {
                    }

                    override fun onProgress(progress: Long, total: Long, isChanged: Boolean) {
                        if (isChanged) {
                            updateProgress(progress, total)
                        }
                    }

                    override fun onFinish(file: File) {
                        AppDialog.INSTANCE.dismissDialog()
                        CommonUtil.toast("下载完成")
                    }

                    override fun onError(e: Exception) {
                        AppDialog.INSTANCE.dismissDialog()
                        CommonUtil.toast("下载失败")
                    }

                    override fun onCancel() {
                        AppDialog.INSTANCE.dismissDialog()
                        CommonUtil.toast("取消下载")
                    }
                })
            mAppUpdater?.start()
        }
    }

    private fun updateProgress(progress: Long, total: Long) {
        if (tvProgress == null || progressBar == null) {
            return
        }
        if (progress > 0) {
            val currProgress = (progress * 1.0f / total * 100.0f).toInt()
            tvProgress!!.text = "正在下载…" + currProgress + "%"
            progressBar!!.progress = currProgress
        } else {
            tvProgress!!.text = "正在获取下载数据…"
        }
    }

    override fun initData() {
    }

    override fun initViewObservable() {
    }
}
