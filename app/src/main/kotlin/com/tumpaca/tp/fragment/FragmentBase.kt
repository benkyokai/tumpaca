package com.tumpaca.tp.fragment

import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import com.tumpaca.tp.R

abstract class FragmentBase : Fragment() {
    protected fun replaceFragment(fragment: Fragment, addStack: Boolean, tag: String? = null) {
        val transaction = activity.supportFragmentManager.beginTransaction()
        if (addStack) {
            transaction.addToBackStack(null)
        }
        transaction.replace(R.id.fragment_container, fragment, tag)
        transaction.commit()
    }

    protected fun getActionBar(): ActionBar? {
        return (activity as? AppCompatActivity)?.supportActionBar
    }

}
