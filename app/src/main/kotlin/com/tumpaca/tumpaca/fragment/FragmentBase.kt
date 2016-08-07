package com.tumpaca.tumpaca.fragment

import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import com.tumpaca.tumpaca.R

abstract class FragmentBase : Fragment() {
    protected fun replaceFragment(fragment:Fragment, addStack: Boolean) {
        val transaction = activity.supportFragmentManager.beginTransaction()
        if (addStack) {
            transaction.addToBackStack(null)
        }
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    protected fun getActionBar(): ActionBar? {
        val activity = activity
        if (activity is AppCompatActivity) {
            return activity.supportActionBar
        }
        return null
    }

}
