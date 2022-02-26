package com.example.solution_challenge_2022_vegather_app

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.core.view.WindowCompat

class UiBar( w : Window) {
    private val window = w

    fun setNaviBarIconColor(isBlack: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // android os 12에서 사용 가능
            window.insetsController?.setSystemBarsAppearance(
                if (isBlack) WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 내비바 아이콘 색상이 8.0부터 가능하므로 커스텀은 동시에 진행해야 하므로 조건 동일 처리.
            window.decorView.systemUiVisibility =
                if (isBlack) {
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

                } else {
                    // 기존 uiVisibility 유지
                    // -> 0으로 설정할 경우, 상태바 아이콘 색상 설정 등이 지워지기 때문
                    window.decorView.systemUiVisibility
                }
        }
    }

    fun setStatusBarIconColor(isBlack : Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // android os 12에서 사용 가능
            window.insetsController?.setSystemBarsAppearance(
                if (isBlack) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // minSdk 6.0부터 사용 가능
            window.decorView.systemUiVisibility = if (isBlack) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                // 기존 uiVisibility 유지
                window.decorView.systemUiVisibility
            }
        }
    }

    fun setStatusBarTransparent(){
        if (Build.VERSION.SDK_INT in 21..29) {
            window.statusBarColor = Color.TRANSPARENT
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        } else if (Build.VERSION.SDK_INT >= 30) {
            window.statusBarColor = Color.TRANSPARENT
            // Making status bar overlaps with the activity
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }
}