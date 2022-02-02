package com.example.solution_challenge_2022_vegather_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityMypageBinding

class MypageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMypageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageButton14.setOnClickListener(){
            finish()
        }

    }
}