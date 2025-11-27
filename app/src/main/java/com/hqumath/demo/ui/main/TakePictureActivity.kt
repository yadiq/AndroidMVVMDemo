package com.hqumath.demo.ui.main

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Size
import android.view.View
import androidx.camera.core.ImageCapture
import androidx.lifecycle.lifecycleScope
import com.hqumath.demo.app.Constant
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityTakePictureBinding
import com.hqumath.demo.utils.CommonUtil
import com.hqumath.demo.utils.LogUtil
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

            //分辨率
            val sizes = getSupportedPictureSizes(mContext, CameraCharacteristics.LENS_FACING_BACK)
            sizes.forEach { LogUtil.d("MonitorService", "JPEG: ${it.width}x${it.height}") }
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

    fun getSupportedPictureSizes(context: Context, lensFacing: Int): Array<Size> {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        cameraManager.cameraIdList.forEach { cameraId ->
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)

            if (facing == lensFacing) {
                val map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                )

                // 拍照使用 JPEG
                return map?.getOutputSizes(ImageFormat.JPEG) ?: emptyArray()
            }
        }
        return emptyArray()
    }

}
