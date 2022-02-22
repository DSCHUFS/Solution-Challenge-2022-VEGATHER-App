package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.FragmentSearchRankingAndHistoryBinding
import com.example.solution_challenge_2022_vegather_app.databinding.SearchHistoryRecyclerBinding

class SearchRankingAndHistoryFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentSearchRankingAndHistoryBinding.inflate(inflater,container,false)
        val bundle = arguments
        val newSearchHistory = bundle?.getString("text")

        binding.top1.setOnClickListener {
            sendFoodInfoToRecipeActivity(binding.top1.text.toString())
        }
        binding.top2.setOnClickListener {
            sendFoodInfoToRecipeActivity(binding.top2.text.toString())
        }
        binding.top3.setOnClickListener {
            sendFoodInfoToRecipeActivity(binding.top3.text.toString())
        }
        binding.top4.setOnClickListener {
            sendFoodInfoToRecipeActivity(binding.top4.text.toString())
        }
        binding.top5.setOnClickListener {
            sendFoodInfoToRecipeActivity(binding.top5.text.toString())
        }

        binding.searchHistoryRecycler.layoutManager = LinearLayoutManager(requireContext(),RecyclerView.HORIZONTAL,false)
        val adapter = SearchHistoryAdapter(SearchHistoryRecyclerBinding.inflate(layoutInflater))
        adapter.settingData()
        if( newSearchHistory!=null ){
            Log.d("text",newSearchHistory.toString())
            adapter.addData(newSearchHistory.toString())
        }
        adapter.loadParentActivity(requireContext())
        binding.searchHistoryRecycler.adapter = adapter

        binding.allClearButton.setOnClickListener {
            adapter.deleteAllSearchHistory()
        }
        return binding.root
    }

    private fun sendFoodInfoToRecipeActivity(foodName : String){
        val context = requireContext()
        val intentRecipe = Intent(context,RecipeMainActivity::class.java)
        intentRecipe.putExtra("callNumberFromAdapter",2)
        intentRecipe.putExtra("foodNameFromAdapter",foodName)
        context.startActivity(intentRecipe)
    }

}

