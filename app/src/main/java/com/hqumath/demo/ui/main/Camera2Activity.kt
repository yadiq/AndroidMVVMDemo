package com.hqumath.demo.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.os.Bundle
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import androidx.core.app.ActivityCompat
import com.hqumath.demo.base.BaseActivity
import com.hqumath.demo.databinding.ActivityCamera2Binding
import com.hqumath.demo.dialog.BottomSelectDialog
import com.hqumath.demo.utils.CommonUtil
import com.hqumath.demo.utils.FileUtil
import com.hqumath.demo.utils.LogUtil


class Camera2Activity : BaseActivity() {
    private lateinit var binding: ActivityCamera2Binding
    private var cameraDevice: CameraDevice? = null
    private var characteristics: CameraCharacteristics? = null //相机特性
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null

    private var imageReader: ImageReader? = null

    private var selectAreaDialog: BottomSelectDialog? = null


    override fun initContentView(savedInstanceState: Bundle?): View {
        binding = ActivityCamera2Binding.inflate(layoutInflater)
        return binding.root
    }

    override fun initListener() {
        binding.btnTakePicture.setOnClickListener {
            capturePhoto()
        }
        binding.btnMode.setOnClickListener {
            if (cameraDevice == null)
                return@setOnClickListener
            //获取支持的白平衡模式数组
            val awbModes: IntArray =
                characteristics?.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES) ?: IntArray(
                    0
                )
            //获取名称
            val names = awbModes.map {
                getModeDescription(it)
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
                        cameraCaptureSession!!.setRepeatingRequest(
                            previewRequestBuilder!!.build(),
                            null,
                            null
                        )
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
        //相机特性
        characteristics = manager.getCameraCharacteristics(cameraId)
        getCharacteristics()
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
                cameraDevice = null
            }

            override fun onError(device: CameraDevice, error: Int) {
                device.close()
                cameraDevice = null
            }
        }, null)
    }

    private fun startPreview() {
        if (cameraDevice == null)
            return
//        val width = 1920
//        val height = 1080
        val width = 1280
        val height = 720
//        val width = 640
//        val height = 480
        ////////////////////////预览////////////////////////
        val texture = binding.textureView.surfaceTexture!!
        texture.setDefaultBufferSize(width, height)
        val surface = Surface(texture)
        //创建捕获请求
        previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        previewRequestBuilder?.addTarget(surface)
        previewRequestBuilder?.set(
            CaptureRequest.CONTROL_AF_MODE, //自动对焦模式
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE //连续对焦，用于拍照。拍照预览时持续追踪焦点
        )
        /*previewRequestBuilder?.set(
            CaptureRequest.CONTROL_AE_MODE, //曝光策略
            CaptureRequest.CONTROL_AE_MODE_ON //开启自动曝光（默认模式），自动调节亮度
            //CONTROL_AE_MODE_OFF 关闭自动曝光，需要手动设置曝光时间、增益和 ISO
        )*/
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
        /*previewRequestBuilder.set(
            CaptureRequest.COLOR_CORRECTION_MODE, //白平衡模式处理方式
            CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX //手动模式：由你提供 RGGB 增益
        )
        previewRequestBuilder.set(
            CaptureRequest.COLOR_CORRECTION_GAINS, //白平衡增益 RGGB 增益矩阵（你的色温/白平衡参数）（R/G/G/B 四通道）
            CameraUtil.KelvinToRggb(3200) // 5000K → RGGB //3200 - 6400 都试一下
        )*/
        ////////////////////////拍照////////////////////////
        imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 2) //2最大缓冲区数量（队列大小）
        imageReader!!.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            // 保存图片
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            FileUtil.getExternalFile("picture", "${System.currentTimeMillis()}.jpg")
                .writeBytes(bytes)
            //File("/sdcard/DCIM/camera2_photo.jpg").writeBytes(bytes)
            image.close()
            CommonUtil.toast("拍照成功")
        }, null)

        //Session会话管理一组相机请求
        cameraDevice!!.createCaptureSession(
            listOf(surface, imageReader!!.surface),
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

    private fun capturePhoto() {
        if (cameraDevice == null)
            return
        val curMode = previewRequestBuilder!!.get<Int>(CaptureRequest.CONTROL_AWB_MODE)

        //创建捕获请求
        captureRequestBuilder =
            cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequestBuilder?.addTarget(imageReader!!.surface)
        captureRequestBuilder?.set(
            CaptureRequest.CONTROL_AWB_MODE, //白平衡模式
            curMode
        )
        captureRequestBuilder?.set(
            CaptureRequest.CONTROL_AF_MODE, //自动对焦模式
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE //连续对焦，用于拍照。拍照预览时持续追踪焦点
        )
        cameraCaptureSession?.capture(captureRequestBuilder!!.build(), null, null)
    }

    //////////////相机特性
    private fun getCharacteristics() {
        //获取支持的白平衡模式数组
        val awbModes: IntArray =
            characteristics?.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES) ?: IntArray(0)
        for (mode in awbModes) {
            LogUtil.d("白平衡模式: ${getModeDescription(mode)}")
        }

        //获取支持的所有分辨率
        // 预览SurfaceTexture.class 拍照ImageFormat.JPEG 录像MediaRecorder.class
        val streamMap: StreamConfigurationMap? =
            characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val sizes: Array<Size> =
            streamMap?.getOutputSizes(SurfaceTexture::class.java) ?: emptyArray()
        sizes.forEach {
            LogUtil.d("分辨率: ${it.width}x${it.height}")
        }
    }

    // 获取白平衡模式描述
    private fun getModeDescription(awbMode: Int): String {
        when (awbMode) {
            CameraMetadata.CONTROL_AWB_MODE_OFF -> return "0白平衡关闭，使用固定色温"
            CameraMetadata.CONTROL_AWB_MODE_AUTO -> return "1自动白平衡，相机自动调整"
            CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT -> return "2白炽灯模式，适用于室内暖光"
            CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT -> return "3荧光灯模式，适用于普通荧光灯"
            CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT -> return "4暖色荧光灯模式"
            CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT -> return "5日光模式，适用于室外晴天"
            CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT -> return "6阴天模式，适用于室外阴天"
            CameraMetadata.CONTROL_AWB_MODE_TWILIGHT -> return "7黄昏模式，适用于日出日落"
            CameraMetadata.CONTROL_AWB_MODE_SHADE -> return "8阴影模式，适用于阴影区域"
            else -> return "未知的白平衡模式"
        }
    }
}
