package com.hqumath.demo.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.View
import androidx.core.app.ActivityCompat
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityCamera2Binding
import com.hqumath.demo.dialog.BottomSelectDialog

class Camera2Activity : BaseActivity() {
    private lateinit var binding: ActivityCamera2Binding
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null

    private var selectAreaDialog: BottomSelectDialog? = null


    override fun initContentView(savedInstanceState: Bundle?): View {
        binding = ActivityCamera2Binding.inflate(layoutInflater)
        return binding.root
    }

    override fun initListener() {
        binding.btnTakePicture.setOnClickListener {
        }
        binding.btnMode.setOnClickListener {
            if (cameraDevice == null)
                return@setOnClickListener
            //获取支持的白平衡模式数组
            val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val characteristics = manager.getCameraCharacteristics(cameraDevice!!.id)
            val awbModes: IntArray? =
                characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES)
            if (awbModes == null)
                return@setOnClickListener
            //获取名称
            val names = awbModes.map {
                when (it) {
                    CaptureRequest.CONTROL_AWB_MODE_OFF -> "OFF"
                    CaptureRequest.CONTROL_AWB_MODE_AUTO -> "自动"
                    CaptureRequest.CONTROL_AWB_MODE_DAYLIGHT -> "日光"
                    CaptureRequest.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT -> "阴天日光"
                    CaptureRequest.CONTROL_AWB_MODE_FLUORESCENT -> "荧光灯"
                    CaptureRequest.CONTROL_AWB_MODE_INCANDESCENT -> "白炽灯"
                    CaptureRequest.CONTROL_AWB_MODE_SHADE -> "阴影"
                    CaptureRequest.CONTROL_AWB_MODE_TWILIGHT -> "黄昏"
                    CaptureRequest.CONTROL_AWB_MODE_WARM_FLUORESCENT -> "暖荧光灯"
                    else -> "UNKNOWN($it)"
                }
            }

            if (selectAreaDialog == null) {
                selectAreaDialog = BottomSelectDialog(
                    context = mContext,
                    title = "选择模式",
                    names = mutableListOf<String>(),
                    positiveAction = { position ->
                        if (cameraCaptureSession == null || previewRequestBuilder == null)
                            return@BottomSelectDialog
                        val mode = awbModes[position]
                        previewRequestBuilder!!.set(CaptureRequest.CONTROL_AWB_MODE, mode) //白平衡模式
                        cameraCaptureSession!!.setRepeatingRequest(previewRequestBuilder!!.build(), null, null)
                    },
                    negativeAction = {}
                )
            }
            selectAreaDialog?.setNames(names)
            selectAreaDialog?.show()
        }
    }

    override fun initData() {
        binding.textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int,
            ) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int,
            ) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = false

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

        }

    }

    override fun initViewObservable() {
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraCaptureSession?.close()
        cameraDevice?.close()
    }

    //打开摄像头
    private fun openCamera() {
        //获取摄像头列表和属性
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = manager.cameraIdList.first { //返回第一个 条件为 true 的元素
            manager.getCameraCharacteristics(it)
                .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }
        //权限
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
            return
        }
        //打开摄像头
        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) {
                cameraDevice = device
                startPreview()
            }

            override fun onDisconnected(device: CameraDevice) {
                device.close()
            }

            override fun onError(device: CameraDevice, error: Int) {
                device.close()
            }
        }, null)
    }

    private fun startPreview() {
        if (cameraDevice == null)
            return
        val texture = binding.textureView.surfaceTexture!!
        texture.setDefaultBufferSize(1280, 720)
        val surface = Surface(texture)
        //定义相机请求（预览、拍照）
        previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        previewRequestBuilder?.addTarget(surface)
        previewRequestBuilder?.set(
            CaptureRequest.CONTROL_AF_MODE, //自动对焦模式
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE //连续对焦，用于拍照。拍照预览时持续追踪焦点
        )
        previewRequestBuilder?.set(
            CaptureRequest.CONTROL_AE_MODE, //曝光策略
            CaptureRequest.CONTROL_AE_MODE_ON //开启自动曝光（默认模式），自动调节亮度
            //CONTROL_AE_MODE_OFF 关闭自动曝光，需要手动设置曝光时间、增益和 ISO
        )
        previewRequestBuilder?.set(
            CaptureRequest.CONTROL_AWB_MODE, //白平衡模式
//            CaptureRequest.CONTROL_AWB_MODE_OFF    //0	关闭自动白平衡，手动设置增益
            CaptureRequest.CONTROL_AWB_MODE_AUTO    //1	自动白平衡（默认），自动检测色温，通常 4000–6500K
//            CaptureRequest . CONTROL_AWB_MODE_TWILIGHT    //7	黄昏（2000–2500K）
//            CaptureRequest.CONTROL_AWB_MODE_INCANDESCENT	//2	白炽灯,钨丝灯（2800–3200K）
//            CaptureRequest.CONTROL_AWB_MODE_WARM_FLUORESCENT	//4	暖荧光灯（3000~3500K）
//            CaptureRequest.CONTROL_AWB_MODE_FLUORESCENT	//3	荧光灯（偏绿，约 4000K）
//            CaptureRequest.CONTROL_AWB_MODE_DAYLIGHT	//5	日光,白天直射阳光（5000~5500K）
//            CaptureRequest.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT	//6	阴天日光,多云阴天（6000~6500K）
//            CaptureRequest.CONTROL_AWB_MODE_SHADE	//8	阴影（偏蓝，色温高，约 7000~7500K）
        )
        //设置色温
//        previewRequestBuilder.set(
//            CaptureRequest.COLOR_CORRECTION_MODE, //白平衡模式处理方式
//            CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX //手动模式：由你提供 RGGB 增益
//        )
//        previewRequestBuilder.set(
//            CaptureRequest.COLOR_CORRECTION_GAINS, //白平衡增益 RGGB 增益矩阵（你的色温/白平衡参数）（R/G/G/B 四通道）
//            CameraUtil.KelvinToRggb(3200) // 5000K → RGGB //3200 - 6400 都试一下
//        )


        //Session会话管理一组相机请求
        cameraDevice!!.createCaptureSession(
            listOf(surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    cameraCaptureSession = session
                    session.setRepeatingRequest(previewRequestBuilder!!.build(), null, null)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            },
            null
        )
    }
}
