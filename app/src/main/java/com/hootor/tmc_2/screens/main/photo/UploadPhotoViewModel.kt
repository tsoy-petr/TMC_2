package com.hootor.tmc_2.screens.main.photo

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.extantion.flatMapFlow
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.functional.getOrElse
import com.hootor.tmc_2.domain.functional.map
import com.hootor.tmc_2.domain.functional.onSuccess
import com.hootor.tmc_2.domain.media.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ExperimentalCoroutinesApi
class UploadPhotoViewModel @Inject constructor(
    private val sendImageFlow: SendImageFlow
) : ViewModel() {

    private var uri: Uri? = null
    private var qrCode: String = ""
    private val _state = MutableStateFlow(ViewState(State.Empty))
    val state = _state.asStateFlow()

    fun setUriFromArgs(argsUri: Uri) {
        uri = argsUri
    }

    fun setQrCodeFromArgs(qrCodeFromRags: String) {
        qrCode = qrCodeFromRags
    }

    fun upload() {

        if (qrCode.isEmpty()) {
            update { copy(state = State.Error("Не передан QR CODE")) }
            return
        }

        if (uri == null) {
            update { copy(state = State.Error("Не определена ссылка на изображение")) }
            return
        }

        viewModelScope.launch {

            sendImageFlow(SendImageFlow.Params(qrCode, uri!!)).onStart {
                withContext(Dispatchers.Main) {
                    update { copy(state = State.Uploading) }
                }
            }.flowOn(Dispatchers.IO)
                .collect { res: Either<Failure, Boolean> ->
                    res.fold(::handleError, ::handleUploadResult)
                }
//            getBitmapFromUri(uri!!).onStart {
//                withContext(Dispatchers.Main) {
//                    update { copy(state = State.Uploading) }
//                }
//            }.flatMapLatest { imageBitmap: Either<Failure, Bitmap> ->
//                imageBitmap.flatMapFlow {
//                    sendImageFlow(SendImageFlow.Params(qrCode, it, uri!!))
//                }
//            }.flowOn(Dispatchers.IO)
//                .collect { res: Either<Failure, Boolean> ->
//                    res.fold(::handleError, ::handleUploadResult)
//                }
        }

    }

    private fun handleUploadResult(isUpload: Boolean) {
        update { copy(state = State.PopBackStack) }
    }

    private fun handleError(errorMessage: Failure) {
        update { copy(state = State.Error(errorMessage.toString())) }
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
    object PopBackStack : State()
    data class Error(val message: String) : State()
    data class Success(val bitmap: Bitmap) : State()
    object Empty : State()
}
