package com.tm.wifidemo

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class PermissionUtil {

    companion object {

        private val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE:Array<String> = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        @JvmStatic
        public fun verifyStoragePermissions(activity: Activity) {
            // Check if we have write permission
            var permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        }
    }



}