package com.tumpaca.tumpaca.util

/**
 * AsyncTaskHelper内で (エラーも含めた) 結果を格納するためのクラス
 * Created by yabu on 8/18/16.
 */
class AsyncTaskResult<T>(val result: T?, val e: Exception? = null) {
    constructor(e: Exception) : this(null, e) {
    }
}