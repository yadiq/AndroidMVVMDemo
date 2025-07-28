package com.hqumath.demo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import com.hqumath.demo.R;
import com.hqumath.demo.databinding.DialogDownloadBinding;

public class DownloadDialog extends Dialog {
    private Context mContext;
    private DialogDownloadBinding binding;

    public DownloadDialog(Context context) {
        super(context, R.style.dialog_common);
        this.mContext = context;
        binding = DialogDownloadBinding.inflate(LayoutInflater.from(mContext));
        setContentView(binding.getRoot());//根布局会被改为自适应宽高,居中
    }

    public void setProgress(long progress, long total) {
        if (progress > 0 && total > 0) {
            int currProgress = (int) (progress * 1.0f / total * 100.0f);
            binding.tvProgress.setText("正在下载…" + currProgress + "%");
            binding.progressBar.setProgress(currProgress);
        } else {
            binding.tvProgress.setText("正在获取下载数据…");
            binding.progressBar.setProgress(0);
        }
    }
}
