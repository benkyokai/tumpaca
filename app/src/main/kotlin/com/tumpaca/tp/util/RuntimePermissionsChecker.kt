package com.tumpaca.tp.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.content.PermissionChecker

class RuntimePermissionsChecker {
    enum class Result {
        GRANTED,
        DENIED,
        INVALID
    }

    companion object {
        private val STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        @JvmStatic fun checkSelfStoragePermissions(context: Context?): Boolean {
            if (context == null) {
                throw IllegalArgumentException("context is null")
            }
            return checkPermissionsImpl(context, STORAGE_PERMISSIONS)
        }

        @JvmStatic fun requestStoragePermissions(activity: Activity?, requestCode: Int): Boolean {
            if (activity == null) {
                throw IllegalArgumentException("activity is null")
            }
            return requestPermissionsImpl(activity, STORAGE_PERMISSIONS, requestCode)
        }

        @JvmStatic fun validateStoragePermissionsResult(permissions: Array<String>, grantResults: IntArray): Result {
            if (!isValidPermissionAndResults(permissions, grantResults, STORAGE_PERMISSIONS)) {
                return Result.INVALID
            }

            return if (grantResults[0] == PackageManager.PERMISSION_GRANTED) Result.GRANTED else Result.DENIED
        }

        @JvmStatic private fun checkPermissionsImpl(context: Context, permissions: Array<String>): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return true
            }

            return permissions
                    .map { PermissionChecker.checkSelfPermission(context, it) }
                    .none { it != PermissionChecker.PERMISSION_GRANTED }
        }

        @JvmStatic private fun requestPermissionsImpl(activity: Activity, permissions: Array<String>, requestCode: Int): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return false
            }

            try {
                activity.requestPermissions(permissions, requestCode)
            } catch (e: Exception) {
                return false
            }

            return true
        }


        @JvmStatic private fun isValidPermissionAndResults(actualPermissions: Array<String>?, actualGrantResults: IntArray?, expectedPermissions: Array<String>): Boolean {
            if (actualPermissions == null || actualGrantResults == null) {
                return false
            }

            val sameLength = actualPermissions.size == expectedPermissions.size && actualGrantResults.size == expectedPermissions.size
            if (!sameLength) {
                return false
            }

            var i = 0
            val size = expectedPermissions.size
            while (i < size) {
                // expectedPermissions は null はありえない。
                if (expectedPermissions[i] != actualPermissions[i]) {
                    return false
                }
                i++
            }
            return true
        }
    }
}
