package com.hootor.tmc_2.screens.main.photo

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hootor.tmc_2.data.media.MediaHelper
import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.media.GetPickedImage
import com.hootor.tmc_2.domain.media.UploadImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class UploadPhotoViewModel @Inject constructor(
    private val getPickedImage: GetPickedImage,
    private val uploadImage: UploadImage
) : ViewModel() {

    private var uri: Uri? = null
    private var qrCode: String = ""
    private var bitmap: Bitmap? = null
    private val _state = MutableStateFlow(ViewState(State.Empty))
    val state = _state.asStateFlow()

    fun setUriFromArgs(argsUri: Uri) {
        uri = argsUri
        update { copy(state = State.Loading) }
        getPickedImage.invoke(
            params = uri,
            scope = viewModelScope
        ) {
            it.fold(::handleFailure, ::handleSuccess)
        }
    }

    fun setQrCodeFromArgs(qrCodeFromRags: String) {
        qrCode = qrCodeFromRags
    }

    fun upload() {
        if (qrCode.isEmpty()){
            update { copy(state = State.Error("Не передан QR CODE")) }
            return
        }
        update { copy(state = State.Uploading) }
        uploadImage.invoke(UploadImage.Params(qrCode, MediaHelper.encodeToBase64(bitmap!!, Bitmap.CompressFormat.JPEG, 100)), viewModelScope)
    }

    private fun handleSuccess(bitmap: Bitmap) {
        this.bitmap = bitmap
        update { copy(state = State.Success(bitmap)) }
    }

    private fun handleFailure(error: Failure) {
        update { copy(state = State.Error(error.toString())) }
    }

    private fun update(mapper: ViewState.() -> ViewState = { this }) {
        _state.value = _state.value.mapper()
    }
}

data class ViewState(
    val state: State,
)

sealed class State {
    object Loading : State()
    object Uploading : State()
    data class Error(val message: String) : State()
    data class Success(val bitmap: Bitmap) : State()
    object Empty : State()
}
