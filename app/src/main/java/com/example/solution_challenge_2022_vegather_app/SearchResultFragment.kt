package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.FragmentSearchResultBinding
import com.example.solution_challenge_2022_vegather_app.databinding.MainPageMoreRecipeRecyclerBinding

class SearchResultFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

         Log.d("확인","프래그먼트")
        val bundle = arguments
        val recipeInfo = bundle?.getParcelableArrayList<RecipeInformation>("foodNameList")
        val binding = FragmentSearchResultBinding.inflate(inflater,container,false)
        binding.textView69.visibility = VISIBLE

        if (recipeInfo != null) {
            if( recipeInfo.isNotEmpty() ) {
                binding.searchResultRecycler.layoutManager = LinearLayoutManager(this.context)

                val adapter = MoreRecipeAdapter(MainPageMoreRecipeRecyclerBinding.inflate(layoutInflater))
                adapter.setData(recipeInfo)
                adapter.loadParentActivity(requireContext())
                binding.searchResultRecycler.adapter = adapter

                binding.textView69.visibility = INVISIBLE
            }
        }
        return binding.root
    }
}