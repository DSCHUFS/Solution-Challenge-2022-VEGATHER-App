package com.example.solution_challenge_2022_vegather_app

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

abstract class PermissionActivity : AppCompatActivity(){
    abstract fun permissionGranted(requestCode: Int)
    abstract fun permissionDenied(requestCode: Int)

    // 권한 검사
    fun requirePermission(permissions: Array<String>, requestCode: Int) {
        // api 버전이 마시멜로 미만이면 권한처리가 필요없다
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            permissionGranted(requestCode)
        } else {
            // 권한이 없으면 권한 요청 -> 팝업
            ActivityCompat.requestPermissions(this, permissions, requestCode)
        }
    }

    // 결과처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.all {it == PackageManager.PERMISSION_GRANTED}) {
            //grantResult가 모두 승인되었다면 1, 하나라도 안되면 0
            permissionGranted((requestCode))
        } else {
            permissionDenied(requestCode)
        }

        for (result in grantResults) {
            if (result == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }
}