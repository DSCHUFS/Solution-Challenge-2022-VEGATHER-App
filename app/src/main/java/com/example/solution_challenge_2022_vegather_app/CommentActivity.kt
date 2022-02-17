package com.example.solution_challenge_2022_vegather_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommentBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommentRecyclerBinding

class CommentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageButton4.setOnClickListener {
            finish()
        }

        binding.commentRecycler.layoutManager = LinearLayoutManager(this)
        val adapter = CommentAdapter(CommentRecyclerBinding.inflate(layoutInflater))
        adapter.settingData()
        adapter.loadParentActivity(this)
        binding.commentRecycler.adapter = adapter
    }


}