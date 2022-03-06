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
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.ActivitySearchBinding
import com.example.solution_challenge_2022_vegather_app.databinding.FragmentSearchRankingAndHistoryBinding
import com.example.solution_challenge_2022_vegather_app.databinding.SearchHistoryRecyclerBinding
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text

class SearchRankingAndHistoryFragment(private val listener: SelectedSearchHistoryListener) : Fragment() {

    private val db : FirebaseFirestore = FirebaseFirestore.getInstance()
    private val topSearchedRecipeList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSearchRankingAndHistoryBinding.inflate(inflater,container,false)
        val topFiveTextViewList : MutableList<TextView> = mutableListOf(
            binding.top1,
            binding.top2,
            binding.top3,
            binding.top4,
            binding.top5)

        getTopSearchedRecipe(topFiveTextViewList)
        setTopSearchedListener(topFiveTextViewList)

        val adapter = SearchHistoryAdapter(SearchHistoryRecyclerBinding.inflate(layoutInflater),listener,requireContext())
        binding.searchHistoryRecycler.layoutManager = LinearLayoutManager(requireContext(),RecyclerView.HORIZONTAL,false)
        binding.searchHistoryRecycler.adapter = adapter

        binding.allClearButton.setOnClickListener {
            adapter.deleteAllSearchHistory()
        }

        return binding.root
    }

    private fun setTopSearchedRecipe( topFiveTextViewList : MutableList<TextView>){
        val index = topSearchedRecipeList.size-1
        for ( i in 0..4){
            topFiveTextViewList[i].text = topSearchedRecipeList[index-i]
        }
    }

    private fun setTopSearchedListener( topFiveTextViewList: MutableList<TextView>){
        for( textView in topFiveTextViewList ){
            textView.setOnClickListener {
                listener.onSearchHistorySelected(textView.text.toString())
            }
        }
    }

    private fun getTopSearchedRecipe(topFiveTextViewList: MutableList<TextView>){
        db.collection("Recipe")
            .orderBy("searched")
            .get()
            .addOnSuccessListener {
                for ( recipe in it ){
                    val convertedData = recipe.toObject(RecipeInformation::class.java)
                    topSearchedRecipeList.add(convertedData.name)
                }
                setTopSearchedRecipe(topFiveTextViewList)
            }
    }

}

