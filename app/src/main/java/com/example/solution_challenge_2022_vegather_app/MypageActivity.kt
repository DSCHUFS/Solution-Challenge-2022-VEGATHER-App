package com.example.solution_challenge_2022_vegather_app

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityMypageBinding
import com.example.solution_challenge_2022_vegather_app.model.UserDTO
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timer

class MypageActivity : AppCompatActivity() {

    val binding by lazy { ActivityMypageBinding.inflate(layoutInflater)}
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var user : FirebaseUser
    val userInfo = UserDTO()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance() //현재 로그인한 사용자 가져오기
        db = FirebaseFirestore.getInstance()
        user = auth.currentUser!!

        val customUiBar = UiBar(window)
        customUiBar.setStatusBarIconColor(isBlack = false)
        customUiBar.setNaviBarIconColor(isBlack = true)
        customUiBar.setStatusBarTransparent()


        binding.btnBack.setOnClickListener(){
            finish()
        }

        //로그아웃 테스트 버튼
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            //페이스북 연동의 경우 아래 코드를 추가해주어야함.
            //그러려면, User가 로그인한게 이메일인지, 구글인지, 페북인지 DB에 따로 저장해야할듯.
            LoginManager.getInstance().logOut()
            val intentMain = Intent(this, LoginActivity::class.java) //메인으로 바로이동
            startActivity(intentMain)
            // activity 종료
            finish()
        }

        //db 테스트 버튼
        binding.button2.setOnClickListener {
            val userRef = db.collection("Users").document(user.email.toString())
            userRef.update("Point", FieldValue.increment(2))
        }

        binding.btnLike.setOnClickListener {
            intentMyRecordActivityFrom("Like")
        }
        binding.btnComment.setOnClickListener {
            intentMyRecordActivityFrom("Comment")
        }
        binding.btnPosting.setOnClickListener {
            intentMyRecordActivityFrom("Posting")
        }

//        binding.radioButton1.setOnCheckedChangeListener { buttonView, isChecked ->
//            textHighlightingDailyMission(isChecked,buttonView,binding.attendanceNum)
//        }
//        binding.radioButton2.setOnCheckedChangeListener { buttonView, isChecked ->
//            textHighlightingDailyMission(isChecked,buttonView,binding.postingNum)
//        }
//        binding.radioButton3.setOnCheckedChangeListener { buttonView, isChecked ->
//            textHighlightingDailyMission(isChecked,buttonView,binding.commentNum)
//        }
//        binding.radioButton4.setOnCheckedChangeListener { buttonView, isChecked ->
//            textHighlightingDailyMission(isChecked,buttonView,binding.likeNum)
//        }

    }

    override fun onStart() {
        super.onStart()
        user?.let{
            val email = user.email
            val userRef = db.collection("Users").document(email.toString())
            userRef.get()
                .addOnSuccessListener { document ->
                    if(document != null) {
                        //Log.d(TAG, "$document.data")
                        userInfo.nickName =  document.data?.get("NickName").toString()
                        userInfo.email = document.data?.get("Email").toString()
                        userInfo.monthlyEat = document.data?.get("MonthlyEat") as Long?
                        userInfo.point = document.data?.get("Point") as Long?

                        binding.userNickname.text = userInfo.nickName
                    }
                }
                .addOnFailureListener{ exception ->
                    Log.d(ContentValues.TAG, "get fail with", exception)
                }
        }
        with(binding){
            btnAttendance.setBackgroundResource(R.drawable.ingredient_background_green)
            btnAttendance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mypage_circle_green, 0, 0, 0)
            textHighlightingDailyMission(btnAttendance, attendanceNum)
        }

        var i : Int = 0
        timer(period = 2, initialDelay = 500){
            i++
            binding.circleBar.setProgress(i.toFloat())
            if(i==290) {cancel()}
        }
    }


    //뒤로가기 막기
    //뒤로가기 버튼 누르면 앱이 다운됨
    override fun onBackPressed() {
        //super.onBackPressed()
    }

    private fun intentMyRecordActivityFrom(text : String){
        val intentMyRecord = Intent(this,MyRecordActivity::class.java)
        intentMyRecord.putExtra("category",text)
        startActivity(intentMyRecord)
    }

    private fun textHighlightingDailyMission(dailyButton : Button, pointNumber : TextView){
        dailyButton.setTextColor(Color.parseColor("#81E768"))
        pointNumber.setTextColor(Color.parseColor("#81E768"))
    }
}