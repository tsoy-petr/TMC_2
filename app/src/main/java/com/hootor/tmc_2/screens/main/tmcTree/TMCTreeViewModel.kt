package com.hootor.tmc_2.screens.main.tmcTree

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.tmc.TMCTree
import com.hootor.tmc_2.domain.tmc.tree.GetTMCTreeByQrCodeUseCase
import io.github.ikws4.treeview.TreeItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class TMCTreeViewModel @Inject constructor(
    private val getTMCTreeByQrCodeUseCase: GetTMCTreeByQrCodeUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ViewState(state = State.Init))
    val state = _state.asStateFlow()

    private fun update(mapper: ViewState.() -> ViewState = { this }) {
        _state.value = _state.value.mapper()
    }

    fun fetch(qrCode: String) {
        update{ copy(state = State.Loading) }
        getTMCTreeByQrCodeUseCase.invoke(
            GetTMCTreeByQrCodeUseCase.Param(qrCode), viewModelScope
        ) {
            Log.i("TMCTreeViewModel.fetch", it.toString())
            it.fold(::handleFailure, ::handleTMCTree)
        }
    }

    private fun handleTMCTree(tree: TMCTree) {
            update{copy(state = State.Success, root = tree.toModel())}
    }

    private fun handleFailure(error: Failure) {
        if(error is Failure.EmptyData){
            update { copy(state = State.Empty) }
        }else update{ copy(state = State.Error(error.toString())) }
    }

}

data class ViewState(
    var root: TreeItem<TMCTree>? = null,
    val state: State,
)

sealed class State {
    object Loading : State()
    data class Error(val message: String) : State()
    object Success : State()
    object Empty : State()
    object Init : State()
}

fun List<TMCTree>.toModel() : List<TreeItem<TMCTree>> {
    return this.map {
        it.toModel()
    }
}

fun TMCTree.toModel():TreeItem<TMCTree> {
    val root = TreeItem(this, this.expandable)
    this.children.forEach {
        root.children.add(it.toModel())
    }
    return root
}