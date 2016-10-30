package com.tumpaca.tp.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.tumpaca.tp.R
import com.tumpaca.tp.model.TPRuntime

/**
 * Created by yabu on 2016/10/26.
 */
object TPToastManager {

    private var toastCounts = 0

    fun show(msg: String) {
        val frameLayout = object : FrameLayout(TPRuntime.mainApplication) {
            override public fun onDetachedFromWindow() {
                super.onDetachedFromWindow()
                toastCounts -= 1
            }
        }
        synchronized(this, {
            val toast = Toast(TPRuntime.mainApplication)
            toast.duration = Toast.LENGTH_SHORT
            val inflater = TPRuntime.mainApplication.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout = inflater.inflate(R.layout.toast, null)
            val textView = layout.findViewById(R.id.tp_toast_text) as TextView
            textView.text = msg
            toast.view = layout

            frameLayout.addView(toast.view)
            toast.view = frameLayout

            //val location = intArrayOf(0, 0)
            //toastView?.getLocationOnScreen(location)

            //Log.v("TPToastManager", "location x=${location.get(0)}, y=${location.get(1)}")
            //toast.setGravity(Gravity.RIGHT or Gravity.TOP, 16, 10 + location.get(1))
            toast.setGravity(Gravity.RIGHT or Gravity.TOP, 16, 10)
            toast.show()
            toastCounts += 1
        })
    }

}