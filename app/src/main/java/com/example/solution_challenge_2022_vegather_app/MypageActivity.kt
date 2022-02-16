package com.example.solution_challenge_2022_vegather_app

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityMypageBinding
import com.example.solution_challenge_2022_vegather_app.model.UserDTO
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.concurrent.schedule

class MypageActivity : AppCompatActivity() {

    val binding by lazy { ActivityMypageBinding.inflate(layoutInflater)}
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var user : FirebaseUser
    val userInfo = UserDTO()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance() //현재 로그인한 사용자 가져오기
        db = FirebaseFirestore.getInstance()
        user = auth.currentUser!!

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
        for(i in 0..290){
            Timer().schedule(1000){
                binding.circleBar.setProgress(i.toFloat())
            }
        }
    }


    //뒤로가기 막기
    //뒤로가기 버튼 누르면 앱이 다운됨
    override fun onBackPressed() {
        //super.onBackPressed()
    }



}