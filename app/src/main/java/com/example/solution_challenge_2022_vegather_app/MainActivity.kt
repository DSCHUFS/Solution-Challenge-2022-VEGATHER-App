package com.example.solution_challenge_2022_vegather_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intentCommunity = Intent(this, CommunityWriteActivity::class.java)
        val intentMypage = Intent(this, MypageActivity::class.java)

       binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
           // 탭 버튼을 선택할 때
           override fun onTabSelected(tab: TabLayout.Tab?) {
               switchActivity(tab)
           }
            // 다른 탭 버튼을 눌러 선택된 탭 버튼이 해제될 때 이벤트
           override fun onTabUnselected(tab: TabLayout.Tab?) {
           }
            // 선택된 탭 버튼을 다시 선택할 때 이벤트
           override fun onTabReselected(tab: TabLayout.Tab?) {
                switchActivity(tab)
           }
            // 선택된 탭에 해당하는 액티비티로 화면 전환
           fun switchActivity(tab: TabLayout.Tab?){
               when(tab?.text){
                   "Community" -> startActivity(intentCommunity)
                   "Mypage" -> startActivity(intentMypage)
               }
           }
       })
    }
}