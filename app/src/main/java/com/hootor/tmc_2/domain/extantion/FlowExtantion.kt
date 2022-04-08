package com.hootor.tmc_2.domain.extantion

import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.functional.flatMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

public fun <T> flowOf(value: T): Flow<T> = flow {
    emit(value)
}

fun <T, L, R> Either<L, R>.flatMapFlow(fn: (R) -> Flow<Either<L, T>>): Flow<Either<L, T>> =
    when (this) {
        is Either.Left -> flow { Either.Left(a) }
        is Either.Right -> fn(b)
    }