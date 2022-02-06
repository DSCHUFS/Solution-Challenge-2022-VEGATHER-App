package com.example.solution_challenge_2022_vegather_app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.SearchHistoryRecyclerBinding

class SearchHistoryAdapter(private val binding : SearchHistoryRecyclerBinding) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    val dataset = ArrayList<String>()

    inner class SearchHistoryViewHolder(val binding : SearchHistoryRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = SearchHistoryViewHolder(
        SearchHistoryRecyclerBinding.inflate(
        LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        binding.textView15.text = dataset[position]
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun settingData(){
        for (i in 1..10){
            val s = "Masala Pasta"
            dataset.add(s)
        }
    }

}