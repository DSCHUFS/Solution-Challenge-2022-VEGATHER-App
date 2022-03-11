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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timer

class MypageActivity : AppCompatActivity() {

    val binding by lazy { ActivityMypageBinding.inflate(layoutInflater)}
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var user : FirebaseUser
    private lateinit var currentUserRef : DocumentReference
    val userInfo = UserDTO()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance() //현재 로그인한 사용자 가져오기
        db = FirebaseFirestore.getInstance()
        user = auth.currentUser!!
        currentUserRef = db.collection("Users").document(user.email.toString())
        currentUserRef.get().addOnSuccessListener { binding.userNickname.text = it.data?.get("NickName").toString() }

        val customUiBar = UiBar(window)
        customUiBar.setStatusBarIconColor(isBlack = false)
        customUiBar.setNaviBarIconColor(isBlack = true)
        customUiBar.setStatusBarTransparent()
        binding.attendanceNum.text = '+' + MyApplication.prefs.getAttend("now", 1).toString()
        textHighlightingDailyMission(binding.checkAttendance, binding.attendanceNum)

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
            val intentMyRecord = Intent(this,MyPostRecordActivity::class.java)
            intentMyRecord.putExtra("category","Posting")
            startActivity(intentMyRecord)
        }

        //오늘안에 했으면 불들어오는 버튼 들
        var checkList = mutableMapOf(
            "postingCheck" to MyApplication.prefs.getPrefs("Posting","Yet"),
            "CommentCheck" to MyApplication.prefs.getPrefs("Comment", "Yet"),
            "LikeCheck" to MyApplication.prefs.getPrefs("Like", "Yet")
        )
        with(binding){
            var list1 = mutableMapOf("postingCheck" to checkPosting,"CommentCheck" to checkComment,"LikeCheck" to checkLike)
            var list2 = mutableMapOf("postingCheck" to postingNum,"CommentCheck" to commentNum,"LikeCheck" to likeNum)
            checkList.forEach{key, value ->
                if(value == "Done") {
                    list1[key]?.setBackgroundResource(R.drawable.ingredient_background_green)
                    list1[key]?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mypage_circle_green, 0, 0, 0)
                    list1[key]?.let { list2[key]?.let { it1 ->
                        textHighlightingDailyMission(it, it1)
                    } }
                }else{
                    list1[key]?.setBackgroundResource(R.drawable.comment_input_background)
                    list1[key]?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mypage_circle_gray, 0, 0, 0)
                }
            }
        }
    }
//User History DB에 저장된 데이터가 몇개인지 세서 binding 바꾸어주어야함.
    override fun onStart() {
        super.onStart()
        currentUserRef.collection("History")
            .get()
            .addOnSuccessListener {

                for (document in it){
                    Log.d("My  DOCUMENT ID History List ====>", document.data.toString())

                    when(document.id){
                        "Comment" -> {
                            var brn = document.toObject(HistoryLikedRecipe::class.java).size()
                            binding.commentNum.text = '+' + brn.toString()
                            Log.d("My  COMMNENT ~~~ History List ====>",brn.toString())

                        }
                    }
                }
            }

        var i : Int = 0
        timer(period = 2, initialDelay = 500){
            i++
            binding.circleBar.setProgress(i.toFloat())
            if(i==290) {cancel()}
        }
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