package com.hootor.tmc_2.screens.main.scanning.qr

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.zxing.Result
import com.hootor.tmc_2.App
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ScanningQRFragment: Fragment(), ZXingScannerView.ResultHandler {

    private lateinit var viewModelScanning: ScanningViewModel
    private lateinit var mScannerView: ZXingScannerView

    private val component by lazy {
        (requireActivity().application as App).component
    }

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModelScanning = ViewModelProvider(requireActivity())[ScanningViewModel::class.java]
        mScannerView = ZXingScannerView(requireActivity())
        return mScannerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        mScannerView.setResultHandler(this)
        mScannerView.startCamera()
    }

    override fun onPause() {
        mScannerView.stopCameraPreview()
        mScannerView.stopCamera()
        super.onPause()
    }
    override fun handleResult(rawResult: Result?) {
        rawResult?.apply {
            viewModelScanning.eventQrCode(this.text)
            findNavController().popBackStack()
        }
    }
}