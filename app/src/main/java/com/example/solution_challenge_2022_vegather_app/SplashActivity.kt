package com.example.solution_challenge_2022_vegather_app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate

class SplashActivity : AppCompatActivity() {

    // splash screen 로딩 시간
    private val timeout: Long = 3000 // 1초

    //@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val today: LocalDate = LocalDate.now()

        //하루가 바꼈을 때
        if(today.toString() != MyApplication.prefs.getPrefs("today", "")){
            MyApplication.prefs.setPrefs("today", today.toString())
            MyApplication.prefs.setPrefs("Posting", "Yet")
            MyApplication.prefs.setPrefs("Comment", "Yet")
            MyApplication.prefs.setPrefs("Like", "Yet")

            //출석 횟수 측정
            var attendNum = MyApplication.prefs.getIntPrefs("attend",0)
            if(attendNum == 0){
                MyApplication.prefs.setIntPrefs("attend", 1)
            }else{
                MyApplication.prefs.setIntPrefs("attend", attendNum+1)
                MyApplication.prefs.setIntPrefs("attendNum",
                    MyApplication.prefs.getIntPrefs("attendNum", 0) + 1)
            }
        }


        // 화면의 상태바와 네비게이션 바를 모두 투명하게 해서 스플래쉬 화면만 보이게 한다.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val uiBarCustom = UiBar(window)
        uiBarCustom.setStatusBarIconColor(isBlack = false)
        Handler().postDelayed({
            // 이 함수는 타이머가 끝난 후 한번만 실행됨
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
            // activity 종료
            finish()
        }, timeout)
    }
}