package com.tumpaca.tumpaca.util

import android.os.AsyncTask

/**
 * Created by amake on 6/3/16.
 */
abstract class AsyncTaskHelper<Params, Progress, Result> () {

    companion object {
        private const val TAG = "AsyncTaskHelper"
    }

    abstract fun doTask(params: (Array<out Params>)): Result

    abstract fun onError(e:Exception)

    abstract fun onSuccess(result: Result)

    private var task = object: AsyncTask<Params, Progress, AsyncTaskResult<Result>>() {
        override fun doInBackground(vararg params: Params): AsyncTaskResult<Result> {
            try {
                val result = doTask(params)
                return AsyncTaskResult(result)
            } catch (e: Exception) {
                return AsyncTaskResult(e)
            }
        }

        override fun onPostExecute(result: AsyncTaskResult<Result>) {
            if (result.e != null) {
                onError(result.e!!)
            } else if (isCancelled) {
                // TODO キャンセル処理
            } else {
                if (result.result != null) {
                    onSuccess(result.result!!)
                } else {
                    throw RuntimeException("doInBackground result is null")
                }
            }
        }
    }

    fun go(vararg params: Params) {
        task.execute(*params)
    }
}
