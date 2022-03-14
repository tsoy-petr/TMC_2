package com.hootor.tmc_2.screens.main.scanning.tmc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.scanning.GetTMCByQrCode
import com.hootor.tmc_2.domain.tmc.ItemField
import com.hootor.tmc_2.domain.tmc.TMC
import com.hootor.tmc_2.domain.tmc.TMCSliderItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class ScanningTMCViewModel @Inject constructor(
    private val getTMCByQrCode: GetTMCByQrCode,
) : ViewModel() {

    private var _currImgListPosition = MutableStateFlow<Int>(-1)
    val currImgListPosition = _currImgListPosition.asStateFlow()

    private var currQrCode = ""
    private val _fSatate = MutableStateFlow<ViewState>(ViewState(state= State.Init))
    val fState = _fSatate.asStateFlow()

    fun fetchData(qrCode: String) {

        currQrCode = qrCode

        _fSatate.value = ViewState(state = State.Loading)
        getTMCByQrCode.invoke(
            params = GetTMCByQrCode.Params(qrCode),
            scope = viewModelScope
        ) {
            it.fold(::handleFailure, ::handleTMC)
        }

    }

    private fun handleTMC(tmc: TMC) {
        _fSatate.value = ViewState(state = State.Success, items = tmc.fields, imgs = tmc.images)
    }

    private fun handleFailure(error: Failure) {
        _fSatate.value = ViewState(state = State.Error(error.toString()))
    }

    fun setCurrImgListPosition(imgListPosition: Int) {
        _currImgListPosition.value = imgListPosition
    }

}

data class ViewState(
    val items: List<ItemField> = emptyList(),
    val imgs: List<TMCSliderItem> = emptyList(),
    val state: State,
    val imgListPosition: Int = -1
)

sealed class State {
    object Loading : State()
    data class Error(val message: String) : State()
    object Success : State()
    object Empty : State()
    object Init : State()
}