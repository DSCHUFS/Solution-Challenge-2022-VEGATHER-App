package com.example.solution_challenge_2022_vegather_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.solution_challenge_2022_vegather_app.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageButton.setOnClickListener(){
            finish()
        }
        // 프래그먼트 영역의 default xml은 인기검색어와 검색기록이어야 한다.
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        val fragment = SearchRankingAndHistoryFragment()
        transaction.add(R.id.fragmentContainerView,fragment)
        transaction.commit()


    }
}