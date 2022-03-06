package com.example.solution_challenge_2022_vegather_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityMyRecordBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MyRecordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMyRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val title = intent.getStringExtra("category").toString()
        binding.textView59.text = title

        binding.imageButton9.setOnClickListener {
            finish()
        }

        val viewPagerAdapter = MyRecordViewPagerAdapter(this)
        viewPagerAdapter.notifyDataSetChanged()

        binding.vp.adapter = viewPagerAdapter

        val tabList = listOf<String>("Basic","Community")

        TabLayoutMediator(binding.tabLayout2,binding.vp) { tab, position -> tab.text = tabList[position] }.attach()
    }
}