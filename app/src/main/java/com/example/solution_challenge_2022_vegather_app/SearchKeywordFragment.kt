package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.FragmentSearchKeywordBinding
import com.example.solution_challenge_2022_vegather_app.databinding.SearchAutocompleteRecyclerBinding

class SearchKeywordFragment(private val listener: SelectedSearchHistoryListener) : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("text","호출")
        // Inflate the layout for this fragment
        val binding = FragmentSearchKeywordBinding.inflate(inflater,container,false)
        val bundle : Bundle? = arguments
        val recipeInfo = bundle?.getParcelableArrayList<RecipeInformation>("foodNameList")
        val startIndex = bundle?.getIntegerArrayList("startIndex")
        val inputValueLength = bundle?.getInt("inputSearchLength")

        binding.searchKeywordList.layoutManager = LinearLayoutManager(this.context)
        val adapter = AutocompleteSearchAdapter(SearchAutocompleteRecyclerBinding.inflate(layoutInflater),listener)

        if( recipeInfo!=null && startIndex!=null && inputValueLength!=null ){
            adapter.setData(recipeInfo,startIndex,inputValueLength)
            adapter.loadParentActivity(requireContext())
            binding.searchKeywordList.adapter = adapter
        }
        return binding.root
    }
}

