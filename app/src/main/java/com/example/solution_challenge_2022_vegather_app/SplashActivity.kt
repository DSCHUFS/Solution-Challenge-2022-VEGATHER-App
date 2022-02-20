package com.example.solution_challenge_2022_vegather_app

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class SplashActivity : AppCompatActivity() {

    // splash screen 로딩 시간
    private val timeout: Long = 3000 // 1초

    //@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 화면의 상태바와 네비게이션 바를 모두 투명하게 해서 스플래쉬 화면만 보이게 한다.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val uiBarCustom = UiBar(window)
        uiBarCustom.setStatusBarIconColor(isBlack = false)
        Handler().postDelayed({
            // 이 함수는 타이머가 끝난 후 한번만 실행됨
            startActivity(Intent(this, LoginActivity::class.java))

            // activity 종료
            finish()
        }, timeout)
    }
}