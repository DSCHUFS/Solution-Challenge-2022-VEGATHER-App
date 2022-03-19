package com.example.solution_challenge_2022_vegather_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityFindPasswordBinding
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class FindPasswordActivity : AppCompatActivity() {
    val binding by lazy { ActivityFindPasswordBinding.inflate(layoutInflater)}
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val uiBar = UiBar(window)
        uiBar.setStatusBarIconColor(isBlack = true)

        auth = FirebaseAuth.getInstance() //현재 로그인한 사용자 가져오기

        binding.btnJoin.setOnClickListener {
            val email = binding.editTextJoinEmail.text.toString()
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "재설정 메일을 보냈습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.btnBack.setOnClickListener{
            finish()
        }

    }
}