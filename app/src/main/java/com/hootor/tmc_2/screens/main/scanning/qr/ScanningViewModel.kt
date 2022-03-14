package com.hootor.tmc_2.screens.main.scanning.qr

import androidx.lifecycle.ViewModel
import com.hootor.tmc_2.utils.Event
import com.hootor.tmc_2.utils.MutableLiveEvent
import com.hootor.tmc_2.utils.publishEvent
import com.hootor.tmc_2.utils.share
import javax.inject.Inject

class ScanningViewModel @Inject constructor(): ViewModel()  {

    private val _qrCode = MutableLiveEvent<String>()
    val qrCode = _qrCode.share()

    fun eventQrCode(qrCode: String){
        _qrCode.publishEvent(qrCode)
    }

}