package com.hqumath.demo.ui.main

import android.os.Bundle
import android.view.View
import androidx.camera.core.ImageCapture
import androidx.lifecycle.lifecycleScope
import com.hqumath.demo.app.Constant
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityTakePictureBinding
import com.hqumath.demo.utils.CommonUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Camera2 API
// CameraX
class TakePictureActivity : BaseActivity() {
    private lateinit var binding: ActivityTakePictureBinding

    override fun initContentView(savedInstanceState: Bundle?): View {
        binding = ActivityTakePictureBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initListener() {
        binding.btnTakePicture.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val path = Constant.monitorService?.takePicture(0, false)
                CommonUtil.toast("拍照成功 $path")
            }
        }
    }

    override fun initData() {
    }

    override fun initViewObservable() {
    }

    override fun onStart() {
        super.onStart()
        Constant.isCameraTest = true
        Constant.monitorService?.openCameraPreview(binding.previewView.surfaceProvider)
    }

    override fun onStop() {
        super.onStop()
        Constant.monitorService?.closeCamera()
        Constant.isCameraTest = false
    }
}
