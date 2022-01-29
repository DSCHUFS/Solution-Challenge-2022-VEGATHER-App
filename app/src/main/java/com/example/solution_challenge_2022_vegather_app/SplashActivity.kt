package com.example.solution_challenge_2022_vegather_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat

class SplashActivity : AppCompatActivity() {

    // splash screen 로딩 시간
    private val timeout: Long = 3000 // 1초

    //@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // status bar 색상 변경
        val window = this.window
        window.statusBarColor = ContextCompat.getColor(this, R.color.main_green)

        Handler().postDelayed({
            // 이 함수는 타이머가 끝난 후 한번만 실행됨
            startActivity(Intent(this, LoginActivity::class.java))

            // activity 종료
            finish()
        }, timeout)
    }
}