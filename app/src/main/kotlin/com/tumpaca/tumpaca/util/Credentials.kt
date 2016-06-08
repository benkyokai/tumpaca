package com.tumpaca.tumpaca.util

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by amake on 6/5/16.
 */
data class Credentials(var consumerKey: String? = null,
                       var consumerSecret: String? = null,
                       var authToken: String? = null,
                       var authTokenSecret: String? = null): Parcelable {

    companion object {
        @JvmField
        val CREATOR = object: Parcelable.Creator<Credentials> {
            override fun createFromParcel(parcel: Parcel): Credentials {
                val values = ArrayList<String>()
                parcel.readStringList(values)
                val (cKey, cSecret, aToken, aTokenSecret) = values
                return Credentials(cKey, cSecret, aToken, aTokenSecret)
            }

            override fun newArray(size: Int): Array<Credentials?> {
                return arrayOfNulls<Credentials>(size)
            }
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeStringList(listOf(consumerKey, consumerSecret, authToken, authTokenSecret))
    }

    override fun describeContents(): Int {
        return 0
    }

    fun isComplete(): Boolean {
        return listOf(consumerKey, consumerSecret, authToken, authTokenSecret).all { it != null }
    }
}
