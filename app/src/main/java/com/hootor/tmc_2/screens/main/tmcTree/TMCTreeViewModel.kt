package com.hootor.tmc_2.screens.main.tmcTree

import androidx.lifecycle.ViewModel
import com.hootor.tmc_2.domain.tmc.tree.GetTMCTreeByQrCodeUseCase
import javax.inject.Inject

class TMCTreeViewModel @Inject constructor(
    private val getTMCTreeByQrCodeUseCase: GetTMCTreeByQrCodeUseCase
): ViewModel() {

}