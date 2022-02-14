package com.example.solution_challenge_2022_vegather_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommentReplyBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommentRecyclerBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommentReplyRecyclerBinding

class CommentReplyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val binding = ActivityCommentReplyBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.imageButton3.setOnClickListener {
            finish()
        }

        val intent = intent
        val nickname = intent.getStringExtra("nickname").toString()
        val comment = "my name is boo.. i'm ironman!"
        val date = "2014.03.21"
        val like = "23"

        binding.textView38.text = nickname
        binding.textView39.text = comment
        binding.date.text = date
        binding.like.text = like

        binding.replyRecycler.layoutManager = LinearLayoutManager(this)
        val adapter = CommentReplyAdapter(CommentReplyRecyclerBinding.inflate(layoutInflater))
        adapter.settingData()
        binding.replyRecycler.adapter = adapter


    }
}