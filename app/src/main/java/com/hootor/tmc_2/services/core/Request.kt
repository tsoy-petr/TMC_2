package com.hootor.tmc_2.services.core

import com.hootor.tmc_2.di.ApplicationScope
import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@ApplicationScope
class Request @Inject constructor() {

    fun <T : BaseResponse, R> make(call: Call<T>, transform: (T) -> R): Either<Failure, R> {
            return execute(call, transform)
    }

    private fun <T : BaseResponse, R> execute(call: Call<T>, transform: (T) -> R): Either<Failure, R> {
        return try {
            val response = call.execute()
            when (response.isSucceed()) {
                true -> Either.Right(transform((response.body()!!)))
                false -> Either.Left(response.parseError())
            }
        } catch (exception: Throwable) {
            Either.Left(Failure.ServerError)
        }
    }
}

fun <T : BaseResponse> Response<T>.isSucceed(): Boolean {
    return isSuccessful && body() != null && (body() as BaseResponse).success == 1
}

fun <T : BaseResponse> Response<T>.parseError(): Failure {
    val message = (body() as BaseResponse).message
    return Failure.ServerError
//    return when (message) {
//        "there is a user has this email",
//        "email already exists" -> Failure.EmailAlreadyExistError
//        "error in email or password" -> Failure.AuthError
//        "Token is invalid" -> Failure.TokenError
//        "this contact is already in your friends list" -> Failure.AlreadyFriendError
//        "already found in your friend requests",
//        "you requested adding this friend before" -> Failure.AlreadyRequestedFriendError
//        "No Contact has this email" -> Failure.ContactNotFoundError
//        " this email is not registered before" -> Failure.EmailNotRegisteredError
//        "can't send email to you" -> Failure.CantSendEmailError
//        else -> Failure.ServerError
//    }
}