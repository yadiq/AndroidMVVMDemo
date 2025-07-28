package com.hqumath.demo.ui.main

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityMainBinding
import com.hqumath.demo.dialog.DownloadDialog
import com.hqumath.demo.utils.CommonUtil
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

    private var downloadDialog: DownloadDialog? = null

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


            val updateConfig = UpdateConfig()
            with(updateConfig){
                filename = "demo.apk"
                url = mUrl
                isShowNotification = false
            }
            mAppUpdater = AppUpdater(mContext, updateConfig)
            mAppUpdater?.setHttpManager(OkHttpManager.getInstance())
            mAppUpdater?.setUpdateCallback(object : UpdateCallback {
                    override fun onDownloading(isDownloading: Boolean) {
                        if (isDownloading) {
                            CommonUtil.toast("已经在下载中,请勿重复下载。")
                        } else {
                            if (downloadDialog == null) {
                                downloadDialog = DownloadDialog(mContext)
                                downloadDialog?.setCancelable(false)
                            }
                            if (downloadDialog?.isShowing == false) {
                                downloadDialog?.setProgress(0, 0)
                                downloadDialog?.show()
                            }
                        }
                    }

                    override fun onStart(url: String) {
                    }

                    override fun onProgress(progress: Long, total: Long, isChanged: Boolean) {
                        if (isChanged) {
                            downloadDialog?.setProgress(progress, total)
                        }
                    }

                    override fun onFinish(file: File) {
                        downloadDialog?.dismiss()
                        CommonUtil.toast("下载完成")
                    }

                    override fun onError(e: Exception) {
                        downloadDialog?.dismiss()
                        CommonUtil.toast("下载失败")
                    }

                    override fun onCancel() {
                        downloadDialog?.dismiss()
                        CommonUtil.toast("取消下载")
                    }
                })
            mAppUpdater?.start()
        }
    }

    override fun initData() {
    }

    override fun initViewObservable() {
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadDialog?.dismiss()
        downloadDialog = null
    }
}
