package com.hootor.tmc_2.screens.main.photo

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.util.Size
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hootor.tmc_2.R
import com.hootor.tmc_2.databinding.FragmentTakePhotoBinding
import com.hootor.tmc_2.utils.Event
import com.hootor.tmc_2.utils.FileUtil
import com.hootor.tmc_2.utils.publishResults
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class PhotoFragment : Fragment(R.layout.fragment_take_photo) {

    private lateinit var binding: FragmentTakePhotoBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var orientationEventListener: OrientationEventListener? = null

    private var animationListener: Animation.AnimationListener? =
        object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                binding.takePhoto.isEnabled = false
            }

            override fun onAnimationEnd(p0: Animation?) {
                binding.takePhoto.isEnabled = true
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }

        }

    private val requestPhotoPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        ::onGotPhotoPermissionResult
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        requestPhotoPermissionLauncher.launch(mutableListOf(
            Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentTakePhotoBinding.inflate(inflater, container, false)
        binding.takePhoto.setOnClickListener {
            takePhoto()
            val animation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_animation)
            animation.setAnimationListener(animationListener)
            binding.takePhoto.startAnimation(animation)
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iniOrientationListenerForCamera()
    }

    private fun iniOrientationListenerForCamera() {
        orientationEventListener = object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation: Int) {
                imageCapture?.targetRotation = getOrientationFromDegrees(orientation)
            }
        }.apply {
            enable()
        }

        val rotation = binding.viewFinder.display.rotation
        val isLandscape = rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetResolution(if (isLandscape) Size(1920, 1080) else Size(1080, 1920))
            .setTargetRotation(rotation)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        animationListener = null
        orientationEventListener = null
    }

    private fun getOrientationFromDegrees(orientation: Int): Int {
        return when {
            orientation == OrientationEventListener.ORIENTATION_UNKNOWN -> {
                Surface.ROTATION_0
            }
            orientation >= 315 || orientation < 45 -> {
                Surface.ROTATION_0 //portrait
            }
            orientation < 135 -> {
                //Surface.ROTATION_90
                Surface.ROTATION_270 //landscape
            }
            orientation < 225 -> {
                Surface.ROTATION_180
            }
            else -> {
                //Surface.ROTATION_270
                Surface.ROTATION_90
            }
        }
    }

    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))

    }

    private fun onGotPhotoPermissionResult(isGranted: Map<String, Boolean>) {
        var granted = true
        isGranted.forEach {
            if (granted) {
                granted = it.value
            }
        }
        if (granted) {
            startCamera()
        } else {
            // example of handling 'Deny & don't ask again' user choice
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                || !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) || !shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                askUserForOpeningAppSettings()
            } else {
                Toast.makeText(requireContext(), R.string.permission_denied, Toast.LENGTH_SHORT)
                    .show()
                findNavController().popBackStack()
            }
        }
    }

    private fun askUserForOpeningAppSettings() {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireActivity().packageName, null)
        )
        if (requireActivity().packageManager.resolveActivity(
                appSettingsIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            ) == null
        ) {
            Toast.makeText(
                requireContext(),
                R.string.permissions_denied_forever,
                Toast.LENGTH_SHORT
            ).show()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.permission_denied)
                .setMessage(R.string.permission_denied_forever_message)
                .setPositiveButton(R.string.open) { _, _ ->
                    startActivity(appSettingsIntent)
                }
                .create()
                .show()
        }
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

//

        // Create time stamped name and MediaStore entry.
        val name = FileUtil.getFileName()
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions: ImageCapture.OutputFileOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireContext().contentResolver,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    findNavController().popBackStack()
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {

//                    val filePhoto = createPhotoFile() ?: return
                    if (File(output.savedUri?.path).delete()) {
                        Log.i("happy", "make delete photo")
                    }
                    publishResults(KEY_ARGS_SAVE_URI, output.savedUri)
                    findNavController().popBackStack()
                }
            }
        )
    }

    private fun createPhotoFile() : File? {
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return  try {
            // Create an image file name
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            ).apply {
                // Save a file: path for use with ACTION_VIEW intents
//                currentPhotoPath = absolutePath
//                binding.textView.text = currentPhotoPath
            }
        } catch (ex: IOException) {
            null
        }
    }

    companion object {
        private const val TAG = "PhotoFragment start camera"
        const val KEY_ARGS_SAVE_URI = "savedUri"
    }

}