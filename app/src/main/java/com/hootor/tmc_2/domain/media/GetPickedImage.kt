package com.hootor.tmc_2.domain.media

import android.graphics.Bitmap
import android.net.Uri
import com.hootor.tmc_2.di.IoDispatcher
import com.hootor.tmc_2.di.MainDispatcher
import com.hootor.tmc_2.domain.interactor.UseCase
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class GetPickedImage @Inject constructor(
    private val mediaRepository: MediaRepository,
    @IoDispatcher
    backgroundContext: CoroutineContext,
    @MainDispatcher
    foregroundContext: CoroutineContext,
) : UseCase<Bitmap, Uri?>(backgroundContext, foregroundContext) {

    override suspend fun run(params: Uri?) = mediaRepository.getPickedImage(params)
}