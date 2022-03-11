package com.example.solution_challenge_2022_vegather_app

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter

class MyRecordViewPagerAdapter(fragmentActivity : FragmentActivity, category : String) : FragmentStateAdapter(fragmentActivity) {

    private val fragmentList = listOf<Fragment>(MyRecordBasicFragment(category),MyRecordCommunityFragment(category))

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}