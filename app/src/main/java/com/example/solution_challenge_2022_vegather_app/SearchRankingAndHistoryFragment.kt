package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.FragmentSearchRankingAndHistoryBinding
import com.example.solution_challenge_2022_vegather_app.databinding.MainPageMoreRecipeRecyclerBinding
import com.example.solution_challenge_2022_vegather_app.databinding.SearchHistoryRecyclerBinding

class SearchRankingAndHistoryFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentSearchRankingAndHistoryBinding.inflate(inflater,container,false)

        binding.searchHistoryRecycler.layoutManager = LinearLayoutManager(this.context,RecyclerView.HORIZONTAL,false)
        val recycler = SearchHistoryAdapter(SearchHistoryRecyclerBinding.inflate(layoutInflater))
        recycler.settingData()
        binding.searchHistoryRecycler.adapter = recycler

        binding.allClearButton.setOnClickListener {
            recycler.deleteAllSearchHistory()
        }

        return binding.root
    }

}

