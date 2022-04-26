package com.hootor.tmc_2.screens.main.photo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.os.bundleOf
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hootor.tmc_2.R
import com.hootor.tmc_2.databinding.CameraUiContainerBinding
import com.hootor.tmc_2.databinding.FragmentCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.window.layout.WindowMetricsCalculator
import com.hootor.tmc_2.data.media.YUVtoRGB
import com.hootor.tmc_2.utils.findTopNavController
import com.hootor.tmc_2.utils.publishResults
import com.hootor.tmc_2.utils.savePhotoToInternalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

val EXTENSION_WHITELIST = arrayOf("JPG")

class CameraFragment : Fragment() {

    private var _fragmentCameraBinding: FragmentCameraBinding? = null
    private val fragmentCameraBinding get() = _fragmentCameraBinding!!

    private var cameraUiContainerBinding: CameraUiContainerBinding? = null

    private lateinit var outputDirectory: File

    private var displayId: Int = -1
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var metricsCalculator: WindowMetricsCalculator

    private val displayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    /**
     * We need a display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifest or for 180-degree
     * orientation changes.
     */
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@CameraFragment.displayId) {
                Log.d(TAG, "Rotation changed: ${view.display.rotation}")
                imageCapture?.targetRotation = view.display.rotation
                imageAnalyzer?.targetRotation = view.display.rotation
            }
        } ?: Unit
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // Shut down our background executor
        cameraExecutor.shutdown()
        displayManager.unregisterDisplayListener(displayListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)
        return fragmentCameraBinding.root
    }

    private fun setGalleryThumbnail(uri: Uri) {
        // Run the operations in the view's thread
        cameraUiContainerBinding?.photoViewButton?.let { photoViewButton ->
            photoViewButton.post {
                // Remove thumbnail padding
                photoViewButton.setPadding(resources.getDimension(R.dimen.stroke_small).toInt())

                // Load thumbnail into circular button using Glide
                Glide.with(photoViewButton)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(photoViewButton)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        displayManager.registerDisplayListener(displayListener, null)

        metricsCalculator = WindowMetricsCalculator.getOrCreate()

        outputDirectory = getOutputDirectory(requireContext())

        fragmentCameraBinding.viewFinder.post {
            // Следите за отображением, к которому прикреплено это представление
            displayId = fragmentCameraBinding.viewFinder.display.displayId

            // Build UI controls
            updateCameraUi()

            // Set up the camera and its use cases
            setUpCamera()
        }
    }

    /**
     * Раздуйте элементы управления камерой и обновите пользовательский интерфейс вручную при изменении конфигурации, чтобы избежать удаления
     * и повторное добавление видоискателя из иерархии представлений; это обеспечивает плавное вращение
     * переход на устройства, которые его поддерживают.
     *
     * ПРИМЕЧАНИЕ. Флаг поддерживается, начиная с Android 8, но на нем все еще есть небольшая вспышка.
     * экран для устройств под управлением Android 9 или ниже.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Rebind the camera with the updated display metrics
        bindCameraUseCases()

        // Enable or disable switching between cameras
        updateCameraSwitchButton()
    }

    /** Инициализируйте CameraX и подготовьтесь к привязке вариантов использования камеры. */
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {

            // CameraProvider
            cameraProvider = cameraProviderFuture.get()

            // Выберите объектив в зависимости от доступных камер
            lensFacing = when {
                hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }

            // Enable or disable switching between cameras
            updateCameraSwitchButton()

            // Build and bind the camera use cases
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /** Declare and bind preview, capture and analysis use cases */
    private fun bindCameraUseCases() {

        // Get screen metrics used to setup camera for full screen resolution
        val metrics = metricsCalculator.computeCurrentWindowMetrics(requireActivity()).bounds
        Log.d(TAG, "Screen metrics: ${metrics.width()} x ${metrics.height()}")

        val screenAspectRatio = aspectRatio(metrics.width(), metrics.height())
        Log.d(TAG, "Preview aspect ratio: $screenAspectRatio")

        val rotation = fragmentCameraBinding.viewFinder.display.rotation

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation
            .setTargetRotation(rotation)
            .build()

        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // We request aspect ratio but no resolution to match preview config, but letting
            // CameraX optimize for whatever specific resolution best fits our use cases
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()

//        // ImageAnalysis
//        imageAnalyzer = ImageAnalysis.Builder()
//            // We request aspect ratio but no resolution
//            .setTargetAspectRatio(screenAspectRatio)
//            // Set initial target rotation, we will have to call this again if rotation changes
//            // during the lifecycle of this use case
//            .setTargetRotation(rotation)
//            .build()
//            // The analyzer can then be assigned to the instance
//            .also {
//                it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
//                    // Values returned from our analyzer are passed to the attached listener
//                    // We log image analysis results here - you should do something useful
//                    // instead!
//                    Log.d(TAG, "Average luminosity: $luma")
//                })
//            }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
            observeCameraState(camera?.cameraInfo!!)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    /** Метод, используемый для перерисовки элементов управления пользовательского интерфейса камеры, вызываемый при каждом изменении конфигурации. */
    private fun updateCameraUi() {

        // Удалите предыдущий пользовательский интерфейс, если он есть
        cameraUiContainerBinding?.root?.let {
            fragmentCameraBinding.root.removeView(it)
        }

        cameraUiContainerBinding = CameraUiContainerBinding.inflate(
            LayoutInflater.from(requireContext()),
            fragmentCameraBinding.root,
            true
        )

        // В фоновом режиме загрузите последнюю сделанную фотографию (если есть) для миниатюры галереи.
        lifecycleScope.launch(Dispatchers.IO) {
            outputDirectory.listFiles { file: File ->
                EXTENSION_WHITELIST.contains(file.extension.uppercase(Locale.ROOT))
            }?.maxOrNull()?.let {
                setGalleryThumbnail(Uri.fromFile(it))
            }
        }

        // Прослушиватель кнопки, используемой для захвата фотографии
        cameraUiContainerBinding?.cameraCaptureButton?.setOnClickListener {

            //Получите стабильную ссылку на случай использования изменяемого захвата изображения
            imageCapture?.let { imageCapture ->

                // Создайте выходной файл для хранения изображения
                val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)

                // Настройка метаданных захвата изображения
                val metadata = ImageCapture.Metadata().apply {

                    // Зеркальное изображение при использовании фронтальной камеры
                    isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
                }

                // Создайте объект параметров вывода, который содержит файл + метаданные
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                    .setMetadata(metadata)
                    .build()

                imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()),
                    object : ImageCapture.OnImageCapturedCallback() {
                        @SuppressLint("UnsafeOptInUsageError")
                        override fun onCaptureSuccess(image: ImageProxy) {
                            super.onCaptureSuccess(image)

                            val bitmap = YUVtoRGB().translateYUV(image.image, requireContext())
                            val uri =
                                savePhotoToInternalStorage(UUID.randomUUID().toString(), bitmap)
                            image.close()
                            publishResults(PhotoFragment.KEY_ARGS_SAVE_URI, uri)
                            findNavController().popBackStack()
                        }

                        override fun onError(exception: ImageCaptureException) {
                            super.onError(exception)
                            exception.printStackTrace()
                        }
                    })

//                // Настройте прослушиватель захвата изображения, который срабатывает после того, как фотография была сделана.
//                imageCapture.takePicture(
//                    outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
//                        override fun onError(exc: ImageCaptureException) {
//                            Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
//                        }
//
//                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                            val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
//                            Log.d(TAG, "Photo capture succeeded: $savedUri")
//
//                            // Мы можем изменить передний план Drawable только с помощью API уровня 23+ API.
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                // Update the gallery thumbnail with latest picture taken
//                                setGalleryThumbnail(savedUri)
//                            }
//
//                            // Неявные широковещательные рассылки будут игнорироваться для устройств с уровнем API >= 24.
//                            // поэтому, если вы нацелены только на уровень API 24+, вы можете удалить это утверждение.
//                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//                                requireActivity().sendBroadcast(
//                                    Intent(android.hardware.Camera.ACTION_NEW_PICTURE, savedUri)
//                                )
//                            }
//
//                            // Если выбранная папка является каталогом внешнего носителя, это
//                            // ненужным, но в противном случае другие приложения не смогут получить доступ к нашему
//                            // изображения, если мы не сканируем их с помощью [MediaScannerConnection]
//                            val mimeType = MimeTypeMap.getSingleton()
//                                .getMimeTypeFromExtension(savedUri.toFile().extension)
//                            MediaScannerConnection.scanFile(
//                                context,
//                                arrayOf(savedUri.toFile().absolutePath),
//                                arrayOf(mimeType)
//                            ) { _, uri ->
//                                Log.d(TAG, "Image capture scanned into media store: $uri")
//                            }
//                        }
//                    })


//                // Мы можем изменить передний план Drawable только с помощью API уровня 23+ API.
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//                    // Display flash animation to indicate that photo was captured
//                    fragmentCameraBinding.root.postDelayed({
//                        fragmentCameraBinding.root.foreground = ColorDrawable(Color.WHITE)
//                        fragmentCameraBinding.root.postDelayed(
//                            { fragmentCameraBinding.root.foreground = null }, ANIMATION_FAST_MILLIS)
//                    }, ANIMATION_SLOW_MILLIS)
//                }
            }
        }

        // Настройка кнопки, используемой для переключения камер
        cameraUiContainerBinding?.cameraSwitchButton?.let {

            // Отключите кнопку, пока камера не будет настроена
            it.isEnabled = false

            // Прослушиватель кнопки, используемой для переключения камер. Вызывается, только если кнопка включена
            it.setOnClickListener {
                lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }
                // Повторно привязать варианты использования для обновления выбранной камеры
                bindCameraUseCases()
            }
        }

//        // Listener for button used to view the most recent photo
//        cameraUiContainerBinding?.photoViewButton?.setOnClickListener {
//            // Only navigate when the gallery has photos
//            if (true == outputDirectory.listFiles()?.isNotEmpty()) {
//                Navigation.findNavController(
//                    requireActivity(), androidx.camera.core.R.id.fragment_container
//                ).navigate(CameraFragmentDirections
//                    .actionCameraToGallery(outputDirectory.absolutePath))
//            }
//        }
        cameraUiContainerBinding?.photoViewButton?.setOnClickListener {
            if (true == outputDirectory.listFiles()?.isNotEmpty()) {
//                findNavController().navigate(
//                    R.id.action_cameraFragment_to_composeUploadFragment,
//                    bundleOf(
//                        "outputDirectory" to outputDirectory.absolutePath
//                    )
//                )
            }
        }
    }

    private fun observeCameraState(cameraInfo: CameraInfo) {
        cameraInfo.cameraState.observe(viewLifecycleOwner) { cameraState ->
            run {
                when (cameraState.type) {
                    CameraState.Type.PENDING_OPEN -> {
                        // Ask the user to close other camera apps
                        Toast.makeText(
                            context,
                            "CameraState: Pending Open",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.Type.OPENING -> {
                        // Show the Camera UI
                        Toast.makeText(
                            context,
                            "CameraState: Opening",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.Type.OPEN -> {
                        // Setup Camera resources and begin processing
                        Toast.makeText(
                            context,
                            "CameraState: Open",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.Type.CLOSING -> {
                        // Close camera UI
                        Toast.makeText(
                            context,
                            "CameraState: Closing",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.Type.CLOSED -> {
                        // Free camera resources
                        Toast.makeText(
                            context,
                            "CameraState: Closed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            cameraState.error?.let { error ->
                when (error.code) {
                    // Open errors
                    CameraState.ERROR_STREAM_CONFIG -> {
                        // Make sure to setup the use cases properly
                        Toast.makeText(
                            context,
                            "Stream config error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Opening errors
                    CameraState.ERROR_CAMERA_IN_USE -> {
                        // Close the camera or ask user to close another camera app that's using the
                        // camera
                        Toast.makeText(
                            context,
                            "Camera in use",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.ERROR_MAX_CAMERAS_IN_USE -> {
                        // Close another open camera in the app, or ask the user to close another
                        // camera app that's using the camera
                        Toast.makeText(
                            context,
                            "Max cameras in use",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.ERROR_OTHER_RECOVERABLE_ERROR -> {
                        Toast.makeText(
                            context,
                            "Other recoverable error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Closing errors
                    CameraState.ERROR_CAMERA_DISABLED -> {
                        // Ask the user to enable the device's cameras
                        Toast.makeText(
                            context,
                            "Camera disabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.ERROR_CAMERA_FATAL_ERROR -> {
                        // Ask the user to reboot the device to restore camera function
                        Toast.makeText(
                            context,
                            "Fatal error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Closed errors
                    CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> {
                        // Ask the user to disable the "Do Not Disturb" mode, then reopen the camera
                        Toast.makeText(
                            context,
                            "Do not disturb mode enabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    /**
     *  [androidx.camera.core.ImageAnalysis.Builder] requires enum value of
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     * Определение наиболее подходящего соотношения для размеров, указанных в @params, путем подсчета абсолютных значений
     * коэффициента предварительного просмотра к одному из предоставленных значений.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    /** Enabled or disabled a button to switch cameras depending on the available cameras */
    private fun updateCameraSwitchButton() {
        try {
            cameraUiContainerBinding?.cameraSwitchButton?.isEnabled =
                hasBackCamera() && hasFrontCamera()
        } catch (exception: CameraInfoUnavailableException) {
            cameraUiContainerBinding?.cameraSwitchButton?.isEnabled = false
        }
    }

    /** Returns true if the device has an available back camera. False otherwise */
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    companion object {

        private const val TAG = "CameraXBasic"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        /** Milliseconds used for UI animations */
        const val ANIMATION_FAST_MILLIS = 50L
        const val ANIMATION_SLOW_MILLIS = 100L

        /** Helper function used to create a timestamped file */
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(
                baseFolder, SimpleDateFormat(format, Locale.US)
                    .format(System.currentTimeMillis()) + extension
            )

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }
}