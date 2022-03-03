package com.example.solution_challenge_2022_vegather_app

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityLoginBinding
import com.example.solution_challenge_2022_vegather_app.model.UserDTO
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.CallbackManager.Factory.create
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore


class LoginActivity : AppCompatActivity() {
    val binding by lazy {ActivityLoginBinding.inflate(layoutInflater)}
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    val userInfo = UserDTO()
    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance() //현재 로그인한 사용자 가져오기
        db = FirebaseFirestore.getInstance()

        // goole login을 위한 사전처리. gogole signin option 개체 생성
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //facebook 로그인
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
            Log.d("Google Login Test", "1")

            googleLogin()
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


    //소셜 로그인 부분
    val user: MutableMap<String, Any> = HashMap()
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("Google Login Test", "2")

        if (requestCode == RC_SIGN_IN) { //구글로그인 콜백
            Log.d("Google Login Test", "3")

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            Log.d("Google Login Test", "4")

            try {
                val account = task.getResult(ApiException::class.java)!!
                //Log.d(GoogleLoginActivity.TAG, "firebaseAuthWithGoogle:" + account.id)
                user["NickName"] = account.displayName.toString()
                Log.d("Google Login Test", "5")
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("Google Login Test", "Google sign in failed", e)
            }
        }else{ //페이스북로그인 콜백
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }


    //구글 로그인
    private fun googleLogin() {
        Log.d("Google Login Test", "6")
        val signInIntent = googleSignInClient.signInIntent
        Log.d("Google Login Test", "7")

        startActivityForResult(signInIntent, RC_SIGN_IN)
        Log.d("Google Login Test", "8")
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Log.d("Google Login Test", "9")

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Google Login Test", "10")

                    // 처음 가입하는 사람
                    Log.d(GoogleLoginActivity.TAG, "signInWithCredential:success")
                    user["Email"] = auth.currentUser?.email.toString()
                    user["Point"] = 0
                    user["MonthlyEat"] = 0

                    db.collection("Users").document(auth.currentUser?.email.toString())
                        .set(user)
                        .addOnSuccessListener {
                            Log.d("Google Login Test", "11")

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error writing document", e) }
                } else {
                    Log.w(GoogleLoginActivity.TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }


    //페이스북 로그인
    private fun facebookLogin() {
        LoginManager.getInstance()
            .logInWithReadPermissions(this, listOf("email", "public_profile"))

        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult?> {
                override fun onCancel() {}
                override fun onError(exception: FacebookException) {}
                override fun onSuccess(result: LoginResult?) {
                    if (result?.accessToken != null) {
                        // facebook 계정 정보를 firebase 서버에게 전달(로그인)
                        val accessToken = result.accessToken
                        firebaseAuthWithFacebook(accessToken)
                    } else {
                        Log.d("Facebook", "Fail Facebook Login")
                    }
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
                    user["Email"] = auth.currentUser?.email.toString()
                    user["NickName"] = auth.currentUser?.displayName.toString()
                    user["Point"] = 0
                    user["MonthlyEat"] = 0

                    //User DB에 저장
                    db.collection("Users").document(auth.currentUser?.email.toString())
                        .set(user)
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