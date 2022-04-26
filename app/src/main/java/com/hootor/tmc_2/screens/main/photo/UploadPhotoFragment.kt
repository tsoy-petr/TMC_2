package com.hootor.tmc_2.screens.main.photo

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.hootor.tmc_2.App
import com.hootor.tmc_2.R
import com.hootor.tmc_2.databinding.FragmentUploadPhotoBinding
import com.hootor.tmc_2.di.ViewModelFactory
import com.hootor.tmc_2.screens.main.photo.State.*
import com.hootor.tmc_2.utils.publishResults
import kotlinx.coroutines.launch
import javax.inject.Inject

class UploadPhotoFragment : Fragment(R.layout.fragment_upload_photo) {

    private lateinit var binding: FragmentUploadPhotoBinding

    private lateinit var viewModel: UploadPhotoViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as App).component
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentUploadPhotoBinding.inflate(inflater, container, false)
        binding.buttonUpload.setOnClickListener {
            viewModel.upload()
        }
        binding.buttonReload.setOnClickListener {
            viewModel.upload()
        }

        viewModel = ViewModelProvider(this, viewModelFactory)[UploadPhotoViewModel::class.java]

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { viewState ->
                    handleEvent(viewState)
                }
            }
        }

        requireArguments().apply {
            getParcelable<Uri>(KEY_ARGS_URI_IMAGE)?.let { uri ->
                binding.imageUpload.setImageURI(uri)
//                binding.imageUpload.swipeToDismissEnabled = true

                viewModel.setUriFromArgs(uri)
            } ?: findNavController().popBackStack()
            getString(KEY_ARGS_QR_CODE)?.let { qrCode ->
                viewModel.setQrCodeFromArgs(qrCode)
            } ?: findNavController().popBackStack()
        }

        return binding.root
    }

    private fun handleEvent(event: ViewState) {
        Log.i("happy", event.state.toString())

        when (event.state) {
            is Loading -> {
                showLoading(true)
            }
            is Uploading -> {showLoading(true)}
            is Error -> {showError(event.state.message)}
            is Success -> {

                findNavController().popBackStack()}
            is Empty -> {}
            is PopBackStack -> {
                publishResults(KEY_RESULT_SEND_PHOTO, true)
                findNavController().popBackStack()}
        }
    }

    private fun showError(errorMessage: String) {
        binding.progressBar.isVisible = false
        binding.groupError.isVisible = true
        binding.imageUpload.isVisible = false
        binding.buttonUpload.isVisible = false
        binding.textViewError.text = errorMessage
    }

    private fun showLoading(isShow: Boolean) {
        binding.progressBar.isVisible = isShow
        binding.groupError.isVisible = !isShow
        binding.imageUpload.isVisible = !isShow
        binding.buttonUpload.isVisible = !isShow
    }

    companion object {
        const val KEY_ARGS_URI_IMAGE = "KEY_ARGS_URI_IMAGE"
        const val KEY_ARGS_QR_CODE = "KEY_ARGS_QR_CODE"
        const val KEY_RESULT_SEND_PHOTO = "KEY_RESULT_SEND_PHOTO"
    }

}


