package com.example.solution_challenge_2022_vegather_app

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityLoginBinding
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
    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance() //현재 로그인한 사용자 가져오기
        db = FirebaseFirestore.getInstance()
//        auth.signOut()
        // goole login을 위한 사전처리. gogole signin option 개체 생성
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //facebook 로그인
        callbackManager = CallbackManager.Factory.create()


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
            googleLogin()
        }

        //페이스북 로그인
        binding.btnFacebookLogin.setOnClickListener {
            Toast.makeText(this,"Coming Soon", Toast.LENGTH_SHORT).show()
            //facebookLogin()
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
        if (requestCode == RC_SIGN_IN) { //구글로그인 콜백
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
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
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    checkUserData(auth.currentUser?.email.toString(), auth.currentUser?.displayName.toString(), "google" )
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
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

        // 성공 시 Firebase (로그인)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener{
                    task ->
                if(task.isSuccessful){ // 정상적으로 email, password 가 전달된 경우
                    //최초 로그인인지 확인
                    checkUserData(auth.currentUser?.email.toString(), auth.currentUser?.displayName.toString(), "facebook")
                } else {
                    // 예외 발생 시 메시지 출력
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    //SNS 로그인 시, 최초 로그인인지 구분
    private fun checkUserData(email : String, nickname : String, login : String){
        db.collection("Users").document(email).get()
            .addOnSuccessListener {
                if(it.exists()){
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    //처음 로그인이면 db 생성
                    makeUserData(nickname, email, login)
                }
            }
    }

    //DB 처리
    private fun makeUserData(nickname : String, email : String, login : String){
        val user: MutableMap<String, Any> = HashMap()
        user["NickName"] = nickname
        user["Email"] = email
        user["Point"] = 0
        user["VeganLevel"] = 1
        user["LoginWith"] = login

        val newLikeData = hashMapOf(
            "basicRecipe" to ArrayList<String>(),
            "communityRecipe" to ArrayList<String>()
        )

        val newCommentData = hashMapOf(
            "basicComment" to HashMap<String,Int>(),
            "communityComment" to HashMap<String,Int>()
        )

        val newPostingData = hashMapOf(
            "posting" to ArrayList<String>()
        )

        val newSearchData = hashMapOf(
            "basicSearch" to HashMap<String,String>(),
            "communitySearch" to HashMap<String,String>()
        )

        //User DB에 초기 정보 저장
        db.collection("Users").document(email)
            .set(user)
            .addOnSuccessListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error writing document", e) }

        db.collection("Users").document(email)
            .collection("History")
            .document("Like")
            .set(newLikeData)
            .addOnSuccessListener {
                Log.d("createHistoryLike","success")
            }
            .addOnFailureListener {
                Log.d("createHistoryLike","fail")
            }

        db.collection("Users").document(email)
            .collection("History")
            .document("Comment")
            .set(newCommentData)
            .addOnSuccessListener {
                Log.d("createHistoryComment","success")
            }
            .addOnFailureListener {
                Log.d("createHistoryComment","fail")
            }

        db.collection("Users").document(email)
            .collection("History")
            .document("Posting")
            .set(newPostingData)
            .addOnSuccessListener {
                Log.d("createHistoryPosting","success")
            }
            .addOnFailureListener {
                Log.d("createHistoryPosting","fail")
            }

        db.collection("Users").document(email)
            .collection("History")
            .document("Search")
            .set(newSearchData)
            .addOnSuccessListener {
                Log.d("createHistorySearch","success")
            }
            .addOnFailureListener {
                Log.d("createHistorySearch","fail")
            }
    }

}