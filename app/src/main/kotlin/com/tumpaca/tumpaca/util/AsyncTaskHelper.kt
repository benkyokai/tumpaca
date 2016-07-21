package com.tumpaca.tumpaca.util

import android.os.AsyncTask

/**
 * Created by amake on 6/3/16.
 */
class AsyncTaskHelper<Params, Progress, Result> private constructor(first: (Array<out Params>) -> Result) {

    companion object {
        fun <Params, Progress, Result> first(doInBackground: (Array<out Params>) -> Result): AsyncTaskHelper<Params, Progress, Result> {
            return AsyncTaskHelper(doInBackground)
        }
    }

    var whenDone: ((Result) -> Unit)? = null

    var task = object: AsyncTask<Params, Progress, Result>() {
        override fun doInBackground(vararg params: Params): Result {
            return first(params)
        }

        override fun onPostExecute(result: Result) {
            whenDone?.invoke(result)
        }
    }

    fun then(onPostExecute: (Result) -> Unit): AsyncTaskHelper<Params, Progress, Result> {
        whenDone = onPostExecute
        return this
    }

    fun go(vararg params: Params) {
        task.execute(*params)
    }
}
