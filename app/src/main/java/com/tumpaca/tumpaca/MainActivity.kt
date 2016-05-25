package com.tumpaca.tumpaca

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val i = 2

        when (i) {
            0 -> setText("i is 0")
            in 1..2 -> setText("i is 1 or 2")
            3, 4 -> setText("i is 3 or 4")
            5 or 6 -> setText("i is 5 or 6")
            else -> setText("else")
        }

    }

    fun setText(message: String) {
        val textview = this.findViewById(R.id.Message) as TextView?
        textview?.setText(message)
    }
}
