package com.hootor.tmc_2.screens.main.scanning.tmc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.scanning.GetTMCByQrCode
import com.hootor.tmc_2.domain.tmc.ItemField
import com.hootor.tmc_2.domain.tmc.TMC
import com.hootor.tmc_2.domain.tmc.TMCSliderItem
import com.hootor.tmc_2.screens.main.core.Item
import com.hootor.tmc_2.screens.main.scanning.tmc.holders.TMCItem
import com.hootor.tmc_2.screens.main.scanning.tmc.adapter.TMCItemBoolean
import com.hootor.tmc_2.screens.main.scanning.tmc.holders.HorizontalItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class ScanningTMCViewModel @Inject constructor(
    private val getTMCByQrCode: GetTMCByQrCode,
) : ViewModel() {

    private var _currImgListPosition = MutableStateFlow<Int>(-1)
    val currImgListPosition = _currImgListPosition.asStateFlow()

    private var currQrCode = ""
    private val _fSatate = MutableStateFlow<ViewState>(ViewState(state = State.Init))
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

    fun reload() {
        fetchData(currQrCode)
    }

    private fun handleTMC(tmc: TMC) {

        val items = mutableListOf<Item>()

        if (tmc.images.isNotEmpty()) items.add(tmc.images.toHorizontalItem())
        items.addAll(tmc.fields.map { field ->
            if (field.type == "Boolean") {
                field.toTMCItemBoolean()
            } else {
                field.toTMCItem()
            }
        })
        _fSatate.value = ViewState(state = State.Success, items = items, imgs = tmc.images)
    }

    private fun handleFailure(error: Failure) {
        _fSatate.value = ViewState(state = State.Error(error.toString()))
    }

    fun setCurrImgListPosition(imgListPosition: Int) {
        _currImgListPosition.value = imgListPosition
    }

}

data class ViewState(
    val items: List<Item> = emptyList(),
    val imgs: List<TMCSliderItem> = emptyList(),
    val state: State,
    val imgListPosition: Int = -1,
)

sealed class State {
    object Loading : State()
    data class Error(val message: String) : State()
    object Success : State()
    object Empty : State()
    object Init : State()
}

private fun ItemField.toTMCItem(): TMCItem {
    return TMCItem(title = this.title, description = this.description)
}

private fun ItemField.toTMCItemBoolean(): TMCItemBoolean {
    return TMCItemBoolean(this.title, this.description == "true")
}

private fun List<TMCSliderItem>.toHorizontalItem(): HorizontalItem {
    return HorizontalItem(this)
}