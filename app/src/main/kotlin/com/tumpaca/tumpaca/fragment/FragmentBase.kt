package com.tumpaca.tumpaca.fragment

import android.support.v4.app.Fragment
import com.tumpaca.tumpaca.MainApplication
import com.tumpaca.tumpaca.R

abstract class FragmentBase : Fragment() {

    protected fun getMainApplication(): MainApplication {
        return (activity.application as MainApplication)
    }

    protected fun replaceFragment(fragment:Fragment, addStack: Boolean) {
        val transaction = activity.supportFragmentManager.beginTransaction()
        if (addStack) {
            transaction.addToBackStack(null)
        }
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
}
