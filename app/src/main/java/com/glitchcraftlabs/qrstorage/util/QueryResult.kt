package com.glitchcraftlabs.qrstorage.util

sealed class QueryResult<T>(val data: T? = null, val message: String? = null) {

    class Success<T>(data: T) : QueryResult<T>(data)
    class Error<T>(message: String?, data: T? = null) : QueryResult<T>(data, message)
    class Loading<T> : QueryResult<T>()

}