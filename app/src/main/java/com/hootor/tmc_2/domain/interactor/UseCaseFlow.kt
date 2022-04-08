package com.hootor.tmc_2.domain.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class UseCaseFlow<out Type, in Params> where Type : Any? {
    abstract fun run(params: Params): Flow<Type>

    operator fun invoke(
        params: Params
    ): Flow<Type> {
        return run(params)
    }

    class None
}