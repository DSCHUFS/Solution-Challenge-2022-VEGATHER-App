package com.example.solution_challenge_2022_vegather_app

import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.media.FaceDetector
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityLoginBinding
import com.example.solution_challenge_2022_vegather_app.model.UserDTO
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class LoginActivity : AppCompatActivity() {

    val binding by lazy {ActivityLoginBinding.inflate(layoutInflater)}
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var callbackManager: CallbackManager
    val userInfo = UserDTO()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance() //현재 로그인한 사용자 가져오기
        db = FirebaseFirestore.getInstance()
        callbackManager = CallbackManager.Factory.create()


        userInfo.uid = auth?.uid.toString() // 로그인한 사용자 id 받아오기

        Log.d("auth", auth.toString())

        //비번 찾기
        binding.btnFindPw.setOnClickListener {
            val intent = Intent(this, FindPasswordActivity::class.java)
            startActivity(intent)
        }

        //이메일로 회원가입
        binding.btnSignup.setOnClickListener {
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

        //이메일 로그인 구현
       binding.btnEmailLogin.setOnClickListener{
           val email = binding.editTextEmail.text.toString()
           val pw = binding.eidtTextPw.text.toString()
           if(email == "" || pw == ""){
               Toast.makeText(this,"Please write your email & password.", Toast.LENGTH_SHORT).show()
           }else{
               signInEmail()
           }
        }

        //구글 계정으로 로그인
        binding.btnGoogleLogin.setOnClickListener {
            val intent = Intent(this, GoogleLoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        //페이스북 로그인
        binding.btnFacebookLogin.setOnClickListener {
            facebookLogin()
        }
    }

    override fun onStart() {
        super.onStart()
        var currentUser : FirebaseUser? = auth?.currentUser //현재 로그인한 사용자 가져오기
        if(currentUser != null){ //로그인한 상태일 때
            currentUser?.let{
//                val email = currentUser.email
//                val userRef = db.collection("Users").document(email.toString())
//                userRef.get()
//                    .addOnSuccessListener { document ->
//                        if(document != null) {
//                            //Log.d(TAG, "$document.data")
//                            userInfo.nickName =  document.data?.get("NickName").toString()
//                            Toast.makeText(this,"안녕하세요. ${userInfo.nickName}님!", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                    .addOnFailureListener{ exception ->
//                        Log.d(TAG, "get fail with", exception)
//                    }
            }
            val intentMain = Intent(this, MainActivity::class.java) //메인으로 바로이동
            startActivity(intentMain)
            // activity 종료
            finish()
        }
    }


    //이메일 로그인
    private fun signInEmail() {
        Log.d(binding.editTextEmail.text.toString(), binding.eidtTextPw.text.toString())
        auth?.signInWithEmailAndPassword(binding.editTextEmail.text.toString(), binding.eidtTextPw.text.toString())
            ?.addOnCompleteListener {
                task ->
                if(task.isSuccessful){
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this,"로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    //페이스북 로그인
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun facebookLogin() {
        LoginManager.getInstance()
            .logInWithReadPermissions(this, listOf("email", "public_profile"))

        LoginManager.getInstance()
            .registerCallback(callbackManager, object: FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    if (result?.accessToken != null) {
                        // facebook 계정 정보를 firebase 서버에게 전달(로그인)
                        val accessToken = result.accessToken

                        firebaseAuthWithFacebook(result?.accessToken)
                    } else {
                        Log.d("Facebook", "Fail Facebook Login")
                    }
                }
                override fun onCancel() {
                    //취소가 된 경우 할일
                }
                override fun onError(error: FacebookException?) {
                    //에러가 난 경우 할일
                }
            })
    }

    private fun firebaseAuthWithFacebook(accessToken: AccessToken?) {
        // AccessToken 으로 Facebook 인증
        val credential = FacebookAuthProvider.getCredential(accessToken?.token!!)

        // 성공 시 Firebase 에 유저 정보 보내기 (로그인)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener{
                    task ->
                if(task.isSuccessful){ // 정상적으로 email, password 가 전달된 경우
                    // 로그인 처리
                    // 처음 가입하는 사람
                    val userInfo: MutableMap<String, Any> = HashMap()
                    userInfo["Email"] = auth.currentUser?.email.toString()
                    userInfo["NickName"] = auth.currentUser?.displayName.toString()
                    userInfo["Point"] = 0
                    userInfo["MonthlyEat"] = 0

                    //User DB에 저장
                    db.collection("Users").document(auth.currentUser?.email.toString())
                        .set(userInfo)
                        .addOnSuccessListener {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error writing document", e) }
                } else {
                    // 예외 발생 시 메시지 출력
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }




}