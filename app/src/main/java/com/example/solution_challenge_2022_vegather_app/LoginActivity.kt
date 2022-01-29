package com.example.solution_challenge_2022_vegather_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    val binding by lazy {ActivityLoginBinding.inflate(layoutInflater)}
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        binding.btnEmailLogin.setOnClickListener{
            signInEmail()
        }

        binding.btnSignup.setOnClickListener {
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        var currentUser : FirebaseUser? = auth?.currentUser
    }

    fun signInEmail() {
        Log.d(binding.editTestEmail.text.toString(), binding.eidtTextPw.text.toString())
        auth?.signInWithEmailAndPassword(binding.editTestEmail.text.toString(), binding.eidtTextPw.text.toString())

            ?.addOnCompleteListener {
                task ->
                if(task.isSuccessful){
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this,"로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

//        Log.d(binding.editTestEmail.text.toString(), binding.eidtTextPw.text.toString())
//        auth?.createUserWithEmailAndPassword(binding.editTestEmail.text.toString(), binding.eidtTextPw.text.toString())
//            ?.addOnCompleteListener {
//                task ->
//                if(task.isSuccessful){
//                    val intent = Intent(this, MainActivity::class.java)
//                    startActivity(intent)
//                } else{
//                    Toast.makeText(this,"회원 가입 실패", Toast.LENGTH_SHORT).show()
//                }
//            }
    }

}