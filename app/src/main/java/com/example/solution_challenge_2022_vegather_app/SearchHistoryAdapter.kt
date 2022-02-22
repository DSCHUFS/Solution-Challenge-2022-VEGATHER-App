package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.SearchHistoryRecyclerBinding

class SearchHistoryAdapter(private val binding : SearchHistoryRecyclerBinding) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val dataset = ArrayList<String>()
    private lateinit var context : Context

    inner class SearchHistoryViewHolder(val binding : SearchHistoryRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = SearchHistoryViewHolder(
        SearchHistoryRecyclerBinding.inflate(
        LayoutInflater.from(parent.context),parent,false))

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as SearchHistoryAdapter.SearchHistoryViewHolder).binding
        binding.searchHistoryText.text = dataset[position]

        binding.searchHistoryText.setOnClickListener {
            sendFoodInfoToRecipeActivity(binding.searchHistoryText.text.toString())
        }

        binding.imageButton15.setOnClickListener {
            deleteSearchHistory(position)
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun settingData(){
        for (i in 1..10){
            val s = "Masala Pasta$i"
            dataset.add(s)
        }
    }

    private fun deleteSearchHistory(position : Int){
        dataset.removeAt(position)
        if( dataset.size!=0 ){
            notifyItemRemoved(position)
        }

    }

    private fun sendFoodInfoToRecipeActivity(foodName : String){
        val intentRecipe = Intent(context,RecipeMainActivity::class.java)
        intentRecipe.putExtra("callNumberFromAdapter",2)
        intentRecipe.putExtra("foodNameFromAdapter",foodName)
        context.startActivity(intentRecipe)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteAllSearchHistory(){
        dataset.clear()
        notifyDataSetChanged()
    }

    fun loadParentActivity(c : Context){
        context = c
    }

}